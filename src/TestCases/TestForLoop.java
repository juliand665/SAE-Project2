// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestForLoop {
	public static void test() {
		Robot r = new Robot(0, 10);
		for(int i = 6; i <= 8; i++) {
			r.weldAt(i);
			r.weldBetween(i-1, i);
		}
	}
}
