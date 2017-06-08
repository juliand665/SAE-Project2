// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_26_OK_OK_NOT_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 180;
		if (b != 180) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
