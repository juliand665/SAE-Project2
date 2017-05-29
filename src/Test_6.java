public class Test_6 {
	public static void m1(int j) {
		Robot r = new Robot(-2, 6);
		int s = 1;
		if (j > 2 && j < 6 && s < 1) {
			r.weldAt(j - 10);
			r.weldBetween(j - 4, j + 1);		
		}else if(j > 2 && j < 6){
			r.weldAt(j - 1);
			r.weldBetween(j - 4, j + 1);	
		}
	}
}
