// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestAssignmentStmtSimple {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = 2;
		int d = 3;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);
	}
}
