// WELD_AT_NOT_OK
// WELD_BETWEEN_OK
public class Test_While_No_Widen_F {
	public static void m() {
		Robot r = new Robot(-10,10);
		int x = -11;
		while (x <= -9) {
			// not ok, first call is weldAt(-11)
			r.weldAt(x);

			// ok
			r.weldBetween(x+2, x+3);
			x = x + 1;
		}
	}
}
