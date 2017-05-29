public class Test_7 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 10);
		Robot r2 = r;
		int weldAt = 0;
		if (j > 2 && j < 6) {
			r2.weldAt(j + 1);
			r2.weldAt(j + 5);
			r2.weldAt(j + 1);
			r2.weldBetween(j - 4, j + 1);		
		}
	}
}
