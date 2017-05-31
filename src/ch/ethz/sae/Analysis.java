package ch.ethz.sae;

import java.util.*;

import apron.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
import soot.util.Chain;

// Implement your numerical analysis here.
public class Analysis extends ForwardBranchedFlowAnalysis<AWrapper> {

	private static final int WIDENING_THRESHOLD = 6;

	private HashMap<Unit, Counter> loopHeads, backJumps;

	private void recordIntLocalVars() {

		Chain<Local> locals = g.getBody().getLocals();

		int count = 0;
		Iterator<Local> it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		local_ints = new String[count];

		int i = 0;
		it = locals.iterator();
		while (it.hasNext()) {
			JimpleLocal next = (JimpleLocal) it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				local_ints[i++] = name;
		}
	}

	private void recordIntClassVars() {

		Chain<SootField> ifields = jclass.getFields();

		int count = 0;
		Iterator<SootField> it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			if (next.getType() instanceof IntegerType)
				count += 1;
		}

		class_ints = new String[count];

		int i = 0;
		it = ifields.iterator();
		while (it.hasNext()) {
			SootField next = it.next();
			String name = next.getName();
			if (next.getType() instanceof IntegerType)
				class_ints[i++] = name;
		}
	}

	/* Builds an environment with integer variables. */
	public void buildEnvironment() {

		recordIntLocalVars();
		recordIntClassVars();

		//Logger.log("local vars:", Arrays.toString(local_ints));
		//Logger.log("class vars:", Arrays.toString(class_ints));

		String ints[] = new String[local_ints.length + class_ints.length];

		/* add local ints */
		for (int i = 0; i < local_ints.length; i++) {
			ints[i] = local_ints[i];
		}

		/* add class ints */
		for (int i = 0; i < class_ints.length; i++) {
			ints[local_ints.length + i] = class_ints[i];
		}

		env = new Environment(ints, reals);
	}

	/* Instantiate a domain. */
	private void instantiateDomain() {
		man = new Polka(true);
	}

	/* === Constructor === */
	public Analysis(UnitGraph g, SootClass jc) {
		super(g);

		this.g = g;
		this.jclass = jc;

		buildEnvironment();
		instantiateDomain();

		loopHeads = new HashMap<Unit, Counter>();
		backJumps = new HashMap<Unit, Counter>();
		for (Loop l : new LoopNestTree(g.getBody())) {
			loopHeads.put(l.getHead(), new Counter(0));
			backJumps.put(l.getBackJumpStmt(), new Counter(0));
		}
	}

	void run() {
		doAnalysis();
	}

	// debug output for uncaught conversion cases
	static void failedConversion(Value value, String dest) {
		Logger.logIndenting(2, "Couldn't convert value of type", value.getClass(), "to", dest);
	}

	// extracts the (arithmetic) operation of a Soot binary expression
	static int getOp(BinopExpr bin) {
		if (bin instanceof JAddExpr) return Texpr1BinNode.OP_ADD;
		if (bin instanceof JSubExpr) return Texpr1BinNode.OP_SUB;
		if (bin instanceof JMulExpr) return Texpr1BinNode.OP_MUL;
		// not even supposed to handle this: if (bin instanceof JDivExpr) return Texpr1BinNode.OP_DIV;
		failedConversion(bin, "op");
		return -1;
	}

	// converts a Soot expression (Value) to an Apron expression (Texpr1Node)
	static Texpr1Node toExpr(Value value) {
		if (value instanceof BinopExpr) {
			BinopExpr bin = (BinopExpr) value;
			int op = getOp(bin);
			Texpr1Node l = toExpr(bin.getOp1());
			Texpr1Node r = toExpr(bin.getOp2());
			return new Texpr1BinNode(op, l, r);
		}
		if (value instanceof IntConstant) {
			int val = ((IntConstant) value).value;
			return new Texpr1CstNode(new MpqScalar(val));
		}
		if (value instanceof JimpleLocal) {
			String name = ((JimpleLocal) value).getName();
			return new Texpr1VarNode(name);
		}
		// this is fine - failedConversion(value, "expr");
		return null;
	}

	/// computes the output states of applying a definition statement
	static void applyDef(DefinitionStmt def, Abstract1 fall, Abstract1 branch) throws ApronException {
		final boolean verbose = false;

		// split into operands
		Value lhs = def.getLeftOp(), rhs = def.getRightOp();
		if (verbose) Logger.log("Definition of", lhs, "(" + lhs.getClass() + ")", "as", rhs, "(" + rhs.getClass() + ")");

		// parse expressions on either side
		String var = ((JimpleLocal) lhs).getName(); // local variable to assign to
		Texpr1Node expr = toExpr(rhs);
		if (verbose) Logger.log("expr:", expr);

		if (expr != null) {
			Texpr1Intern val = new Texpr1Intern(env, expr); // value to assign
			// apply to state
			fall.assign(man, var, val, null);
			branch.assign(man, var, val, null);
		} // we can ignore anything not parsed by toExpr
	}

	/// computes the output states of applying an if statement
	static void applyIf(JIfStmt jIf, Abstract1 fall, Abstract1 branch) throws ApronException {
		final boolean verbose = false;

		// parse expressions on either side
		ConditionExpr cond = (ConditionExpr) jIf.getCondition();
		Texpr1Node l = toExpr(cond.getOp1());
		Texpr1Node r = toExpr(cond.getOp2());
		if (verbose) Logger.log(cond.getClass(), cond.getOp1().getClass(), cond.getOp2().getClass());

		// parse (in-)equality for easier logic later in toConstraint
		boolean equality = cond instanceof JEqExpr || cond instanceof JNeExpr; // ==, !=
		boolean strict = cond instanceof JGtExpr || cond instanceof JLtExpr; // >, <
		boolean negated = cond instanceof JNeExpr || cond instanceof JLeExpr || cond instanceof JLtExpr; // !=, <=, <
		if (verbose) Logger.log("=", equality, "//", "</>", strict, "//", "!", negated);

		// convert to constraints for un-/fulfilment
		Tcons1 tCons = toConstraint(l, r, equality, strict, negated);
		Tcons1 fCons = toConstraint(l, r, equality, !strict, !negated);
		if (verbose) Logger.log("true:", tCons, "//", "false:", fCons);

		// apply to state
		branch.meet(man, tCons);
		fall.meet(man, fCons);
	}

	// converts an (in-)equality of a given type to a Tcons1 linear constraint (e.g. l >= r -> l-r >= 0; l < r -> r-l > 0 -> r-l-1 >= 0)
	static Tcons1 toConstraint(Texpr1Node l, Texpr1Node r, boolean equality, boolean strict, boolean negated) {
		// if negated, constrain r-l, otherwise l-r
		Texpr1BinNode sub = new Texpr1BinNode(Texpr1BinNode.OP_SUB, negated ? r : l, negated ? l : r);
		int cons;
		if (equality)
			cons = negated ? Tcons1.DISEQ : Tcons1.EQ;
		else
			cons = strict ? Tcons1.SUP : Tcons1.SUPEQ;
		// something not to be confused by: SUP is just SUPEQ with the bound adjusted by one
		return new Tcons1(env, cons, sub);
	}

	@Override
	protected void flowThrough(AWrapper in, Unit op,
			List<AWrapper> fallOut, List<AWrapper> branchOut) {
		final boolean verbose = false;

		Stmt s = (Stmt) op;

		// debug output
		if (verbose)
			try {
				Logger.logIndenting(1, op, "-----", in, "->", fallOut, "+", branchOut);
			} catch (Exception e) {
				Logger.log("Error while trying to log:", e);
			}
		else
			Logger.logIndenting(1, op);

		try {
			Abstract1 fall = new Abstract1(man, in.get());
			Abstract1 branch = new Abstract1(man, in.get());

			// parse statement
			try {
				if (s instanceof DefinitionStmt) {
					applyDef((DefinitionStmt) s, fall, branch);
				} else if (s instanceof JIfStmt) {
					applyIf((JIfStmt) s, fall, branch);
				}
			} catch (IllegalArgumentException e) {
				// This mostly happens when variables aren't defined in our environment, which (hopefully) means we don't care about them.
				if (verbose) Logger.logIndenting(2, "Statement ignored! Illegal argument given (don't worry unless this was an int):", e);
			}

			// apply to wrappers
			for (AWrapper out : fallOut)
				out.set(fall);
			for (AWrapper out : branchOut)
				out.set(branch);

			if (verbose) Logger.logIndenting(2, "Fall through:", fallOut, "Branch:", branchOut);

		} catch (ApronException e) {
			if (verbose) Logger.log("ApronException in flowThrough:", e);
			//e.printStackTrace();
		}
	}

	@Override
	protected void copy(AWrapper source, AWrapper dest) {
		try {
			dest.set(new Abstract1(man, source.get()));
		} catch (ApronException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected AWrapper entryInitialFlow() {
		Abstract1 top = null;
		try {
			top = new Abstract1(man, env);
		} catch (ApronException e) {
		}
		return new AWrapper(top);
	}

	private static class Counter {
		int value;

		Counter(int v) {
			value = v;
		}
	}

	@Override
	protected void merge(Unit succNode, AWrapper w1, AWrapper w2, AWrapper w3) {
		Counter count = loopHeads.get(succNode);

		Abstract1 a1 = w1.get();
		Abstract1 a2 = w2.get();
		Abstract1 a3 = null;

		try {
			if (count != null) {
				++count.value;
				if (count.value < WIDENING_THRESHOLD) {
					a3 = a1.joinCopy(man, a2);
				} else {
					a3 = a1.widening(man, a2);
				}
			} else {
				a3 = a1.joinCopy(man, a2);
			}
			w3.set(a3);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	protected void merge(AWrapper src1, AWrapper src2, AWrapper trg) {

		Abstract1 a1 = src1.get();
		Abstract1 a2 = src2.get();
		Abstract1 a3 = null;

		try {
			a3 = a1.joinCopy(man, a2);
		} catch (ApronException e) {
			e.printStackTrace();
		}
		trg.set(a3);
	}

	@Override
	protected AWrapper newInitialFlow() {
		Abstract1 bot = null;

		try {
			bot = new Abstract1(man, env, true);
		} catch (ApronException e) {
		}
		AWrapper a = new AWrapper(bot);
		a.man = man;
		return a;

	}

	public static final boolean isIntValue(Value val) {
		return val.getType().toString().equals("int")
				|| val.getType().toString().equals("short")
				|| val.getType().toString().equals("byte");
	}


	public static Manager man;
	public static Environment env;
	public UnitGraph g;
	public String local_ints[]; // integer local variables of the method
	public static String reals[] = { "x" };
	public SootClass jclass;
	private String class_ints[]; // integer class variables where the method is defined
}
