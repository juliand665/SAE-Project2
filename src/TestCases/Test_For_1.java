// WELD_AT_OK
// WELD_BETWEEN_NOT_OK
public class Test_For_1 {
	public static void m() {
		Robot r = new Robot(0, 420);

		// doesn't widen
		for (int i = 10; i < 14; i++) {
			r.weldAt(i);
		}

		// widens
		for (int i = 10; i < 17; i++) {
			r.weldBetween(i, i+1);
		}

	}
}
