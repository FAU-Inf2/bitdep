package synth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static smt.Builder.*;
import static synth.LibraryFunction.*;

import smt.BVAst;



public class TestData {

	public static List<TestCase> getFastSatTestCases() {
		return Arrays.asList(
				test2BytesSet(),
				testAbsolute(),
				testAndEquivalent(),
				testAshrGreater(),
				testAshrGreater2(),
				testBitReset(),
				testCondSetClearBits(),
				testDivisionBy2(),
				testFloorAverage(),
				testGreaterZero(),
				testHasZeroByte(),
				testHighestBit(),
				testMaskLeastBitTrail0(),
				testMaskRightmostOne(),
				testMaxGreaterOne(),
				testMaximum(),
				testRemAddGreater(),
				testTest2PNm1(),
				testXor3WithNor());
	}



	public static List<TestCase> getSlowSatTestCases() {
		return Arrays.asList(
				testByteSuffixSum(),
				testDetectAddOverflowHard(),
				testEqualNLZ(),
				testExpressAdd(),
				testIsolateRightMostOne(),
				testIsolateRightMostZero(),
				testKleeneXor6(),
				testMulAddLess(),
				testOr4With5Operations(),
				testParity(),
				testReverseBytes(),
				testSignExtend(),
				testSmallerNLZ(),
				testSubXorAshr(),
				testUnsignedLessEqual());
	}



	public static List<TestCase> getSatTestCases() {
		final List<TestCase> result = new ArrayList<>();
		result.addAll(getFastSatTestCases());
		result.addAll(getSlowSatTestCases());
		return result;
	}



	public static List<TestCase> getFastUnsatTestCases() {
		return Arrays.asList(
				testExpressMulWithAdds(),
				testExpressMulWithXors());
	}



	public static List<TestCase> getSlowUnsatTestCases() {
		return Arrays.asList(
				testExpressXor3WithAndOr(),
				testIsolateLeftMostOne(),
				testKleeneXor4(),
				testNegationFromAddition());
	}



	public static List<TestCase> getUnsatTestCases() {
		final List<TestCase> result = new ArrayList<>();
		result.addAll(getFastUnsatTestCases());
		result.addAll(getSlowUnsatTestCases());
		return result;
	}



	//
	// SAT TEST CASES
	//

	private static TestCase testGreaterZero() {
		return new TestCase("GreaterZero",
				new Specification(1, FunctionParser.parse("x -> (ite (sgt x 0:32) 1:32 0:32)")),
				Library.of(getSGtBV(32), getConst(32, 0)));
	}


	private static TestCase testAshrGreater() {
		return new TestCase("AshrGreater",
				new Specification(1, FunctionParser
					.parse("x -> (ite (sgt (add 1:32 (ashr x 1:32)) x) 1:32 0:32)")),
				Library.of(getSGtBV(32), getConst(32, 1)));
	}


	private static TestCase testHighestBit() {
		return new TestCase("HighestBit",
				new Specification(1, FunctionParser.bitReduceR(
						FunctionParser.parse("x a i -> (ite (neq (and x (shl 1:32 i)) 0:32) (shl 1:32 i) a)"),
						mkBVConst(32, 0))),
				Library.of(getSub(), getAnd(), getConst(32, 0), getConst(32, 1)));
	}


	private static TestCase testAbsolute() {
		return new TestCase("Absolute",
				new Specification(1, FunctionParser.parse("x -> (ite (slt x 0:32) (sub 0:32 x) x)")),
				Library.of(getAshrJava(), getXor(), getSub(), getConst(32, 31)));
	}


	private static TestCase testMaximum() {
		return new TestCase("Maximum",
				new Specification(2, FunctionParser.parse("x y -> (ite (sgt x y) x y)")),
				Library.of(getXor(), getXor(), getAnd(), getSub(), getSLtBV(32), getConst(32, 0)));
	}


	private static TestCase testHasZeroByte() {
		return new TestCase("HasZeroByte",
				new Specification(1, FunctionParser.parse("x -> (ite "
						+ "(neq (or (or (add (and x 0x7F7F7F7F:32) 0x7F7F7F7F:32) x) 0x7F7F7F7F:32) 0:32) "
						+ "1:32 0:32)")),
				Library.of(getAnd(), getAdd(), getOr(), getOr(), getNeqBV(32), getConst(32, 0),
						getConst(32, 0x7F7F7F7F)));
	}


