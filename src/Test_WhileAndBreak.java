public class Test_WhileAndBreak {
	public static void m1(int j) {
		Robot r1 = new Robot(-5, 5);
		
		// This works fine
		int k = 0;
		while(true){
			r1.weldAt(k);
			k++;
			break;
		}
		
		// This works fine
		k = 0;
		while(true){
			r1.weldAt(k);
			k++;
			if(k > 0)
				break;
		}
		
		// This works fine
		k = 0;
		while(true){
			r1.weldAt(k);
			k++;
			if(k < 2)
				break;
		}
		
		// This works fine
		k = 0;
		if(k == 0)
			r1.weldAt(k);
		
		// This does not
		k = 0;
		while(true){
			r1.weldAt(k);
			k++;
			if(k == 1)
				break;
		}
		
		// This works again
		r1.weldAt(k);
		
	}
}