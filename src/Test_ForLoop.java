
public class Test_ForLoop {
	
	public static void testSeparate() {
		Robot r = new Robot(2, 3);
		
		for(int i = 0; i < 5; i++){
			if(i > 1)
				r.weldAt(i);
		}
	}
}

//WELD_AT_NOT_OK
//WELD_BETWEEN_OK
