// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_if_reassign {
	/*
	 *  yields:
	 *  WELD_AT_OK
	 *  WELD_BETWEEN_NOT_OK
	 *  
	 *  should yield:
	 *  WELD_AT_OK
	 *  WELD_BETWEEN_OK
	 *  
	 *  sound, but imprecise
	 */
	
	public static void m1(int j) {
		Robot r = new Robot(4, 10);

		if (j > 10)
			r.weldBetween(4, 5);
		else if (j < 10)
			r.weldBetween(4, 5);
		else {
			Robot r1 = new Robot(10, 11);
			r = r1;
		}

		r.weldAt(10); // Without this everything is fine and precise
	}
}
