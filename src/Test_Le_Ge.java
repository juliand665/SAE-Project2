// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Le_Ge {
	public static void m(int x) {
		Robot r = new Robot(-10, 10);
		if (x <= 7 && x >= -8) {
			r.weldAt(x);
		}
	}
}
