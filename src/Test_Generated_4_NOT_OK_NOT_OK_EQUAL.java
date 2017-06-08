// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Generated_4_NOT_OK_NOT_OK_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = -1;
		if (b == -1) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
