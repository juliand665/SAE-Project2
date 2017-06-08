// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Pointer_If {
	public static void m(int a) {
		Robot r1 = new Robot(-10, 10);
		Robot r2 = new Robot(-420, 420);
		Robot r3 = new Robot(-42, 42);
		Robot r;

		if  (a < 0) {
			r = r1;
		} else if (a == 0) {
			r = r2;
		} else {
			r = r3;
		}

		r.weldAt(0);
	}
}
