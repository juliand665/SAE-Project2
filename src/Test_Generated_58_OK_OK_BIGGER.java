// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Generated_58_OK_OK_BIGGER {
	public static void m(int a) {
		Robot r = new Robot(0, 10);
		int b = 9;
		if (b > 8) {
			r.weldAt(b);
			r.weldBetween(b, b+1);
		}
	}
}
