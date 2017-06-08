// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_33_OK_OK_SMALLER {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 13;
		if (b < 12) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
