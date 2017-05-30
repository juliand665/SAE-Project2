
public class Test_MultipleRobots {
	
	public static void testSeparate() {
		Robot r1 = new Robot(0, 0);
		Robot r2 = new Robot(42, 42);
		r1.weldAt(0);
		r2.weldAt(42);
	}

}
