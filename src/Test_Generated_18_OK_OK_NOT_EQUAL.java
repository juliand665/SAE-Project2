// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_18_OK_OK_NOT_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 9;
		if (b != 8) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
