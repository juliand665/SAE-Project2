
public class Test_Reassigning {
	
	public static void testEasy() {
		Robot r1 = new Robot(-40, 10);
		Robot r2 = new Robot(39, 50);
		Robot r3 = new Robot(-20, 30);
		Robot r4 = new Robot(-15, 35);
		
		r1 = r2;
		
		for(int i = 0; i < 5; i++){
			if(i > 0){
				r1.weldAt(i);
				r3.weldBetween(i, i*2);
				r3.weldAt(i);
				r4.weldBetween(i, i*3);
			}
			r3 = r4;	
		}
		
		r3 = r1;
		r3.weldBetween(40,  42);
		
		//WELD_AT_NOT_OK
		//WELD_BETWEEN_OK
	}
}