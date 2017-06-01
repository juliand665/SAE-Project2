import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import ch.ethz.sae.Verifier;

@RunWith(Parameterized.class)
public class UnitTests {

	@Parameter(0)
	public String mNameOfClass;
	@Parameter(1)
	public boolean mExpWeldAt;
	@Parameter(2)
	public boolean mExpWeldBet;

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { 
				// TESTNAME, WELDAT, WELDBETWEEN
				{"Test_1", true, true},
				{"Test_2", true, true},
				{"Test_3", false, true},
				{"Test_WhileAndBreak", false, true}
		};
		return Arrays.asList(data);
	}

	@Before
	public void setup() {
		System.setOut(new PrintStream(outContent));
	}

	// The output that's printed to the console.
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	@Test
	public void test() {
		// The correct results of weldAt and weldBetween
		boolean expectedWeldAt = mExpWeldAt;
		boolean expectedWeldBetween = mExpWeldBet;
		String[] nameOfTest = { mNameOfClass };

		// run the Verifier

		Verifier.main(nameOfTest);

		// split up the output
		String[] lines = outContent.toString().split("\n");

		boolean actualWeldAt = false;
		if (lines[lines.length - 2].equals(mNameOfClass + " WELD_AT_OK"))
			actualWeldAt = true;

		boolean actualWeldBetween = false;
		if (lines[lines.length - 1].equals(mNameOfClass + " WELD_BETWEEN_OK"))
			actualWeldBetween = true;

		assertEquals("Weld_At assertion failed!", expectedWeldAt, actualWeldAt);
		assertEquals("Weld_Between assertion failed!", expectedWeldBetween,
				actualWeldBetween);
	}

}
