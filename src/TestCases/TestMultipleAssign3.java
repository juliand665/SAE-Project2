// WELD_AT_NOT_OK
// WELD_BETWEEN_OK
public class TestMultipleAssign3 {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -6;
		c = -5;
		r.weldAt(c);

		c = 6;
		r.weldAt(c);
	}
}
