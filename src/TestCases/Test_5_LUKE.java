// WELD_AT_NOT_OK
// WELD_BETWEEN_OK
public class Test_5_LUKE {
	public static void m5(int a, int n) {
		// expect WELD_AT_NOT_OK
		// expect WELD_BETWEEN_OK
		Robot r = new Robot(0, 2);
		if (n <= 0) {
			a = -n;
		} else {
			a = n;
		}
		r.weldAt(a);
	}
}
