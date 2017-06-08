// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestAliasing {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		Robot r2 = r;
		int c = -5;
		int d = -3;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);
		r2.weldAt(c);
		r2.weldBetween(c, d);
	}
}
