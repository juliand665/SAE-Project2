// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Many_Bots {
	public static void m() {
		/* What happens if you create multiple robots 
		 * and let them work one after another?
		 * expect WELD_AT_OK
		 * expect WELD_BETWEEN_OK
		 */

		Robot l = new Robot(1,2);
		Robot m = new Robot(3,19);
		l.weldAt(1);
		m.weldAt(5);
		l.weldBetween(1, 2);
		Robot n = new Robot(1,30);
		Robot o = new Robot(1,20);
		Robot p = new Robot(-11,2);
		n.weldAt(30);
		Robot r = new Robot(0,5);
		n.weldBetween(2, 29);
		o.weldBetween(1, 10);
		r.weldAt(0);
		p.weldBetween(-10, 2);
	}
}


