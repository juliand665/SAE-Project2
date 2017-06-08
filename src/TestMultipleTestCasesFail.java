// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class TestMultipleTestCasesFail {

	public static void asdf() {
		Robot r1 = new Robot(6, 10);
		r1.weldAt(7);
		r1.weldBetween(6, 11);
	}
	
	public static void jklö() {
		Robot r1 = new Robot(0, 5);
		r1.weldAt(6);
		r1.weldBetween(4, 5);
	}
	
	public static void O987() {
		Robot r1 = new Robot(0, 5);
		r1.weldAt(5);
		r1.weldBetween(0, 5);
	}
}