	private static TestCase testAshrGreater2() {
		return new TestCase("AshrGreater2",
				new Specification(2, FunctionParser.parse("x y -> (ite (sgt (add x y) "
						+ "(ashr (add (add (add 1:32 x) (ashr (add (add 1:32 x) y) 1:32)) y) 1:32)) "
						+ "1:32 0:32)")),
				Library.of(getSGtBV(32), getAdd(), getConst(32, 3)));
	}


	private static TestCase testSubXorAshr() {
		return new TestCase("SubXorAshr",
				new Specification(1, FunctionParser
					.parse("x -> (sub (xor x (ashr x 0x1F:32)) (ashr x 0x1F:32))")),
				Library.of(getSub(), getAdd(), getAshrJava(), getLshrJava(), getXor(), getAnd(),
						getOr(), getConst(32, 1), getConst(32, 0), getConst(32, 0x1F), getConst(32, -1)));
	}


	private static TestCase testRemAddGreater() {
		return new TestCase("ModAddGreater",
				new Specification(2, FunctionParser
					.parse("x y -> (ite (sgt (srem (add x 1:32) 4:32) y) 1:32 0:32)")),
				Library.of(getSGtBV(32), getAdd(), getSRem(), getConst(32, 1), getConst(32, 4)));
	}


	private static TestCase testMaxGreaterOne() {
		return new TestCase("MaxGreaterOne",
				new Specification(1, FunctionParser
					.parse("x -> (ite (sgt (ite (sgt x 0:32) x 0:32) 1:32) 1:32 0:32)")),
				Library.of(getSGtBV(32), getAdd(), getAnd(), getConst(32, 0), getConst(32, 1)));
	}


	private static TestCase testMulAddLess() {
		return new TestCase("MulAddLess",
				new Specification(3, FunctionParser
					.parse("x y z -> (ite (slt (mul (add x y) 2:32) (add (mul y 2:32) z)) 1:32 0:32)")),
				Library.of(getSLtBV(32), getMul(), getMul(), getAdd(), getAdd(), getConst(32, 2)));
	}


	private static TestCase testParity() {
		return new TestCase("Parity",
				new Specification(1, FunctionParser.concat(
					FunctionParser.parse("x -> (and x 1:32)"),
					FunctionParser.bitReduceR(
						FunctionParser.parse("x a i -> (xor a (and (ashr x i) 1:32))"),
						mkBVConst(32, 0)))),
				Library.of(
						getXor(), getXor(),
						new LibraryFunction("ashr1", 1, FunctionParser.parse("x -> (ashr x 1:32)")),
						new LibraryFunction("ashr2", 1, FunctionParser.parse("x -> (ashr x 2:32)")),
						new LibraryFunction("ashr28", 1, FunctionParser.parse("x -> (ashr x 28:32)")),
						new LibraryFunction("and1", 1, FunctionParser.parse("x -> (and x 1:32)")),
						new LibraryFunction("and0x11111111", 1,
							FunctionParser.parse("x -> (and x 0x11111111:32)")),
						new LibraryFunction("mul0x11111111", 1,
							FunctionParser.parse("x -> (mul x 0x11111111:32)"))));
	}


	private static TestCase testReverseBytes() {
		return new TestCase("Reverse Bytes",
				new Specification(1, FunctionParser.parse("x -> (or (lshr x 24:32) (or (and (lshr x 8:32) "
					+ "0xFF00:32) (or (and (shl x 8:32) 0xFF0000:32) (shl x 24:32))))")),
				Library.of(getAnd(), getAnd(),
					new LibraryFunction("or4", 4, FunctionParser.parse("x y z w -> (or (or x y) (or z w))")),
					getShlJava(), getShlJava(), getLshrJava(), getLshrJava(), getConst(32, 8),
					getConst(32, 24), getConst(32, 0xFF00)));
	}


