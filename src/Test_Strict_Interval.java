// WELD_AT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Strict_Interval {
	public static void m() {
		Robot r = new Robot(0,5);
		// expect WELD_BETWEEN_NOT_OK, as interval borders are not strictly ordered.
		r.weldBetween(4,4);
	}
}