// WELD_AT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Generated_85_OK_NOT_OK_BIGGER_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 10;
		if (b >= 10) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
