//WELD_AT_NOT_OK
//WELD_BETWEEN_NOT_OK
public class Test_5 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		if (j > 2 && j < 6) {
			r.weldAt(j - 2);
			r.weldAt(j - j);
			r.weldBetween(j - 4, j + 1);
			r.weldBetween(j - 4, j + 10);			
		}		
	}
}
