// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_WeldBetween {
	
	// this tests all branches of verifyWeldBetween
	// expected restult is:
	// WELD_AT_OK
	// WELD_BETWEEN_OK

	// IntConstant, IntConstant
	public static void m1() {
		Robot r1 = new Robot(-10, 10);
		r1.weldBetween(0,1);
	}

	// JimpleLocal, JimpleLocal
	public static void m2() {
		Robot r1 = new Robot(-11, 11);
		int x = 3;
		int y = 6;
		r1.weldBetween(x,y);
	}

	// JimpleLocal, IntConstant
	public static void m3() {
		Robot r1 = new Robot(-12, 12);
		int x = 0;
		r1.weldBetween(x,4);
	}

	// IntConstant, JimpleLocal
	public static void m4() {
		Robot r1 = new Robot(-13, 13);
		int y = 13;
		r1.weldBetween(-12,y);
	}

	// pointer analysis
	public static void m5(int a) {
		Robot r1 = new Robot(-14, 14);
		Robot r2 = new Robot(-100, 420);
		if  (a < 0) {
			r1 = r2;
		}
		r1.weldBetween(0,1);
	}
}