	// From SyGuS problem "hd-02-d1" (should have at least two solutions)
	private static TestCase testTest2PNm1() {
		return new TestCase("Test 2^n - 1",
				new Specification(1, FunctionParser.parse("x -> (and x (add x 1:32))")),
				Library.of(getAnd(), getSub(), getOr(), getAdd(), getXor(), getConst(32, 0),
					getConst(32, -1), getConst(32, 1)));
	}


	// From SyGuS problem "hd-04-d1"
	private static TestCase testMaskLeastBitTrail0() {
		return new TestCase("Mask up to Least Bit",
				new Specification(1, FunctionParser.bitReduceR(FunctionParser.parse(
						"x a i -> (ite (neq (and x (shl 1:32 i)) 0:32) (sub (shl 1:32 i) 1:32) a)"),
						mkBVConst(32, -1))),
				Library.of(getSub(), getXor(), getNeg(), getAdd(), getOr(), getAnd(), getConst(32, 0),
					getConst(32, 1), getConst(32, -1)));
	}


	// From SyGuS problem "hd-10-d1"
	private static TestCase testEqualNLZ() {
		return new TestCase("Equal NLZ",
				new Specification(2, FunctionParser.concat2(
						FunctionParser.parse("nlzx nlzy -> (ite (eq nlzx nlzy) 1:32 0:32)"), nlz(), nlz())),
				Library.of(getULeBV(32), getSLeBV(32), getAnd(), getXor(), getOr(), getAdd(), getSub(),
						getNot(), getNeg()));
	}


	// From SyGuS problem "hd-11-d1"
	private static TestCase testSmallerNLZ() {
		return new TestCase("Smaller NLZ",
				new Specification(2, FunctionParser.concat2(
						FunctionParser.parse("nlzx nlzy -> (ite (ult nlzx nlzy) 1:32 0:32)"), nlz(), nlz())),
				Library.of(getUGtBV(32), getUGeBV(32), getAnd(), getXor(), getOr(), getAdd(), getSub(),
						getNot(), getNeg()));
	}


	// From SyGuS problem "hd-14-d0"
	private static TestCase testFloorAverage() {
		return new TestCase("Floor Average",
				new Specification(2, FunctionParser
					.parse("x y -> (extract 0 31 (udiv (add (concat 0:32 x) (concat 0:32 y)) 2:64))")),
				Library.of(getLshr(), getXor(), getAdd(), getAnd(), getConst(32, 1)));
	}

	// From Bit Twiddling Hacks, "Conditionally set of clear bits without branching"
	private static TestCase testCondSetClearBits() {
		return new TestCase("Cond. Set/Clear Bits",
				new Specification(3, FunctionParser
					.parse("f m w -> (ite (eq f 0:32) (and w (not m)) (or w m))")),
				Library.of(
						new LibraryFunction("normbool", 1,
							FunctionParser.parse("x -> (ite (eq x 0:32) 0:32 1:32)")),
						getNeg(), getNot(), getAnd(), getAnd(), getOr()));
	}


	// From AMD Software Optimization Guide for the AMD64 Processors, Page 162
	private static TestCase testDivisionBy2() {
		return new TestCase("Division by 2",
				new Specification(1, FunctionParser.parse("x -> (sdiv x 2:32)")),
				Library.of(
						new LibraryFunction("sbb", 3, FunctionParser.parse("x y z -> (sub x (add y z))")),
						getSLeBV(32), getAshr(), getConst(32, 1), getConst(32, -1)));
	}


	// Adapted from SyGuS problem "btr-am-base-solution-1"
	private static TestCase testBitReset() {
		return new TestCase("Bit Reset",
				new Specification(2, FunctionParser
					.parse("x y -> (and x (not (shl 1:32 (and y 0x1f:32))))")),
				Library.of(getAnd(), getAdd(), getShlJava(), getConst(32, -1)));
	}


	// Test size restriction
	private static TestCase testMaskRightmostOne() {
		return new TestCase("Mask Rightmost One",
				new Specification(1, 3, FunctionParser.parse("x -> (or (not x) (sub x 1:32))")),
				Library.of(getNot(), getNeg(), getAnd(), getXor(), getOr(), getAdd(), getSub(),
						getConst(32, 1)));
	}


	private static TestCase testXor3WithNor() {
		return new TestCase("Xor3 With Nor",
				new Specification(3, FunctionParser.parse("x y z -> (xor (xor x y) z)")),
				Library.of(Collections.nCopies(8,
					new LibraryFunction("nor", 2, FunctionParser.parse("x y -> (not (or x y))")))));
	}


