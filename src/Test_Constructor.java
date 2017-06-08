// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Constructor {
	public static void m(int a) {
		Robot r = new Robot(420, -5); // left < right must hold
		r.weldAt(0);
		r.weldBetween(0, 5);
	}
}
