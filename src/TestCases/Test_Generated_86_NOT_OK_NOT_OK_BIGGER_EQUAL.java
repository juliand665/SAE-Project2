// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Generated_86_NOT_OK_NOT_OK_BIGGER_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 11;
		if (b >= 10) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
