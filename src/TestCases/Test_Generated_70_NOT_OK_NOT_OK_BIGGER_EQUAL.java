// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Generated_70_NOT_OK_NOT_OK_BIGGER_EQUAL {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 13;
		if (b >= 13) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
