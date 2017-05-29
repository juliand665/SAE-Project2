package ch.ethz.sae;

import java.util.HashMap;
import java.util.LinkedList;

import apron.Abstract1;
import apron.ApronException;
import apron.DoubleScalar;
import apron.Interval;
import apron.Scalar;
import soot.jimple.internal.ImmediateBox;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JSpecialInvokeExpr;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.Immediate;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.BriefUnitGraph;
import soot.jimple.*;

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

			if (!verifyWeldAt(method, analysis, pointsToAnalysis)) {
				weldAtFlag = 0;
			}
			Logger.log();
			if (!verifyWeldBetween(method, analysis, pointsToAnalysis)) {
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

	private static boolean verifyWeldBetween(SootMethod method, Analysis fixPoint, PAG pointsTo) {
		// TODO: change to using hashmaps just as in weldAt, avoid duplicated code
		Logger.log("Verifying WeldBetween...");
		PatchingChain<Unit> ops = method.getActiveBody().getUnits();

		// get arguments
		Interval constraintsInterval = getRobotConstraints(ops);
		Logger.logIndenting(1, "Robot is allowed to weld in", constraintsInterval);

		// search for all calls to weldBetween
		LinkedList<JInvokeStmt> invokeCalls = getInvokeCalls(ops, "weldBetween");

		// check constraints
		Logger.logIndenting(1, "Checking constraints...");
		for (JInvokeStmt stmt : invokeCalls) {
			// get variable names
			String weldLeftName = null, weldRightName = null;
			for (Object s : stmt.getUseBoxes()) {
				if (s instanceof ImmediateBox) {
					if (weldLeftName == null) {
						weldLeftName = ((ImmediateBox) s).getValue().toString();
					} else {
						weldRightName = ((ImmediateBox) s).getValue().toString();
					}
				}
			}
			Logger.logIndenting(2, "Robot will weld between", weldLeftName, "and", weldRightName);

			// get intervals for the two variables
			AWrapper a = fixPoint.getFlowBefore(stmt);
			Logger.logIndenting(2, "Abstract domain:", a);
			Interval intervalLeft = null, intervalRight = null;
			try {
				intervalLeft = a.get().getBound(a.man, weldLeftName);
				intervalRight = a.get().getBound(a.man, weldRightName);
			} catch (ApronException e) {
				e.printStackTrace();
			}
			Logger.logIndenting(2, "Possible values for", weldLeftName + ":", intervalLeft);
			Logger.logIndenting(2, "Possible values for", weldRightName + ":", intervalRight);

			// test intervals against constraints
			boolean satisfiesLeftConstraint = false;
			boolean satisfiesRightConstraint = false;
			try {
				satisfiesLeftConstraint = a.get().satisfy(a.man, weldLeftName, constraintsInterval);
				satisfiesRightConstraint = a.get().satisfy(a.man, weldRightName, constraintsInterval);
			} catch (ApronException e) {
				e.printStackTrace();
			}
			Logger.logIndenting(2, "Is", intervalLeft, "in", constraintsInterval + "?", satisfiesLeftConstraint);
			Logger.logIndenting(2, "Is", intervalRight, "in", constraintsInterval + "?", satisfiesRightConstraint);
			Logger.log();

			if (!satisfiesLeftConstraint || !satisfiesRightConstraint) {
				return false;
			}
		}
		return true;
	}

	private static boolean verifyWeldAt(SootMethod method, Analysis fixPoint, PAG pointsTo) {
		// TODO: change to be able to use multiple robots (hashmap)
		Logger.log("Verifying WeldAt...");
		PatchingChain<Unit> ops = method.getActiveBody().getUnits();

		// get arguments
		// TODO: change output of getRobotsConstraint to hashmap<robot_id, interval>
		Interval constraintsInterval = getRobotConstraints(ops);
		Logger.logIndenting(1, "Robot is allowed to weld in", constraintsInterval);

		// search for all calls to weldAt
		// TODO: change output to hashmap<robot_id, JInvokeStmt>
		LinkedList<JInvokeStmt> invokeCalls = getInvokeCalls(ops, "weldAt");

		// check constraints
		Logger.logIndenting(1, "Checking constraints...");
		for (JInvokeStmt stmt : invokeCalls) {
			// get variable names
			String weldPositionName = null;
			for (Object s : stmt.getUseBoxes()) {
				if (s instanceof ImmediateBox) {
					weldPositionName = ((ImmediateBox) s).getValue().toString();
				}
			}
			Logger.logIndenting(2, "Robot will weld at", weldPositionName);

			// get interval for the position variable
			// TODO: update to using hashmap, only cosmetic
			AWrapper a = fixPoint.getFlowBefore(stmt);
			Logger.logIndenting(2, "Abstract domain: " + a);
			Interval positionInterval = null;
			try {
				positionInterval = a.get().getBound(a.man, weldPositionName);
			} catch (ApronException e) {
				e.printStackTrace();
			}
			Logger.logIndenting(2, "Possible domain for", weldPositionName + ":", positionInterval);

			// test interval against constraints
			boolean satisfiesConstraint = false;
			try {
				// TODO: find robot associated with the JInvokeStmt and change constrainsInterval accordingly
				satisfiesConstraint = a.get().satisfy(a.man, weldPositionName, constraintsInterval);
			} catch (ApronException e) {
				e.printStackTrace();
			}
			Logger.logIndenting(2, "Is", weldPositionName, "in", constraintsInterval + "?", satisfiesConstraint);
			Logger.log();

			if(!satisfiesConstraint)
				return false;
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
