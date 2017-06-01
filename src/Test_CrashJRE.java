public class Test_CrashJRE {
	public static void crash(int j) { // only crashes if it takes an argument
		// minimal working example of a crashing program
		while (true) {
			int k = 1;
			if(k <= 1 && k <= 1)
				break;
		}
	}
}