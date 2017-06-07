public class Test_ArgOverlap {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);

		// Yields WELD_BETWEEN_NOT_OK, but it's a pretty big overapproximation (but sound)
		if (j < 6 && j > -2)
			r.weldBetween(j, j + 1);
	}
}