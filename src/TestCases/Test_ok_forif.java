// WELD_AT_NOT_OK
// WELD_BETWEEN_NOT_OK
public class Test_ok_forif {
    public static void m1(int j) {
        Robot r = new Robot(-2, 8);
        int i = 8;

        for(int k = 1; k != i && j >= 0; k = k*2){
            r.weldAt(k);
            if(j == k){
                r.weldBetween(j-k,j);
            }
        }
    }
}
