// WELD_AT_OK
// WELD_BETWEEN_OK
public class TestRenatoCrash {
	public static void Renato() {
		Robot r = new Robot(-5, 5);
		while(true) {
			int k = 0;
			k++;
			r.weldAt(k);
			r.weldBetween(k, k+1);
			if(k <= 1 && k >= 1) {
				break;
			}
		}
	}
}
