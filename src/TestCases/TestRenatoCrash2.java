// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestRenatoCrash2 {
	public static void Renato() {
		Robot r = new Robot(-5, 5);
		int k = 0;
		while(true) {
			k++;
			r.weldAt(k);
			r.weldBetween(k, k+1);
			if(k <= 1 && k >= 1) {
				break;
			}
		}
	}
}
