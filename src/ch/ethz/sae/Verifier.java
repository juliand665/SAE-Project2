package ch.ethz.sae;

import java.util.*;

import apron.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
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
			Logger.log();
			Logger.log("%%%%%%%%% BEGIN VERIFYING %%%%%%%%%");
			Logger.log();
			Logger.log();

			if (!verifyCallsTo("weldAt", method, analysis, pointsToAnalysis)) {
				weldAtFlag = 0;
			}
			Logger.log();
			if (!verifyCallsTo("weldBetween", method, analysis, pointsToAnalysis)) {
				weldBetweenFlag = 0;
			}

			Logger.log();
			Logger.log("%%%%%%%%%% END VERIFYING %%%%%%%%%%");
			Logger.log();
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
		// TODO: Change to handle multiple robots! (possibly with a HashMap)
		Logger.log("Verifying", methodName + "...");
		PatchingChain<Unit> ops = method.getActiveBody().getUnits();

		// get robot constraints
		Interval robotBounds = getRobotConstraints(ops);
		Logger.logIndenting(1, "Robot is allowed to weld in", robotBounds);

		// search for all calls to weldAt
		LinkedList<JInvokeStmt> invocations = getInvokeCalls(ops, methodName);
		
		return doArgsOfInvocationsLieWithinBounds(fixPoint, invocations, robotBounds);
	}
	
	private static boolean doArgsOfInvocationsLieWithinBounds(Analysis fixPoint, List<JInvokeStmt> invocations, Interval bounds) {
		// TODO take a specific robot as argument
		final boolean verbose = true;
		
		// check constraints
		Logger.logIndenting(1, "Checking constraints...");
		for (JInvokeStmt stmt : invocations) {
			AWrapper state = fixPoint.getFlowBefore(stmt);
			InvokeExpr invoke = stmt.getInvokeExpr();
			// this works for both weldAt and weldBetween
			for (Value arg : invoke.getArgs()) {
				try {
					Texpr1Node expr = Analysis.toExpr(arg);
					if (expr == null)
						break;
					Texpr1Intern inContext = new Texpr1Intern(state.get().getEnvironment(), expr);
					Interval possibleValues = state.get().getBound(state.man, inContext);

					if (verbose) Logger.logIndenting(2, "Possible welding values:", possibleValues);
					
					int comparison = bounds.cmp(possibleValues);
					if (comparison != 0 && comparison != 1) // not equal and not contained
						return false;
				} catch (ApronException e) {
					Logger.log("Caught ApronException in verification:", e);
				}
			}
		}
		return true;
	}

	private static int toInt(Value value) {
		if (value instanceof IntConstant) {
			return ((IntConstant) value).value;
		}
		Logger.log("Could not convert value", value, "to IntConstant!");
		return 0;
	}

	private static Interval getRobotConstraints(PatchingChain<Unit> ops) {
		for (Unit op : ops) {
			//search for initialization of the robot
			if (op instanceof JInvokeStmt) {
				InvokeExpr init = ((JInvokeStmt) op).getInvokeExpr();
				if (init.getMethod().isConstructor()) {
					// construct interval from arguments to robot constructor
					return new Interval(toInt(init.getArg(0)), toInt(init.getArg(1)));
				}
			}
		}

		Logger.log("Something went wrong in finding the robot constraints!");
		return new Interval(0, 0);
	}

	private static LinkedList<JInvokeStmt> getInvokeCalls(PatchingChain<Unit> ops, String stmt) {
		LinkedList<JInvokeStmt> stmts = new LinkedList<JInvokeStmt>();

		for (Unit op : ops) {
			if (op instanceof JInvokeStmt) {
				JInvokeStmt invoke = (JInvokeStmt) op;
				if (invoke.getInvokeExpr().getMethod().getName().equals(stmt))
					stmts.add(invoke);
			}
		}

		return stmts;
	}

	private static SootClass loadClass(String name) {
		SootClass c = Scene.v().loadClassAndSupport(name);
		c.setApplicationClass();
		return c;
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
