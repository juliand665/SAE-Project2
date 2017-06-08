// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_73_OK_OK_BIGGER_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 13;
		if (b >= 16) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
