package ch.ethz.sae;

import java.util.HashMap;

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
    	Value[] weldBorders = getArguments(ops);
    	Value weldLeft = weldBorders[0];
    	Value weldRight = weldBorders[1];
    	System.out.println("Robot needs to weld between " + weldLeft + " and " + weldRight);
    	
        return false;
    }

    private static boolean verifyWeldAt(SootMethod method, Analysis fixPoint, PAG pointsTo) {
    	/* TODO: check whether all calls to weldAt respect Property 1 */
    	System.out.println("\nVerify WeldAt");
    	PatchingChain<Unit> ops = method.getActiveBody().getUnits();
    	
    	// get arguments
    	Value[] weldBorders = getArguments(ops);
    	Value weldLeft = weldBorders[0];
    	Value weldRight = weldBorders[1];
    	System.out.println("Robot needs to weld at (" + weldLeft + ", " + weldRight + ")");
    	
        return false;
    }
    
    private static Value[] getArguments(PatchingChain<Unit> ops){
    	Value weldLeft = null, weldRight = null;
    	
    	for(Unit op : ops){
    		//search for initialization of the robot
    		if(op.toString().contains("<init>")){
    			System.out.println(op);
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
