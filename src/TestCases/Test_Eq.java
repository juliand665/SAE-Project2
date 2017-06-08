// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Eq {
	public static void m(int a) {
		Robot r = new Robot(-10, 10);
		if (a == 3) {
			r.weldAt(a);
			r.weldBetween(a, a+1);
		}	
	}
}
