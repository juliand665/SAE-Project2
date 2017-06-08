// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestMultipleAssign {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -5;
		int d = 5;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);

		c = 10;
		d = 11;
		Robot r2 = new Robot(10, 11);
		r2.weldAt(c);
		r2.weldAt(d);
		r2.weldBetween(c, d);
	}
}
