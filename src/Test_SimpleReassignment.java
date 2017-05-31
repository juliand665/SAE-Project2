public class Test_SimpleReassignment {
	public static void method(int i) {
		Robot r = new Robot(42, 42);
		r.weldAt(42);
		if (i > 0)
			r = new Robot(69, 69);
		r.weldAt(i > 0 ? 69 : 42);
	}
}
