package ch.ethz.sae;

import java.util.*;

import apron.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.*;
import soot.jimple.spark.sets.*;
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
			
			Verifier verifier = new Verifier(method.retrieveActiveBody(), c, pointsToAnalysis);
			
			if (continueAfterFailure || weldAtOK)
				weldAtOK = verifier.verifyCallsTo("weldAt") && weldAtOK;
			if (continueAfterFailure || weldBetweenOK)
				weldBetweenOK = verifier.verifyCallsTo("weldBetween") && weldBetweenOK;
		}

		System.out.print(analyzedClass + " WELD_AT_");
		System.out.println(weldAtOK ? "OK" : "NOT_OK");
		
		System.out.print(analyzedClass + " WELD_BETWEEN_");
		System.out.println(weldBetweenOK ? "OK" : "NOT_OK");
	}
	
	private HashMap<Value, Interval> robotConstraints;
	private PatchingChain<Unit> ops;
	private Analysis fixPoint;
	private PAG pointsTo;
	
	private Verifier(Body body, SootClass c, PAG pointsTo) {
		this.pointsTo = pointsTo;
		
		fixPoint = new Analysis(new BriefUnitGraph(body), c);
		fixPoint.run();
		Logger.log();
		
		ops = body.getUnits();
		
		parseRobotInitializations(body.getUnits());
	}

	// analyzes all initialization of robots and saves them to robotContraints
	private void parseRobotInitializations(PatchingChain<Unit> ops) {
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
	private boolean verifyCallsTo(String methodName) {
		try {
			Logger.log("Verifying", methodName + "...");

			// search for all calls to the method
			LinkedList<JInvokeStmt> invocations = getInvokeCalls(methodName);
			if (invocations.isEmpty()) {
				Logger.logIndenting(1, "No calls to", methodName);
				return true;
			}

			return doArgsOfInvocationsLieWithinBounds(invocations);
		} catch (Exception e) {
			Logger.log("Returning false because I caught an exception:", e);
			//e.printStackTrace();
			return false;
		}
	}

	// checks if all the arguments used in weldAt/weldBetween lie within the respective constraints
	private boolean doArgsOfInvocationsLieWithinBounds(List<JInvokeStmt> invocations) throws ApronException {
		final boolean verbose = true;
		boolean constraintsViolated = false;
		Logger.logIndenting(1, "Checking constraints...");

		for (JInvokeStmt stmt : invocations) {
			Interval bounds = getCurrentConstraints(stmt);
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
				
				if (verbose) Logger.logIndenting(4, "Possible values of", arg + ":\t", state.possibleValuesOf(expr));
				
				if (!state.isLessThan(lo, expr, false) || !state.isLessThan(expr, hi, false)) {
					if (!verbose) return false;
					constraintsViolated = true;
					Logger.logConstraintViolation(arg + " might exceed robot bounds!");
				}
			}
			
			// check if, for weldBetween, arguments do not overlap
			constraintsViolated = constraintsViolated || state.doArgsOverlap(args);
			
			Logger.log();
		}
		return !constraintsViolated;
	}

	// finds all invocations of a given method
	private LinkedList<JInvokeStmt> getInvokeCalls(String methodName) {
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
	private Interval getCurrentConstraints(JInvokeStmt invoke) {
		Value robot = getCallee(invoke);
		VarNode robotNode = pointsTo.findLocalVarNode(robot);

		/* TODO use this
		PointsToSetInternal allocs = (PointsToSetInternal) pointsTo.reachingObjects((Local) robot);
		List<Integer> nums = getNumbers(allocs);
		Logger.logIndenting(2, robotNode, "points to:", nums, Arrays.toString(pointsTo.allocInvLookup(robotNode)));
		*/

		LinkedList<Value> rootReferencePointers = findRootPointers(robotNode);
		Logger.logIndenting(2, "Robot", robot, "references", rootReferencePointers);

		// Intersect all possible constraint intervals, preserving soundness at the cost of precision
		Interval intersection = new Interval();
		intersection.setTop();
		for (Value robotName : rootReferencePointers)
			intersection = intersect(intersection, robotConstraints.get(robotName));
		
		if (intersection.isTop())
			Logger.logIndenting(3, "Intersecting all possible constraints of", robot, "yielded", intersection, "(which is very likely wrong, causing unsoundness!)");

		return intersection;
	}

	// recursively finds all references a node points to
	private LinkedList<Value> findRootPointers(VarNode node) {
		LinkedList<Value> referencePointers = new LinkedList<Value>();
		Node[] pointerList = pointsTo.simpleInvLookup(node);

		if (pointerList.length == 0) {
			// Base case, node is already an object
			LocalVarNode varNode = (LocalVarNode) node;
			referencePointers.add((Value) varNode.getVariable());
		} else {
			// Node points to multiple references
			for (Node n : pointerList)
				referencePointers.addAll(0, findRootPointers((VarNode) n));
		}

		return referencePointers;
	}

	// can't believe there's no built-in for this
	private static Interval intersect(Interval i1, Interval i2) {
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
	
	// returns all the numbers of nodes in a PointsToSetInternal
	private static List<Integer> getNumbers(PointsToSetInternal pts) {
		final List<Integer> nums = new ArrayList<Integer>();
		pts.forall(new P2SetVisitor() {
			public void visit(Node n) {
				if (n instanceof AllocNode) {
					nums.add(n.getNumber());
				}
			}
		});
		return nums;
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
	private synchronized static PAG doPointsToAnalysis(SootClass c) {
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
