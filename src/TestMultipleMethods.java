// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestMultipleMethods {

	public static void asdf() {
		Robot r1 = new Robot(6, 10);
		r1.weldAt(7);
		r1.weldBetween(6, 10);
	}
	
	public static void jklö() {
		Robot r1 = new Robot(0, 5);
		r1.weldAt(5);
		r1.weldBetween(0, 5);
	}
	
}
