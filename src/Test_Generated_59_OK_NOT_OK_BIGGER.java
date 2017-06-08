// WELD_AT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Generated_59_OK_NOT_OK_BIGGER {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 10;
		if (b > 9) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
