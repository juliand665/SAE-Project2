// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestForLoopGotoStmt {
	public static void main() {
		Robot r = new Robot(0, 5);
		for(int i = 0; i < 4; i++) {
			r.weldAt(i);
			r.weldBetween(i, i+1);
		}
	}
}
