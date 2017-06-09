import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import ch.ethz.sae.Verifier;

@RunWith(Parameterized.class)
public class UnitTests {

	// The output that's printed to the console.
	PrintStream stdOut;

	@Before
	public void before() {
		stdOut = System.out;
	}

	@After
	public void after() {
		System.setOut(stdOut);
	}

	@Test
	public void test() {
		// The correct results of weldAt and weldBetween
		boolean expectedWeldAt = mExpWeldAt;
		boolean expectedWeldBetween = mExpWeldBet;
		String[] nameOfTest = { mNameOfClass };

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();

		outContent.reset();

		// run the Verifier
		System.setOut(new PrintStream(outContent));
		while (true) // sometimes there are weird errors (which are hopefully not our fault) that resolve upon retrying
			try {
				Verifier.main(nameOfTest.clone());
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}

		// split up the output
		String[] lines = outContent.toString().split("\n");

		stdOut.println(outContent);
		stdOut.println("Penultimate: " + lines[lines.length - 2]);

		boolean actualWeldAt = lines[lines.length - 2].equals(mNameOfClass + " WELD_AT_OK");
		boolean actualWeldBetween = lines[lines.length - 1].equals(mNameOfClass + " WELD_BETWEEN_OK");

		// error on unsoundness
		if (!expectedWeldAt && actualWeldAt || !expectedWeldBetween && actualWeldBetween)
			throw new RuntimeException("UNSOUND!");

		// assert precision
		assertEquals("\nWELD_AT FAILED:" + expectedWeldAt + " " + actualWeldAt, expectedWeldAt, actualWeldAt);
		assertEquals("\nWELD_BETWEEN FAILED:", expectedWeldBetween, actualWeldBetween);
	}

