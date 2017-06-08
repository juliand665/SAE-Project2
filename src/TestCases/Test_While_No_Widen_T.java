// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_While_No_Widen_T {
	public static void m() {
		Robot r = new Robot(-10,10);
		int x = -1;
		while (x <= 2) {
			r.weldAt(x);
			x = x + 1;
		}
	}
}
