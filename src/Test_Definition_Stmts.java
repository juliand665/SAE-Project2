// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Definition_Stmts {
	public static void m(int a) {
		Robot r = new Robot(-30, 30);
		int x = 5;
		int y = x + 6;
		int z0 = x - y;
		int z1 = x + y;
		r.weldBetween(z0, z1);
	}
}
