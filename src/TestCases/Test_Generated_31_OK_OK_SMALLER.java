// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_31_OK_OK_SMALLER {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = -2;
		if (b < -3) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
