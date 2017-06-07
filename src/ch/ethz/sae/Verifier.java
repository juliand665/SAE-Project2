package ch.ethz.sae;

import java.util.*;

import apron.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.DoublePointsToSet;
import soot.jimple.spark.sets.HybridPointsToSet;
import soot.jimple.spark.sets.P2SetVisitor;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.sae.Verifier <class to test>");
			System.exit(-1);
		}
		String analyzedClass = args[0];
		
		SootClass c = loadClass(analyzedClass);
		PAG pointsToAnalysis = doPointsToAnalysis(c);

		final boolean continueAfterFailure = false;
		boolean weldAtOK = true;
		boolean weldBetweenOK = true;
		for (SootMethod method : c.getMethods()) {
			
			if (method.isConstructor())
				continue;
			
			Logger.log();
			
			Analysis analysis = new Analysis(new BriefUnitGraph(method.retrieveActiveBody()), c);
			analysis.run();
			
			Logger.log();

			PatchingChain<Unit> ops = method.getActiveBody().getUnits();
			parseRobotInitializations(ops);
			
			if (continueAfterFailure || weldAtOK)
				weldAtOK = weldAtOK && verifyCallsTo("weldAt", ops, analysis, pointsToAnalysis);
			if (continueAfterFailure || weldBetweenOK)
				weldBetweenOK = weldBetweenOK && verifyCallsTo("weldBetween", ops, analysis, pointsToAnalysis);
		}

		System.out.print(analyzedClass + " WELD_AT_");
		System.out.println(weldAtOK ? "OK" : "NOT_OK");
		
		System.out.print(analyzedClass + " WELD_BETWEEN_");
		System.out.println(weldBetweenOK ? "OK" : "NOT_OK");
	}
	
	static HashMap<Value, Interval> robotConstraints;

	// analyzes all initialization of robots and saves them to robotContraints
	private static void parseRobotInitializations(PatchingChain<Unit> ops) {
		robotConstraints = new HashMap<Value, Interval>();
		for (Unit op : ops) {
			// search for initialization of the robot
			if (op instanceof JInvokeStmt) {
				JInvokeStmt invoke = (JInvokeStmt) op;
				InvokeExpr init = invoke.getInvokeExpr();
				if (init.getMethod().isConstructor()) {
					// construct interval from arguments to robot constructor
					Interval interval = new Interval(toInt(init.getArg(0)), toInt(init.getArg(1)));
					Value robotName = getCallee(invoke);
					robotConstraints.put(robotName, interval);
				}
			}
		}
		Logger.log("Original robot constraints:", robotConstraints);
		Logger.log();
	}

	// checks if all calls to the given method (weldAt/weldBetween) are valid
	private static boolean verifyCallsTo(String methodName, PatchingChain<Unit> ops, Analysis fixPoint, PAG pointsTo) {
		try {
			Logger.log("Verifying", methodName + "...");

			// search for all calls to the method
			LinkedList<JInvokeStmt> invocations = getInvokeCalls(methodName, ops);
			if (invocations.isEmpty()) {
				Logger.logIndenting(1, "No calls to", methodName);
				return true;
			}

			return doArgsOfInvocationsLieWithinBounds(invocations, fixPoint, pointsTo);
		} catch (Exception e) {
			Logger.log("Returning false because I caught an exception:", e);
			return false;
		}
	}

	// checks if all the arguments used in weldAt/weldBetween lie within the respective constraints
	private static boolean doArgsOfInvocationsLieWithinBounds(List<JInvokeStmt> invocations, Analysis fixPoint, PAG pointsTo) throws ApronException {
		final boolean verbose = true;
		boolean constraintsViolated = false;
		Logger.logIndenting(1, "Checking constraints...");

		for (JInvokeStmt stmt : invocations) {
			Interval bounds = getCurrentConstraints(stmt, pointsTo);
			Texpr1Node lo = new Texpr1CstNode(bounds.inf);
			Texpr1Node hi = new Texpr1CstNode(bounds.sup);
			
			AWrapper state = fixPoint.getFlowBefore(stmt);
			InvokeExpr invoke = stmt.getInvokeExpr();
			List<Value> args = invoke.getArgs();

			if (verbose) {
				Logger.logIndenting(3, "Allowed welding values:\t", bounds);
			}
			
			// check if individual welds violate constraints
			for (Value arg : args) {
				Texpr1Node expr = Analysis.toExpr(arg);
				
				if (verbose) {
					Texpr1Intern inContext = new Texpr1Intern(state.get().getEnvironment(), expr);
					Logger.logIndenting(4, "Possible values of", arg + ":\t", state.get().getBound(state.man, inContext));
				}
				
				if (!isLessThan(lo, expr, false, state) || !isLessThan(expr, hi, false, state)) {
					if (!verbose) return false;
					constraintsViolated = true;
					Logger.logConstraintViolation(arg + " might exceed robot bounds!");
				}
			}
			
			// check if, for weldBetween, arguments do not overlap
			constraintsViolated = constraintsViolated || doArgsOverlap(args, state);
			
			Logger.log();
		}
		return !constraintsViolated;
	}
	
	// checks if the arguments to weldBetween calls overlap (if applicable)
	private static boolean doArgsOverlap(List<Value> args, AWrapper state) throws ApronException {
		// only check for weldBetween
		if (args.size() == 2 && !isLessThan(args.get(0), args.get(1), true, state)) {
			Logger.logConstraintViolation("weldBetween arguments might overlap!");
			return true;
		}
		return false;
	}
	
	// convenient way of calling isLessThan(Texpr1Node…) with unparsed Soot Values
	private static boolean isLessThan(Value l, Value r, boolean strict, AWrapper state) throws ApronException {
		Texpr1Node lhs = Analysis.toExpr(l);
		Texpr1Node rhs = Analysis.toExpr(r);
		return isLessThan(lhs, rhs, strict, state);
	}
	
	// checks if l is guaranteed to be less than r in the given state
	private static boolean isLessThan(Texpr1Node l, Texpr1Node r, boolean strict, AWrapper state) throws ApronException {
		Texpr1Node sub = new Texpr1BinNode(Texpr1BinNode.OP_SUB, r, l);
		int op = strict ? Tcons1.SUP : Tcons1.SUPEQ;
		Tcons1 constraint = new Tcons1(state.get().getEnvironment(), op, sub);
		return state.get().satisfy(state.man, constraint);
	}

	// finds all invocations of a given method
	private static LinkedList<JInvokeStmt> getInvokeCalls(String methodName, PatchingChain<Unit> ops) {
		LinkedList<JInvokeStmt> stmts = new LinkedList<JInvokeStmt>();
		for (Unit op : ops) {
			if (op instanceof JInvokeStmt) {
				JInvokeStmt invoke = (JInvokeStmt) op;
				if (invoke.getInvokeExpr().getMethod().getName().equals(methodName))
					stmts.add(invoke);
			}
		}
		return stmts;
	}

	// finds all possible objects the robot could point to and intersects the corresponding constraint intervals
	private static Interval getCurrentConstraints(JInvokeStmt invoke, PAG pointsTo) {
		Value robot = getCallee(invoke);
		VarNode robotNode = pointsTo.findLocalVarNode(robot);

		/* TODO use this
		DoublePointsToSet allocs = (DoublePointsToSet) pointsTo.reachingObjects((Local) robot);
		final List<Integer> nums = new ArrayList<Integer>();
		allocs.forall(new P2SetVisitor() {
			public void visit(Node n) {
				if (n instanceof AllocNode) {
					nums.add(n.getNumber());
				}
			}
		});
		Logger.logIndenting(2, robotNode, "points to:", nums);*/

		LinkedList<Value> rootReferencePointers = findRootPointers(robotNode, pointsTo);
		Logger.logIndenting(2, "Robot", robot, "references", rootReferencePointers);

		// Intersect all possible constraint intervals, guaranteeing soundness at the cost of precision
		Interval intersection = new Interval();
		intersection.setTop();
		for (Value robotName : rootReferencePointers)
			intersection = intersectInterval(intersection, robotConstraints.get(robotName));
		
		if (intersection.isTop())
			Logger.logIndenting(3, "Intersecting all possible constraints of", robot, "yielded", intersection, "(which is very likely wrong, causing unsoundness!)");

		return intersection;
	}

	// can't believe there's no built-in for this
	private static Interval intersectInterval(Interval i1, Interval i2) {
		Interval interval = new Interval(1.0, -1.0);

		// Check if intervals are disjoint
		if(i1.sup.cmp(i2.inf) < 0 || i2.sup.cmp(i1.inf) < 0)
			return interval;

		// Set lower bound
		if(i1.inf.cmp(i2.inf) < 0)
			interval.setInf(i2.inf);
		else
			interval.setInf(i1.inf);

		// Set upper bound
		if(i1.sup.cmp(i2.sup) < 0)
			interval.setSup(i1.sup);
		else
			interval.setSup(i2.sup);

		return interval;
	}

	// recursively finds all references a node points to
	private static LinkedList<Value> findRootPointers(VarNode node, PAG pointsTo) {
		LinkedList<Value> referencePointers = new LinkedList<Value>();
		Node[] pointerList = pointsTo.simpleInvLookup(node);

		if (pointerList.length == 0) {
			// Base case, node is already an object
			LocalVarNode varNode = (LocalVarNode) node;
			referencePointers.add((Value) varNode.getVariable());
		} else {
			// Node points to multiple references
			for (Node n : pointerList)
				referencePointers.addAll(0, findRootPointers((VarNode) n, pointsTo));
		}

		return referencePointers;
	}

	// extracts the constant int value out of a Soot Value (which is assumed to be an IntConstant)
	private static int toInt(Value value) {
		if (value instanceof IntConstant)
			return ((IntConstant) value).value;
		Logger.log(value, "is unexpectedly not an IntConstant!");
		return 0;
	}

	// extracts the callee an invoke statement is called on
	private static Value getCallee(JInvokeStmt stmt) {
		if (!(stmt.getInvokeExpr() instanceof InstanceInvokeExpr)) {
			Logger.log("InvokeExpr", stmt.getInvokeExpr(), "is not an InstanceInvokeExpr! D:");
			return null;
		}
		InstanceInvokeExpr invoke = (InstanceInvokeExpr) stmt.getInvokeExpr();
		return invoke.getBase();
	}

	// Soot setup
	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}

	// performs points-to analysis
	private static PAG doPointsToAnalysis(SootClass c) {
		Scene.v().setEntryPoints(c.getMethods());

		HashMap<String, String> options = new HashMap<String, String>();
		options.put("enabled", "true");
		options.put("verbose", "false");
		options.put("propagator", "worklist");
		options.put("simple-edges-bidirectional", "false");
		options.put("on-fly-cg", "true");
		options.put("set-impl", "double");
		options.put("double-set-old", "hybrid");
		options.put("double-set-new", "hybrid");

		SparkTransformer.v().transform("", options);
		PAG pag = (PAG) Scene.v().getPointsToAnalysis();

		return pag;
	}

}
