// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Neq {
	public static void m1(int a) {
		Robot r = new Robot(-10,10);
		if (a > -5 && a <= 11) {
			if (a != 11) {
				// theoretically ok, but Polka domain
				// cannot handle DISEQ constraints
				// this way and is imprecise
				r.weldAt(a);	

				// ok
				if (a > 3) {
					r.weldBetween(2, a - 1);
				}
			}
		}
	}
}
