// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestFromMailinglist {
	public static void m(int j) {
		Robot r = new Robot(-2, 6);
		
		if (j > 2 && j < 6) {
			r.weldAt(j - 2);
			//this is a bug of apron; should be valid!
			r.weldBetween(3, j + 1);
		}
	}
}
