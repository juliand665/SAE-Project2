package ch.ethz.sae;

import java.util.*;

import apron.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.*;
import soot.toolkits.graph.BriefUnitGraph;

public class Verifier {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java -classpath soot-2.5.0.jar:./bin ch.ethz.sae.Verifier <class to test>");
			System.exit(-1);
		}
		String analyzedClass = "Test_SimpleReassignment";// TODO args[0];
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
	}

	private static boolean doArgsOfInvocationsLieWithinBounds(Analysis fixPoint, List<JInvokeStmt> invocations, PAG pointsTo, HashMap<Value, Interval> robotConstraints) {
		final boolean verbose = false;
		// check constraints
		Logger.logIndenting(1, "Checking constraints...");

		for (JInvokeStmt stmt : invocations) {
			Interval bounds = getCurrentConstraints(stmt, pointsTo, robotConstraints);
			if (verbose) Logger.logIndenting(3, "Robot", getCallee(stmt), ": Allowed welding values:", bounds);

			AWrapper state = fixPoint.getFlowBefore(stmt);
			InvokeExpr invoke = stmt.getInvokeExpr();
			// this works for both weldAt and weldBetween
			for (Value arg : invoke.getArgs()) {
				Texpr1Node expr = Analysis.toExpr(arg);
				if (expr == null)
					break;
				Texpr1Intern inContext = new Texpr1Intern(state.get().getEnvironment(), expr);
				Interval possibleValues = new Interval();
				try {
					possibleValues = state.get().getBound(state.man, inContext);
				} catch (ApronException e) {
					Logger.log("Caught ApronException in verification:", e);
				}

				Logger.logIndenting(3, "Possible welding values:", possibleValues);

				int comparison = bounds.cmp(possibleValues);
				if (comparison != 0 && comparison != 1) // not equal and not contained
					return false;
			}
		}
		return true;
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
		
		/* TODO this should work with context but doesn't! Although it looks like without context is just as precise...
		Logger.logIndenting(2, "reaching without context:", pointsTo.reachingObjects((Local) robotNode.getVariable()),
				"// with context:", pointsTo.reachingObjects(invoke, (Local) robotNode.getVariable()));*/
		
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
		Interval interval = new Interval();
		interval.setBottom();

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
