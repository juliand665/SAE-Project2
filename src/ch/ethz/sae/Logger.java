package ch.ethz.sae;

import apron.Interval;

public class Logger {

	public static final boolean LOG_DEBUG = true;
	public static final String INDENTATION = "  ";
	
	// helper function for debug output
	public static void log(Object... objs) {
		logIndenting(0, objs);
	}

	public static void logIndenting(int indentation, Object... objs) {
		if (!LOG_DEBUG) return;

		System.out.print("[Debug] ");

		for (int i = 0; i < indentation; i++)
			System.out.print(INDENTATION);
		
		doLog(objs);
	}
	
	private static void doLog(Object[] objs) {
		int count = objs.length;
		if (count == 0) {
			System.out.println();
		} else {
			for (int i = 0; i < count - 1; i++)
				print(objs[i] + " ");
			println(objs[count - 1]);
		}
	}
	
	private static void print(Object o){
		if(o instanceof Interval && ((Interval)o).isBottom()){
			System.out.print("[ - ]");
		}else{
			System.out.print(o);
		}
	}
	
	private static void println(Object o){
		print(o);
		System.out.println();
	}
}