	// Inspired by the Totalizer CNF encodings
	private static TestCase test2BytesSet() {
		return new TestCase("2 Bytes Set",
				new Specification(1, FunctionParser.parse("x -> (ite (eq "
					+ "(add (add (add (ite (neq (and x 0xFF:32) 0:32) 1:32 0:32) "
					+ "(ite (neq (and x 0xFF00:32) 0:32) 1:32 0:32)) "
					+ "(ite (neq (and x 0xFF0000:32) 0:32) 1:32 0:32)) "
					+ "(ite (neq (and x 0xFF000000:32) 0:32) 1:32 0:32)) 2:32) 1:32 0:32)")),
				Library.of(
						new LibraryFunction("lb1", 1, FunctionParser.parse(
								"x -> (or (ite (neq (and x 0xFF:32) 0:32) 1:32 0:32) "
									+ "(ite (neq (and x 0xFF00:32) 0:32) 1:32 0:32))")),
						new LibraryFunction("lb2", 1, FunctionParser.parse(
								"x -> (and (ite (neq (and x 0xFF:32) 0:32) 1:32 0:32) "
									+ "(ite (neq (and x 0xFF00:32) 0:32) 1:32 0:32))")),
						new LibraryFunction("hb1", 1, FunctionParser.parse(
								"x -> (or (ite (neq (and x 0xFF0000:32) 0:32) 1:32 0:32) "
									+ "(ite (neq (and x 0xFF000000:32) 0:32) 1:32 0:32))")),
						new LibraryFunction("hb2", 1, FunctionParser.parse(
								"x -> (and (ite (neq (and x 0xFF0000:32) 0:32) 1:32 0:32) "
									+ "(ite (neq (and x 0xFF000000:32) 0:32) 1:32 0:32))")),
						getAnd(), getXor(), getXor()));
	}


	// Suffix Sum of bytes in 32 bit integer
	private static TestCase testByteSuffixSum() {
		return new TestCase("Byte Suffix Sum",
				new Specification(1, FunctionParser.parse("x -> (or (or (or "
						+ "(and x 0xFF000000:32) (and (add x (ashr (and x 0xFF000000:32) 8:32)) 0xFF0000:32)) "
						+ "(and (add x (ashr (and (add x (ashr (and x 0xFF000000:32) 8:32)) 0xFF0000:32) "
						+ "8:32)) 0xFF00:32)) "
						+ "(and (add x (ashr (and (add x (ashr (and (add x (ashr (and x 0xFF000000:32) "
						+ "8:32)) 0xFF0000:32) 8:32)) 0xFF00:32) 8:32)) 0xFF:32))")),
				Library.of(
						new LibraryFunction("lshr8", 1, FunctionParser.parse("x -> (lshr x 8:32)")),
						new LibraryFunction("lshr16", 1, FunctionParser.parse("x -> (lshr x 16:32)")),
						new LibraryFunction("lshr24", 1, FunctionParser.parse("x -> (lshr x 24:32)")),
						new LibraryFunction("shl8", 1, FunctionParser.parse("x -> (shl x 8:32)")),
						new LibraryFunction("shl8", 1, FunctionParser.parse("x -> (shl x 8:32)")),
						new LibraryFunction("shl8", 1, FunctionParser.parse("x -> (shl x 8:32)")),
						new LibraryFunction("addb", 2, FunctionParser.parse("x y -> (and (add x y) 255:32)")),
						new LibraryFunction("addb", 2, FunctionParser.parse("x y -> (and (add x y) 255:32)")),
						new LibraryFunction("addb", 2, FunctionParser.parse("x y -> (and (add x y) 255:32)")),
						getOr(), getOr(), getOr()));
	}


