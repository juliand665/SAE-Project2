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
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
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
        String analyzedClass = "Test_5";
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
            
            if (!verifyWeldAt(method, analysis, pointsToAnalysis)) {
                weldAtFlag = 0;
            }
            if (!verifyWeldBetween(method, analysis, pointsToAnalysis)) {
                weldBetweenFlag = 0;
            }
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
    	/* TODO: check whether all calls to weldBetween respect Property 2 */
    	System.out.println("\nVerify WeldBetween");
    	PatchingChain<Unit> ops = method.getActiveBody().getUnits();
    	
    	// get arguments
    	Value[] weldConstraints = getRobotConstraints(ops);
    	int weldLeftConstraint = ((IntConstant)weldConstraints[0]).value;
    	int weldRightConstraint = ((IntConstant)weldConstraints[1]).value;;
    	Interval constraintsInterval = new Interval(weldLeftConstraint, weldRightConstraint);
    	System.out.println("\t" + "Robot needs to weld between " + weldLeftConstraint + " and " + weldRightConstraint);
    	
    	// search for all calls to weldBetween
    	LinkedList<JInvokeStmt> invokeCalls = getInvokeCalls(ops, "weldBetween");
    	
    	// check constraints
    	System.out.println("\tCheck constraints...");
    	boolean checkAllConstraints = true;
    	for(JInvokeStmt stmt : invokeCalls){
    		// get variable names
    		String weldLeftName = null, weldRightName = null;
    		for(Object s : stmt.getUseBoxes()){
    			if(s instanceof ImmediateBox){
    				if(weldLeftName == null){
    					weldLeftName = ((ImmediateBox) s).getValue().toString();
    				}else{
    					weldRightName = ((ImmediateBox) s).getValue().toString();
    				}
    			}
    		}
    		System.out.println("\t\tRobot will weld between " + weldLeftName + " and " + weldRightName);
    		

    		// get intervals for the two variables
    		AWrapper a = fixPoint.getFlowBefore(stmt);
    		System.out.println("\t\tAbstract domain: " + a);
    		Interval intervalLeft = null, intervalRight = null;
    		try {
    			intervalLeft = a.elem.getBound(a.man, weldLeftName);
    			intervalRight = a.elem.getBound(a.man, weldRightName);
			} catch (ApronException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\t\tPossible values for " + weldLeftName + ": " + intervalLeft);
			System.out.println("\t\tPossible values for " + weldRightName + ": " + intervalRight);
    		
			
			// test interval against constraints
			boolean satisfiesLeftConstraint = false;
			boolean satisfiesRightConstraint = false;
			try {
				satisfiesLeftConstraint = a.elem.satisfy(a.man, weldLeftName, constraintsInterval);
				satisfiesRightConstraint = a.elem.satisfy(a.man, weldRightName, constraintsInterval);
			} catch (ApronException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\t\tIs " + intervalLeft + " in " + constraintsInterval + ": " + satisfiesLeftConstraint);
			System.out.println("\t\tIs " + intervalRight + " in " + constraintsInterval + ": " + satisfiesRightConstraint + "\n");
			
			if(!satisfiesLeftConstraint || !satisfiesRightConstraint){
				checkAllConstraints = false;
			}
    	}
    	
    	System.out.println("\n\tDid all the constraints hold: " + checkAllConstraints + "\n");
        return checkAllConstraints;
    }

    private static boolean verifyWeldAt(SootMethod method, Analysis fixPoint, PAG pointsTo) {
    	/* TODO: check whether all calls to weldAt respect Property 1 */
    	System.out.println("\nVerify WeldAt");
    	PatchingChain<Unit> ops = method.getActiveBody().getUnits();
    	
    	// get arguments
    	Value[] weldConstraints = getRobotConstraints(ops);
    	Value weldLeftConstraint = weldConstraints[0];
    	Value weldRightConstraint = weldConstraints[1];
    	System.out.println("\t" + "Robot needs to weld at (" + weldLeftConstraint + ", " + weldRightConstraint + ")");
    	
    	// search for all calls to weldAt
    	LinkedList<JInvokeStmt> invokeCalls = getInvokeCalls(ops, "weldAt");
    	
        return false;
    }
    
    private static Value[] getRobotConstraints(PatchingChain<Unit> ops){
    	Value weldLeft = null, weldRight = null;
    	
    	for(Unit op : ops){
    		//search for initialization of the robot
    		if(op.toString().contains("<init>")){
    			System.out.println("\t" + op);
    			JInvokeStmt init = (JInvokeStmt) op;
    			// search for arguments
    			boolean leftAssigned = false;
    			for(Object o : init.getUseBoxes()){
    				if(o instanceof ImmediateBox){
    					if(!leftAssigned){
    						weldLeft = ((ImmediateBox)o).getValue();
    						leftAssigned = true;
    					}else{
    						weldRight = ((ImmediateBox)o).getValue();
    					}
    				}
    			}
    		}
    	}
    	
    	if(weldLeft == null || weldRight == null){
    		System.out.println("Something went wrong in finding th arguments :/");
    	}
    	return new Value[] {weldLeft, weldRight};
    }
    
    private static LinkedList<JInvokeStmt> getInvokeCalls(PatchingChain<Unit> ops, String stmt){
    	if(stmt != "weldAt" && stmt != "weldBetween"){
    		System.out.println("Error, stmt not defined properly");
    		return null;
    	}
    	
    	LinkedList<JInvokeStmt> stmts = new LinkedList<JInvokeStmt>();
    	for(Unit op : ops){
    		if(op.toString().contains(stmt)){
    			stmts.add((JInvokeStmt) op);
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
