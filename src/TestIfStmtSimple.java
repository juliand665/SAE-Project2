// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestIfStmtSimple {
	public static void m3(int a, int b) {
		Robot r = new Robot(-5, 5);
		int c = -5;
		int d = 5;
		if (c < d)
		{
		    r.weldAt(c);
		}
		r.weldBetween(c, d);
	}
}