	// Sign Extension
	private static TestCase testSignExtend() {
		return new TestCase("Sign Extend",
				new Specification(2, FunctionParser.parse("x b -> "
						+ "(ite (eq (extract 31 31 (shl (and x (sub (shl 1:32 (and 0x1F:32 b)) 1:32)) "
						+ "(and 0x1F:32 (sub 32:32 b)))) 1:1) (or x (shl -1:32 (and 0x1F:32 b))) "
						+ "(and x (sub (shl 1:32 (and 0x1F:32 b)) 1:32)))")),
				Library.of(
						new LibraryFunction("sub1", 1, FunctionParser.parse("x -> (sub x 1:32)")),
						new LibraryFunction("sub1", 1, FunctionParser.parse("x -> (sub x 1:32)")),
						new LibraryFunction("1shl", 1, FunctionParser.parse("x -> (shl 1:32 (and 31:32 x))")),
						new LibraryFunction("1shl", 1, FunctionParser.parse("x -> (shl 1:32 (and 31:32 x))")),
						getAnd(), getXor(), getSub()));
	}


	// From SyGuS problem "hd-03-d5"
	private static TestCase testIsolateRightMostOne() {
		return new TestCase("Isolate rightmost 1",
				new Specification(1, 2, FunctionParser.bitReduceR(
						FunctionParser.parse("x a i -> (ite (neq (and x (shl 1:32 i)) 0:32) (shl 1:32 i) a)"),
						mkBVConst(32, 0))),
				Library.of(getNot(), getAnd(), getXor(), getOr(), getNeg(), getAdd(), getMul(),
						getUDiv(), getURem(), getLshr(), getAshr(), getShl(), getSDiv(), getSRem(),
						getSub(), getConst(32, 1), getConst(32, 0), getConst(32, -1)));
	}


	// From SyGuS problem "hd-07-d5"
	private static TestCase testIsolateRightMostZero() {
		return new TestCase("Isolate rightmost 0",
				new Specification(1, 3, FunctionParser.bitReduceR(
						FunctionParser.parse("x a i -> (ite (eq (and x (shl 1:32 i)) 0:32) (shl 1:32 i) a)"),
						mkBVConst(32, 0))),
				Library.of(getNot(), getAnd(), getXor(), getOr(), getNeg(), getAdd(), getMul(),
						getUDiv(), getURem(), getLshr(), getAshr(), getShl(), getSDiv(), getSRem(),
						getSub(), getConst(32, 1), getConst(32, 0), getConst(32, -1)));
	}



	// From Hacker's Delight, page 23
	private static TestCase testUnsignedLessEqual() {
		return new TestCase("Unsigned Less Equal",
				new Specification(2, FunctionParser.parse("x y -> (ite (ule x y) 1:32 0:32)")),
				Library.of(getNot(), getNot(), getOr(), getOr(), getAnd(), getXor(), getSub(),
						new LibraryFunction("lshr31", 1, FunctionParser.parse("x -> (lshr x 31:32)"))));
	}



	// Inspired by Hacker's Delight, pages 51-52
	private static TestCase testOr4With5Operations() {
		return new TestCase("Or4 with 5 Operations",
				new Specification(4, 5, FunctionParser.parse("x y z w -> (or (or x y) (or z w))")),
				Library.of(getOr(), getOr(), getAnd(), getAnd(), getAnd(), getAnd(), getXor(),
						getXor(), getXor(), getXor(), getNot(), getNot(), getNot(), getNot()));
	}



	// From Hacker's Delight, page 16; has several solutions
	private static TestCase testExpressAdd() {
		return new TestCase("Express Add",
				new Specification(2, 5, FunctionParser.parse("x y -> (add x y)")),
				Library.of(getNot(), getShl(), getOr(), getXor(), getSub(), getAshr(), getMul(),
						getConst(32, 1), getConst(32, 0)));
	}



	// From Hacker's Delight, page 31
	private static TestCase testDetectAddOverflowHard() {
		return new TestCase("Detect Add Overflow (Hard)",
				new Specification(2, FunctionParser.parse(
						"x y -> (ite (ugt (add (concat 0:32 x) (concat 0:32 y)) 0xFFFFFFFF:64) 1:32 0:32)")),
				Library.of(getAnd(), getAnd(), getOr(), getOr(), getNot(), getAdd(),
						new LibraryFunction("lshr31", 1, FunctionParser.parse("x -> (lshr x 31:32)"))));
	}



