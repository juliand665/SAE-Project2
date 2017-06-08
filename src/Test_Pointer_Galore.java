// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_Pointer_Galore {
	public static void m1(int a) {
		Robot r1 = new Robot(-10,10);
		Robot r2 = new Robot(-20,10);
		Robot r3 = new Robot(-30,10);
		Robot r4 = new Robot(-40,10);
		Robot r5 = new Robot(0,100);
		Robot rx = r3;
		Robot ry = r5;
		if (a < 100) {
			rx = r1;
			ry = r2;
		}
		if (a < 101) {
			rx = r2;
			ry = r3;
		}
		if (a < 102) {
			rx = r3;
			ry = r4;
		}
		if (a < 103) {
			rx = r4;
			ry = r5;
		}
		if (a < 104) {
			rx = r5;
			ry = r1;
		}

		rx.weldAt(0); 		// works
		ry.weldAt(-15); 	// doesn't work
		rx.weldBetween(0,5); 	// works
		ry.weldBetween(230,0); 	// doesn't work
		
	}
}
