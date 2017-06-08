// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class TestForLoopFail {
	public static void main() {
		Robot r = new Robot(0, 5);
		for(int i = 0; i < 4; i++) {
			r.weldAt(i+3);
			r.weldBetween(i-1, i);
		}
	}
}
