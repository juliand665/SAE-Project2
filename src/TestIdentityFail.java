// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class TestIdentityFail {
	public static void m2(int b) {
		Robot r = new Robot(0, 5);
		int a = -1;
		
		if (a == b) {
			r.weldAt(b);
			r.weldBetween(b - 1, 5);
		}
	}
}
