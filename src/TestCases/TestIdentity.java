// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestIdentity {
	public static void m1(int b) {
		Robot r = new Robot(-5, 5);
		int a = 1;
		
		if (a == b) {
			r.weldAt(b);
			r.weldBetween(-5, b+4);
		}
	}
}
