// WELD_AT_NOT_OK
// WELD_BETWEEN_OK
public class Test_While_Widen_2 {
	public static void m() {
		Robot r = new Robot(-10,10);
		int x = -10;
		while (x <= 10) {
			r.weldAt(x);
			x = x + 1;
		}
	}
}
