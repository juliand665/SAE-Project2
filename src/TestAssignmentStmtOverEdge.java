// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class TestAssignmentStmtOverEdge {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -6;
		int d = 5;
		r.weldAt(c);
		r.weldAt(d);
		r.weldBetween(c, d);
	}
}
