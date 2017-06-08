// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_CrashJRE {
	// minimal working example of a crashing program
	public static void crash() {
		int k = 1;
		while (true) if (k <= 1 && k <= 1) break;
	}
}
