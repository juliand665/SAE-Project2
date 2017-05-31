public class Test_Loops {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		
		int k = 0;
		for(k = 0; k < 5; k++){
			r.weldAt(k);
		}
		
		
		k = 0;
		do {
			r.weldAt(k);
			k++;
		} while (k < 5);
		
		
		k = 0;
		while (k < 5) {
			r.weldAt(k);
			k++;
		}
		
		
		r.weldAt(k);
	}
}
