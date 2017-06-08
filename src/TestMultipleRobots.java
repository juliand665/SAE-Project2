// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestMultipleRobots {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -5;
		int d = 5;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);

		int c2 = -3;
		int d2 = 3;
		Robot r2 = new Robot(-3, 3);
		r2.weldAt(c2);
		r2.weldAt(d2);
		r2.weldBetween(c2, d2);
	}
}
