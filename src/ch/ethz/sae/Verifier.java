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
	/*TODO:
	 *  - Fix crash for certain programs as inputs
	 *    Test_CrashJRE
	 *    
	 *  - Fix argument overlap detection
	 *    Test_ArgOverlap
	 */

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.sae.Verifier <class to test>");
			System.exit(-1);
		}
		String analyzedClass = args[0];
		SootClass c = loadClass(analyzedClass);

		PAG pointsToAnalysis = doPointsToAnalysis(c);

		int weldAtFlag = 1;
		int weldBetweenFlag = 1;

		for (SootMethod method : c.getMethods()) {

			if (method.getName().contains("<init>")) {
				// skip constructor of the class
				continue;
			}
			Analysis analysis = new Analysis(new BriefUnitGraph(method.retrieveActiveBody()), c);
			analysis.run();

			Logger.log();
			Logger.log("BEGIN VERIFYING");
			Logger.log();

			if (!verifyCallsTo("weldAt", method, analysis, pointsToAnalysis)) {
				weldAtFlag = 0;
			}
			Logger.log();

			if (!verifyCallsTo("weldBetween", method, analysis, pointsToAnalysis)) {
				weldBetweenFlag = 0;
			}
			Logger.log();

			Logger.log("END VERIFYING");
			Logger.log();
		}

		// Do not change the output format
		if (weldAtFlag == 1) {
			System.out.println(analyzedClass + " WELD_AT_OK");
		} else {
			System.out.println(analyzedClass + " WELD_AT_NOT_OK");
		}
		if (weldBetweenFlag == 1) {
			System.out.println(analyzedClass + " WELD_BETWEEN_OK");
		} else {
			System.out.println(analyzedClass + " WELD_BETWEEN_NOT_OK");
		}
	}

	private static boolean verifyCallsTo(String methodName, SootMethod method, Analysis fixPoint, PAG pointsTo) {
		try {
			Logger.log("Verifying", methodName + "...");
			PatchingChain<Unit> ops = method.getActiveBody().getUnits();

			// search for all calls to the method
			LinkedList<JInvokeStmt> invocations = getInvokeCalls(ops, methodName, pointsTo);
			if (invocations.isEmpty()) {
				Logger.logIndenting(1, "No calls to", methodName);
				return true;
			}

			// get original robot constraints
			HashMap<Value, Interval> robotConstraints = getRobotConstraints(ops);
			Logger.logIndenting(1, "Original robot constraints:", robotConstraints);

			return doArgsOfInvocationsLieWithinBounds(fixPoint, invocations, pointsTo, robotConstraints);
		} catch (Exception e) {
			Logger.log("Returning false because I caught an exception:", e);
			return false;
		}
	}

	private static boolean doArgsOfInvocationsLieWithinBounds(Analysis fixPoint, List<JInvokeStmt> invocations, PAG pointsTo, HashMap<Value, Interval> robotConstraints) throws ApronException {
		final boolean verbose = true;
		boolean constraintsViolated = false;
		Logger.logIndenting(1, "Checking constraints...");

		for (JInvokeStmt stmt : invocations) {
			Interval bounds = getCurrentConstraints(stmt, pointsTo, robotConstraints);
			Texpr1Node lo = new Texpr1CstNode(bounds.inf);
			Texpr1Node hi = new Texpr1CstNode(bounds.sup);
			
			AWrapper state = fixPoint.getFlowBefore(stmt);
			InvokeExpr invoke = stmt.getInvokeExpr();
			List<Value> args = invoke.getArgs();

			if (verbose) {
				Logger.logIndenting(3, "Allowed welding values:\t", bounds);
			}
			
			// Check if individual welds violate constraints
			for (Value arg : args) {
				Texpr1Node expr = Analysis.toExpr(arg);
				
				if (verbose) {
					Texpr1Intern inContext = new Texpr1Intern(state.get().getEnvironment(), expr);
					Logger.logIndenting(4, "Possible values for", arg + ":\t", state.get().getBound(state.man, inContext), "(overapproximated)");
				}
				
				if (!isLessThan(false, state, lo, expr) || !isLessThan(false, state, expr, hi)) {
					constraintsViolated = true;
					if (!verbose) return false;
					Logger.logConstraintViolation();
				}
			}
			
			// Check if, for weldBetween, arguments do not overlap
			constraintsViolated = constraintsViolated || doArgsOverlap(state, args);
			Logger.log();
		}
		return !constraintsViolated;
	}
	
	private static boolean doArgsOverlap(AWrapper state, List<Value> args) throws ApronException {
		// only check for weldBetween
		if (args.size() == 2 && !isLessThan(true, state, args.get(0), args.get(1))) {
			Logger.logConstraintViolation("weldBetween arguments overlap!");
			return true;
		}
		return false;
	}
	
	private static boolean isLessThan(boolean strict, AWrapper state, Value l, Value r) throws ApronException {
		Texpr1Node lhs = Analysis.toExpr(l);
		Texpr1Node rhs = Analysis.toExpr(r);
		return isLessThan(strict, state, lhs, rhs);
	}
	
	private static boolean isLessThan(boolean strict, AWrapper state, Texpr1Node l, Texpr1Node r) throws ApronException {
		Texpr1Node sub = new Texpr1BinNode(Texpr1BinNode.OP_SUB, r, l);
		int op = strict ? Tcons1.SUP : Tcons1.SUPEQ;
		Tcons1 constraint = new Tcons1(state.get().getEnvironment(), op, sub);
		return state.get().satisfy(state.man, constraint);
	}

	private static LinkedList<JInvokeStmt> getInvokeCalls(PatchingChain<Unit> ops, String stmt, PAG pointsTo) {
		LinkedList<JInvokeStmt> stmts = new LinkedList<JInvokeStmt>();

		for (Unit op : ops) {
			if (op instanceof JInvokeStmt) {
				JInvokeStmt invoke = (JInvokeStmt) op;
				if (invoke.getInvokeExpr().getMethod().getName().equals(stmt)){
					stmts.add(invoke);
				}
			}
		}

		return stmts;
	}

	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
	}

	private static int toInt(Value value) {
		if (value instanceof IntConstant) {
			return ((IntConstant) value).value;
		}
		Logger.log("Could not convert value", value, "to IntConstant!");
		return 0;
	}

	private static HashMap<Value, Interval> getRobotConstraints(PatchingChain<Unit> ops) {
		HashMap<Value, Interval> constraints = new HashMap<Value, Interval>();
		for (Unit op : ops) {
			// search for initialization of the robot
			if (op instanceof JInvokeStmt) {
				JInvokeStmt invoke = (JInvokeStmt) op;
				InvokeExpr init = invoke.getInvokeExpr();
				if (init.getMethod().isConstructor()) {
					// construct interval from arguments to robot constructor
					Interval interval = new Interval(toInt(init.getArg(0)), toInt(init.getArg(1)));
					Value robotName = getCallee(invoke);
					constraints.put(robotName, interval);
				}
			}
		}

		return constraints;
	}

	private static Value getCallee(JInvokeStmt stmt) {
		if (!(stmt.getInvokeExpr() instanceof InstanceInvokeExpr)) {
			Logger.log("InvokeExpr", stmt.getInvokeExpr(), "is not an InstanceInvokeExpr! D:");
			return null;
		}
		InstanceInvokeExpr invoke = (InstanceInvokeExpr) stmt.getInvokeExpr();
		return invoke.getBase();
	}

	/*
	 * Finds all possible objects the robot could point to and performs an
	 * intersection of the according intervals
	 */
	private static Interval getCurrentConstraints(JInvokeStmt invoke, PAG pointsTo, HashMap<Value, Interval> originalConstraints) {
		// Get all possible references
		Value robot = getCallee(invoke);
		VarNode robotNode = pointsTo.findLocalVarNode(robot);

		/* TODO:
		 * Context does not seem to be necessary as robots in different JInvokeStmt are treated
		 * as different objects by soot. Might be because of the pointer analysis.
		 * 
		 * pointsTo.reachingObjects(invoke, (Local) robotNode.getVariable()));
		 */
		Logger.logIndenting(2, "Points to:");
		DoublePointsToSet allocs = (DoublePointsToSet) pointsTo.reachingObjects((Local) robotNode.getVariable());
		HybridPointsToSet hybrid = (HybridPointsToSet) allocs.getOldSet();
		hybrid.forall(new P2SetVisitor() {
			public void visit(Node n) {
				Logger.logIndenting(3, n);
			}
		});

		LinkedList<Value> rootReferencePointers = findRootPointers(robotNode, pointsTo);
		Logger.logIndenting(2, "Robot", robot, "references", rootReferencePointers);

		// Calculate intersection
		Interval intervalIntersected = new Interval();
		intervalIntersected.setTop();
		for(Value robotName : rootReferencePointers){
			Interval interval = originalConstraints.get(robotName);
			intervalIntersected = intersectInterval(intervalIntersected, interval);
		}

		return intervalIntersected;
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

	// Recursively finds all references node points to
	private static LinkedList<Value> findRootPointers(VarNode node, PAG pointsTo) {
		LinkedList<Value> referencePointers = new LinkedList<Value>();
		Node[] pointerList = pointsTo.simpleInvLookup(node);

		if (pointerList.length == 0) {
			// Base case, node is already an object
			LocalVarNode varNode = (LocalVarNode) node;
			referencePointers.add((Value) varNode.getVariable());
		} else {
			// Node points to multiple references
			for (Node n : pointerList) {
				referencePointers.addAll(0, findRootPointers((VarNode) n, pointsTo)); 
			}
		}

		return referencePointers;
	}

	// Performs Points-To Analysis
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
