// WELD_AT_OK
// WELD_BETWEEN_OK
public class Test_Large {
	public static void m1(int a, int b, int c) {
		Robot r1 = new Robot(-100, 100);
		Robot r2 = new Robot(-200, 200);
		if (a != 50) {
			r1 = r2;
		}

		if (b >= -100 && b <= 90) {
			// unreachable
			if (b > 150) {
				r1.weldAt(b);
			}

			// ok, (b+c) in [-80, 80]
			if (b+c >= -40 && b+c <= 40) {
				r2.weldAt(2*(b+c));
			}

			// ok
			for (int k = 0; 2*k < 7; k++) {
				r1.weldAt(k + 5);
			}
		} else if (c < 180 && c > -42) {
			// ok
			r2.weldAt(c);
		}
	}

	public static void m2(int a, int b, int c) {
		Robot r = new Robot(-30, 30);
		int x = a + b + c;
		if ((x >= -30 && x <= -5) || (x >= 5 && x <= 30)) {
			// ok
			r.weldAt(x);
		}
	}

	public static void m3(int a, int b, int c) {
		Robot r1 = new Robot(-9000, 9000);

		int x = a + (a - c);
		x = x + b * 2;
		x = x + c - 50;
		if (x <= 5000 && x > -3999) {
			// ok
			r1.weldBetween(x - 40, x + 1000);
		}
	}
}
