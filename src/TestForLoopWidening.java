// WELD_AT_NOT_OK
// WELD_BETWEEN_OK
public class TestForLoopWidening {
	public static void something() {
		Robot r = new Robot(-10000, 10000);
		for(int i = -10000; i < 10000; i++) {
			r.weldAt(i);
		}
	}
}
