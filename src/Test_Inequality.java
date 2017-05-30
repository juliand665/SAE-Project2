
public class Test_Inequality {
	public static void test(int j) {
        Robot r = new Robot(-2, 8); // 1
        if (0 < j && j < 11){ // 2
            if (j != 9) { // 3
                if (j != 10) { // 4
                    r.weldAt(j); // 5
                }
            }
        }
    }
}
