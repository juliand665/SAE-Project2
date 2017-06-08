//WELD_AT_OK
//WELD_BETWEEN_OK
public class Test_Unreachable {
	public static void m1(int j) {
		Robot r = new Robot(-2, 10);
		
		boolean falseCondition = false;
		if(falseCondition){
			r.weldAt(-3);
		}
		r.weldAt(2);
	}
}
