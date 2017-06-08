// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_4 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 2);
		int k = j;
		int l = j;
		r.weldAt(k - l); // should be OK
		r.weldBetween(k - l - 1, k - l + 1); // should be OK
	}
}
