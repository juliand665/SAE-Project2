
public class Test_MultipleRobots {
	
	public static void testSeparate() {
		Robot r1 = new Robot(0, 0);
		Robot r2 = new Robot(42, 42);
		r1.weldAt(0);
		r2.weldAt(42);
	}
	
	public static void testEasy() {
		Robot r = new Robot(0, 10);
		for (int i = 0; i < 2; i++) {
			r.weldAt(5);
			r = new Robot(0, 5);
		}
	}

	public static void testHard() {
		Robot r = new Robot(5, 5);
		for (int i = 0; i < 2; i++) {
			r.weldAt(5 - i);
			r = new Robot(4, 4);
		}
	}
}
