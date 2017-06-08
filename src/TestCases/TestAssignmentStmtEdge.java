// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestAssignmentStmtEdge {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -5;
		int d = 5;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);
	}
}
