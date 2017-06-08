// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_109_OK_OK_SMALLER_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 5;
		if (b <= 7) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