	// XOR in Kleene's ternary logic (0: F, 1: T, 2: U)
	private static TestCase testKleeneXor6() {
		// sat with 6 operations
		final Specification spec = new Specification(2, 6, FunctionParser.parse(
				"x y -> (ite (or (eq x 2:32) (eq y 2:32)) 2:32 "
					+ "(ite (or (and (eq x 0:32) (eq y 1:32)) (and (eq x 1:32) (eq y 0:32))) 1:32 0:32))"));
		spec.addPrecondition(FunctionParser.parseBool("x y -> (and (ult x 3:32) (ult y 3:32))"));
		return new TestCase("Kleene Xor (6)", spec,
				Library.of(getXor(), getAnd(), getOr(), getShl(), getLshr(), getAdd(), getConst(32, 0),
						getConst(32, 1), getConst(32, 2), getConst(32, 3)));
	}



	private static TestCase testAndEquivalent() {
		return new TestCase("And-Equivalent",
				new Specification(1, FunctionParser.parse("x -> (add (or x -6:32) 6:32)")),
				Library.of(getArbitraryConst(32), getAdd(32), getMul(32), getAnd(32)));
	}


//
// UNSAT TEST CASES
//

	private static TestCase testExpressMulWithAdds() {
		return new TestCase("ExpressMulWithAdds",
				new Specification(2, FunctionParser.parse("x y -> (ite (sgt (mul x y) x) 1:32 0:32)")),
				Library.of(getSGtBV(32), getAdd(), getAdd(), getConst(32, 0)));
	}



	private static TestCase testExpressMulWithXors() {
		return new TestCase("ExpressMulWithXors",
				new Specification(1, FunctionParser
					.parse("x -> (ite (sgt (mul (mul x x) x) x) 1:32 0:32)")),
				Library.of(getSGtBV(32), getXor(), getXor(), getXor(), getConst(32, 1)));
	}



	private static TestCase testExpressXor3WithAndOr() {
		return new TestCase("Express Xor3 with And/Or",
				new Specification(3, FunctionParser.parse("x y z -> (xor (xor x y) z)")),
				Library.of(getAnd(), getAnd(), getAnd(), getAnd(), getOr(), getOr(), getOr(), getOr()));
	}



	// Adapted from testIsolateRightMostOne; unsat due to the right-to-left
	// computability test, see Hacker's Delight, page 13-14
	private static TestCase testIsolateLeftMostOne() {
		return new TestCase("Isolate leftmost 1",
				new Specification(1, FunctionParser.bitReduceL(
						FunctionParser.parse("x a i -> (ite (neq (and x (shl 1:32 i)) 0:32) (shl 1:32 i) a)"),
						mkBVConst(32, 0))),
				Library.of(getNot(), getAnd(), getXor(), getOr(), getMul(), getAdd(),
						getConst(32, 1), getConst(32, 0), getConst(32, -1)));
	}



	private static TestCase testNegationFromAddition() {
		return new TestCase("Neg from Add",
				new Specification(1, FunctionParser.parse("x -> (neg x)")),
				Library.of(getAdd(), getAdd(), getAdd(), getAdd(), getAdd(), getConst(32, 1)));
	}



	// XOR in Kleene's ternary logic (0: F, 1: T, 2: U)
	private static TestCase testKleeneXor4() {
		// unsat with 4 operations
		final Specification spec = new Specification(2, 4, FunctionParser.parse(
				"x y -> (ite (or (eq x 2:32) (eq y 2:32)) 2:32 "
					+ "(ite (or (and (eq x 0:32) (eq y 1:32)) (and (eq x 1:32) (eq y 0:32))) 1:32 0:32))"));
		spec.addPrecondition(FunctionParser.parseBool("x y -> (and (ult x 3:32) (ult y 3:32))"));
		return new TestCase("Kleene Xor (4)", spec,
				Library.of(getXor(), getAnd(), getOr(), getShl(), getLshr(), getAdd(), getConst(32, 0),
						getConst(32, 1), getConst(32, 2), getConst(32, 3)));
	}



	//
	// HELPERS
	//

	private static Function<List<BVAst>, BVAst> nlz() {
		return FunctionParser.bitReduceL(FunctionParser.parse(
				"x a i -> (ite (neq (and x (shl 1:32 i)) 0:32) (add a 1:32) 0:32)"), mkBVConst(32, 0));
	}
}