	@Parameter(0)
	public String mNameOfClass;
	@Parameter(1)
	public boolean mExpWeldAt;
	@Parameter(2)
	public boolean mExpWeldBet;

	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		final Object[][] data = new Object[][] {
				// TESTNAME, WELDAT, WELDBETWEEN
				{"TestAliasing",true, true}, 
				{"TestAssignmentStmtEdge",true, true}, 
				{"TestAssignmentStmtOverEdge",false, false}, 
				{"TestAssignmentStmtSimple",true, true}, 
				{"TestForLoop",true, true}, 
				{"TestForLoopFail",false, false}, 
				{"TestForLoopGotoStmt",true, true}, 
				{"TestForLoopWidening",false, true}, 
				{"TestFromMailinglist",true, true}, 
				{"TestIdentity",true, true}, 
				{"TestIdentityFail",false, false}, 
				{"TestIfStmtSimple",true, true}, 
				{"TestMultipleAssign",true, true}, 
				{"TestMultipleAssign2",false, false}, 
				{"TestMultipleAssign3",false, true}, 
				{"TestMultipleMethods",true, true}, 
				{"TestMultipleRobots",true, true}, 
				{"TestMultipleTestCasesFail",false, false}, 
				{"TestRenatoCrash",true, true}, 
				{"TestRenatoCrash2",true, true}, 
				{"Test_1",true, true}, 
				{"Test_2",true, true}, 
				{"Test_3",true, true}, 
				{"Test_4",true, true}, 
				{"Test_5",true, false}, 
				{"Test_5_LUKE",false, true}, 
				{"Test_6",true, true}, 
				{"Test_7",true, true}, 
				{"Test_ArgOverlap",true, true}, 
				{"Test_Between_F",true, false}, 
				{"Test_Constructor",false, false}, 
				{"Test_CrashJRE",true, true}, 
				{"Test_Definition_Stmts",true, true}, 
				{"Test_Eq",true, true}, 
				{"Test_ForLoop",false, true}, 
				{"Test_For_1",true, false}, 
				{"Test_Inequality",true, true}, 
				{"Test_Large",true, true}, 
				{"Test_Le_Ge",true, true}, 
				{"Test_Loops",true, true}, 
				{"Test_Lt_Gt",true, true}, 
				{"Test_Many_Bots",true, true}, 
				{"Test_MultipleRobots",true, true}, 
				{"Test_Neq",true, true}, 
				{"Test_Pointer_Galore",false, false}, 
				{"Test_Pointer_If",true, true}, 
				{"Test_Reassigning",false, true}, 
				{"Test_SimpleReassignment",true, true}, 
				{"Test_Strict_Interval",true, false}, 
				{"Test_Unreachable",true, true}, 
				{"Test_WeldBetween",true, true}, 
				{"Test_WhileAndBreak",true, true}, 
				{"Test_While_No_Widen_F",false, true}, 
				{"Test_While_No_Widen_T",true, true}, 
				{"Test_While_Widen_1",false, true}, 
				{"Test_While_Widen_2",false, true}, 
				{"Test_if_reassign",true, true}, 
				{"Test_ok_forif",false, false}, 
				{"Test_Generated_0_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_100_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_101_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_102_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_103_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_104_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_105_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_106_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_107_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_108_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_109_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_10_NOT_OK_NOT_OK_EQUAL",false, false}, 
				{"Test_Generated_110_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_111_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_112_OK_NOT_OK_SMALLER_EQUAL",true, false}, 
				{"Test_Generated_113_OK_NOT_OK_SMALLER_EQUAL",true, false}, 
				{"Test_Generated_114_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_115_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_116_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_117_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_118_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_119_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_11_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_120_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_121_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_122_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_123_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_12_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_13_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_14_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_15_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_16_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_17_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_18_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_19_OK_NOT_OK_NOT_EQUAL",true, false}, 
				{"Test_Generated_1_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_20_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_21_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_22_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_23_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_24_NOT_OK_NOT_OK_NOT_EQUAL",false, false}, 
				{"Test_Generated_25_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_26_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_27_OK_OK_NOT_EQUAL",true, true}, 
				{"Test_Generated_28_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_29_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_2_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_30_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_31_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_32_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_33_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_34_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_35_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_36_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_37_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_38_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_39_OK_NOT_OK_SMALLER",true, false}, 
				{"Test_Generated_3_NOT_OK_NOT_OK_EQUAL",false, false}, 
				{"Test_Generated_40_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_41_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_42_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_43_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_44_NOT_OK_NOT_OK_SMALLER",false, false}, 
				{"Test_Generated_45_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_46_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_47_OK_OK_SMALLER",true, true}, 
				{"Test_Generated_48_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_49_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_4_NOT_OK_NOT_OK_EQUAL",false, false}, 
				{"Test_Generated_50_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_51_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_52_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_53_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_54_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_55_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_56_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_57_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_58_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_59_OK_NOT_OK_BIGGER",true, false}, 
				{"Test_Generated_5_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_60_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_61_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_62_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_63_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_64_NOT_OK_NOT_OK_BIGGER",false, false}, 
				{"Test_Generated_65_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_66_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_67_OK_OK_BIGGER",true, true}, 
				{"Test_Generated_68_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_69_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_6_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_70_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_71_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_72_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_73_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_74_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_75_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_76_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_77_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_78_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_79_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_7_OK_OK_EQUAL",true, true}, 
				{"Test_Generated_80_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_81_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_82_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_83_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_84_OK_NOT_OK_BIGGER_EQUAL",true, false}, 
				{"Test_Generated_85_OK_NOT_OK_BIGGER_EQUAL",true, false}, 
				{"Test_Generated_86_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_87_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_88_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_89_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_8_OK_NOT_OK_EQUAL",true, false}, 
				{"Test_Generated_90_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_91_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_92_NOT_OK_NOT_OK_BIGGER_EQUAL",false, false}, 
				{"Test_Generated_93_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_94_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_95_OK_OK_BIGGER_EQUAL",true, true}, 
				{"Test_Generated_96_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_97_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_98_NOT_OK_NOT_OK_SMALLER_EQUAL",false, false}, 
				{"Test_Generated_99_OK_OK_SMALLER_EQUAL",true, true}, 
				{"Test_Generated_9_NOT_OK_NOT_OK_EQUAL",false, false}
		};
		return Arrays.asList(data);
	}
}
