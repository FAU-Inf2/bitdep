package analysis.essential;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Test;

import smt.Builder;
import smt.BVAst;
import synth.Library;
import synth.LibraryFunction;
import synth.Specification;

import static analysis.essential.ShapeFeasibilityChecker.*;
import static analysis.essential.ShapeFeasibilityChecker.Shape.*;



public class ShapeFeasibilityCheckerTest {

	private static Op[] makeOps(final Shape[] ... arguments) {
		final Op[] result = new Op[arguments.length];
		for (int i = 0; i < arguments.length; ++i) {
			result[i] = new Op(arguments[i]);
		}
		return result;
	}



	@Test
	public void testOptimizeBL_AA_D() {
		final Op[] ops = makeOps(
				new Shape[] { BLOCK, LINEAR },
				new Shape[] { ASCENDING, ASCENDING },
				new Shape[] { DESCENDING });

		final Shape[] varShapes = { BLOCK, BLOCK, BLOCK };

		final int[][] successors = { { -1, -1 }, { -1, -1 }, { -1 }, { 0 } };
		optimize(varShapes, ops, successors);

		assertEquals(2, 3 - evaluate(varShapes, ops, successors));
	}



	@Test
	public void testOptimizeSubsetSum() {
		// Subset sum problem with target 11

		final Op[] ops = makeOps(
				new Shape[] { ASCENDING, DESCENDING },
				new Shape[] { LINEAR, LINEAR, LINEAR, LINEAR, LINEAR, LINEAR },
				new Shape[] { LINEAR, LINEAR },
				new Shape[] { LINEAR, LINEAR, LINEAR, LINEAR, LINEAR, LINEAR, LINEAR, LINEAR, LINEAR },
				new Shape[] { LINEAR, LINEAR, LINEAR },
				new Shape[] { LINEAR, LINEAR, LINEAR, LINEAR });

		final Shape[] varShapes = {
				ASCENDING, ASCENDING, ASCENDING, ASCENDING, ASCENDING, ASCENDING, ASCENDING, ASCENDING,
				ASCENDING, ASCENDING, ASCENDING, DESCENDING, DESCENDING, DESCENDING, DESCENDING,
				DESCENDING, DESCENDING, DESCENDING, DESCENDING, DESCENDING, DESCENDING };

		final int[][] successors = { { -1, -1 }, { -1, -1, -1, -1, -1, -1 }, { -1, -1},
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1 }, { -1, -1, -1 }, { -1, -1, -1, -1 }, { 0 } };
		optimize(varShapes, ops, successors);

		assertEquals(21, 21 - evaluate(varShapes, ops, successors));
	}



	@Test
	public void testOptimizePruning() {
		final Op[] ops = makeOps(
				new Shape[] { LINEAR, ASCENDING },
				new Shape[] { BLOCK, BLOCK },
				new Shape[] { BLOCK, BLOCK, BLOCK });

		final Shape[] varShapes = { ASCENDING, BLOCK, BLOCK, BLOCK, BLOCK };

		final int[][] successors = { { -1, -1 }, { -1, -1 }, { -1, -1, -1 }, { 0 } };
		optimize(varShapes, ops, successors);

		assertEquals(varShapes.length, varShapes.length - evaluate(varShapes, ops, successors));
	}



	@Test
	public void testGetAscDescMerged1() {
		final Shape[] varShapes = { ASCENDING };
		final Op[] ops = makeOps(new Shape[] { ASCENDING, DESCENDING });

		final List<Op[]> result = Lists.newArrayList(getAscDescMerged(varShapes, ops));
		assertEquals(1, result.size());
		assertEquals(ops.length, result.get(0).length);
		assertArrayEquals(ops[0].arguments, result.get(0)[0].arguments);
	}



	@Test
	public void testGetAscDescMerged2() {
		final Shape[] varShapes = { BLOCK };
		final Op[] ops = makeOps(new Shape[] { ASCENDING, LINEAR });

		final List<Op[]> result = Lists.newArrayList(getAscDescMerged(varShapes, ops));
		assertEquals(1, result.size());
		assertEquals(ops.length, result.get(0).length);
		assertArrayEquals(ops[0].arguments, result.get(0)[0].arguments);
	}



	@Test
	public void testGetAscDescMerged3() {
		final Shape[] varShapes = { BLOCK };
		final Op[] ops = makeOps(new Shape[] { ASCENDING, DESCENDING });

		final List<Op[]> result = Lists.newArrayList(getAscDescMerged(varShapes, ops));
		assertEquals(2, result.size());
		assertEquals(ops.length, result.get(0).length);
		assertArrayEquals(ops[0].arguments, result.get(0)[0].arguments);
		assertEquals(ops.length, result.get(1).length);
		assertArrayEquals(new Shape[] { BLOCK }, result.get(1)[0].arguments);
	}



	@Test
	public void testOneCheck_SRem_Sub_Const2_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final BVAst specFun = Builder.mkSDiv(vars.get(0), vars.get(1));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				LibraryFunction.getSRem(4),
				LibraryFunction.getSub(4),
				LibraryFunction.getConst(4, 2))));
	}



	@Test
	public void testOneCheck_Ashr_Sub_Const2_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final BVAst specFun = Builder.mkSDiv(vars.get(0), vars.get(1));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				LibraryFunction.getAshr(4),
				LibraryFunction.getSub(4),
				LibraryFunction.getConst(4, 2))));
	}



	@Test
	public void testOneCheck_Add_Neg_Const1_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkNot(vars.get(0));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				LibraryFunction.getAdd(4),
				LibraryFunction.getNeg(4),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_Add_Neg_Const2_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkNot(vars.get(0));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				LibraryFunction.getAdd(4),
				LibraryFunction.getNeg(4),
				LibraryFunction.getConst(4, 2))));
	}



	@Test
	public void testOneCheck_Xor_Sub_Const1_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0),
				Builder.mkSub(vars.get(0), Builder.mkBVConst(4, 1)));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				LibraryFunction.getXor(4),
				LibraryFunction.getSub(4),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_Ashr_Ashr_Const1_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0),
				Builder.mkSub(vars.get(0), Builder.mkBVConst(4, 1)));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				LibraryFunction.getAshr(4),
				LibraryFunction.getAshr(4),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_Shl1_Shl1_Const1_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0), Builder.mkBVConst(4, 4));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				new LibraryFunction("shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(xs.get(0), Builder.mkBVConst(4, 1))),
				new LibraryFunction("shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(xs.get(0), Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_Shl1_Const1_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0), Builder.mkBVConst(4, 4));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				new LibraryFunction("shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(xs.get(0), Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_Lshr1_Lshr1_Const4_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0), Builder.mkBVConst(4, 1));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				new LibraryFunction("lshr1", Collections.singletonList(4), 4,
					xs -> Builder.mkLshr(xs.get(0), Builder.mkBVConst(4, 1))),
				new LibraryFunction("lshr1", Collections.singletonList(4), 4,
					xs -> Builder.mkLshr(xs.get(0), Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 4))));
	}



	@Test
	public void testOneCheck_Lshr1_Const4_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final BVAst specFun = Builder.mkXor(vars.get(0), Builder.mkBVConst(4, 1));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				new LibraryFunction("lshr1", Collections.singletonList(4), 4,
					xs -> Builder.mkLshr(xs.get(0), Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 4))));
	}



	@Test
	public void testOneCheck_Not_Neg_And_1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final BVAst specFun = Builder.mkAdd(vars.get(0),
				Builder.mkOr(Builder.mkNot(vars.get(0)), vars.get(1)));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertTrue(oneCheck(specApprox, Library.of(
				LibraryFunction.getNot(4),
				LibraryFunction.getNeg(4),
				LibraryFunction.getAnd(4))));
	}



	@Test
	public void testOneCheck_Add_Sub_Mul_SRem_Shl_Ashr_No1() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final BVAst specFun = Builder.mkAdd(vars.get(0),
				Builder.mkOr(Builder.mkNot(vars.get(0)), vars.get(1)));

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, vars);

		assertFalse(oneCheck(specApprox, Library.of(
				LibraryFunction.getAdd(4),
				LibraryFunction.getSub(4),
				LibraryFunction.getMul(4),
				LibraryFunction.getSRem(4),
				LibraryFunction.getShl(4),
				LibraryFunction.getAshr(4))));
	}



	@Test
	public void testOneCheck_Const4_1() {
		final BVAst specFun = Builder.mkBVConst(4, 4);

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, Collections.emptyList());

		assertTrue(oneCheck(specApprox, Library.of(LibraryFunction.getConst(4, 4))));
	}



	@Test
	public void testOneCheck_And1Shl1_And2Shl2_Const1_1() {
		final BVAst specFun = Builder.mkBVConst(4, 8);

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, Collections.emptyList());

		assertTrue(oneCheck(specApprox, Library.of(
				new LibraryFunction("and1shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 1)),
						Builder.mkBVConst(4, 1))),
				new LibraryFunction("and2shl2", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 2)),
						Builder.mkBVConst(4, 2))),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_And2Shl2_And1Shl1_Const1_1() {
		final BVAst specFun = Builder.mkBVConst(4, 8);

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, Collections.emptyList());

		assertTrue(oneCheck(specApprox, Library.of(
				new LibraryFunction("and2shl2", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 2)),
						Builder.mkBVConst(4, 2))),
				new LibraryFunction("and1shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 1)),
						Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testOneCheck_And1Shl1_And4Shl1_And2Shl1_Const1_1() {
		final BVAst specFun = Builder.mkBVConst(4, 8);

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= ZPolyUnderApproximation.create(specFun, Collections.emptyList());

		assertTrue(oneCheck(specApprox, Library.of(
				new LibraryFunction("and1shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 1)),
						Builder.mkBVConst(4, 1))),
				new LibraryFunction("and4shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 4)),
						Builder.mkBVConst(4, 1))),
				new LibraryFunction("and2shl1", Collections.singletonList(4), 4,
					xs -> Builder.mkShl(Builder.mkAnd(xs.get(0), Builder.mkBVConst(4, 2)),
						Builder.mkBVConst(4, 1))),
				LibraryFunction.getConst(4, 1))));
	}



	@Test
	public void testIsUnsatSDiv7_Asc() {
		final Specification spec = new Specification(
				Collections.singletonList(4),
				4,
				xs -> Builder.mkSDiv(Builder.mkBVConst(4, 7), xs.get(0)));

		final Library lib = Library.of(
				LibraryFunction.getAnd(4),
				LibraryFunction.getOr(4),
				LibraryFunction.getAdd(4),
				LibraryFunction.getSub(4));

		assertTrue("There should not be an Upper Bound for the Synthesis Problem", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatSDiv7_Desc() {
		// Attention: This test requires UA2!
		final Specification spec = new Specification(
				Collections.singletonList(4),
				4,
				xs -> Builder.mkSDiv(Builder.mkBVConst(4, 7), xs.get(0)));

		final Library lib = Library.of(
				LibraryFunction.getAnd(4),
				LibraryFunction.getOr(4),
				LibraryFunction.getSLtBV(4),
				new LibraryFunction("ashr3", Arrays.asList(4), 4,
					xs -> Builder.mkAshr(xs.get(0), Builder.mkBVConst(4, 3))));

		assertTrue("There should not be an Upper Bound for the Synthesis Problem", isUnsat(spec, lib));
	}




	@Test
	public void testIsUnsatNotUnsatConst() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4),
				4,
				xs -> Builder.mkSub(
					Builder.mkAshr(
						Builder.mkShl(xs.get(1), Builder.mkBVConst(4, 13)),
						Builder.mkBVConst(4, 14)),
					Builder.mkIte(
						Builder.mkEq(xs.get(0), Builder.mkURem(xs.get(1), Builder.mkBVConst(4, 0))),
						Builder.mkBVConst(4, 2), Builder.mkXor(Builder.mkBVConst(4, 10), xs.get(1)))));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkSub(
						Builder.mkAshr(
							Builder.mkShl(xs.get(1), Builder.mkBVConst(4, 13)),
							Builder.mkBVConst(4, 14)),
						Builder.mkIte(
							Builder.mkEq(xs.get(0), Builder.mkURem(xs.get(1), xs.get(2))),
							Builder.mkLshr(Builder.mkBVConst(4, 2), xs.get(2)),
							Builder.mkXor(Builder.mkBVConst(4, 10), xs.get(1))))),
				LibraryFunction.getConst(4, 0));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}




	@Test
	public void testIsUnsatNotUnsatConst2() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4),
				4,
				xs -> Builder.mkSRem(
					Builder.mkOr(
						Builder.mkNeg(xs.get(1)),
						Builder.mkSDiv(Builder.mkBVConst(4, 15), Builder.mkBVConst(4, 5))),
					xs.get(0)));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkSRem(
						Builder.mkOr(
							Builder.mkNeg(xs.get(2)),
							Builder.mkSDiv(Builder.mkBVConst(4, 15), xs.get(1))),
						xs.get(0))),
				LibraryFunction.getConst(4, 5));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}




	@Test
	public void testIsUnsatNotUnsatConst3() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4),
				4,
				xs -> Builder.mkAdd(
					Builder.mkSDiv(
						Builder.mkSub(Builder.mkBVConst(4, 3), Builder.mkBVConst(4, 1)),
						Builder.mkSub(Builder.mkBVConst(4, 0), Builder.mkBVConst(4, 2))),
					Builder.mkSDiv(Builder.mkOr(xs.get(0), xs.get(1)), Builder.mkBVConst(4, 1))));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkAdd(
						Builder.mkSDiv(
							Builder.mkSub(Builder.mkBVConst(4, 3), xs.get(0)),
							Builder.mkSub(Builder.mkBVConst(4, 0), Builder.mkBVConst(4, 2))),
						Builder.mkSDiv(Builder.mkOr(xs.get(1), xs.get(2)), xs.get(0)))),
				LibraryFunction.getConst(4, 1));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatNotUnsatConst4() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4),
				4,
				xs -> Builder.mkAshr(
					Builder.mkXor(
						Builder.mkAshr(Builder.mkBVConst(4, 14), Builder.mkBVConst(4, 5)),
						Builder.mkXor(xs.get(1), xs.get(0))),
					Builder.mkBVConst(4, 5)));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkAshr(
						Builder.mkXor(
							Builder.mkAshr(Builder.mkBVConst(4, 14), xs.get(0)),
							Builder.mkXor(xs.get(2), xs.get(1))),
						xs.get(0))),
				LibraryFunction.getConst(4, 5));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatNotUnsatConst5() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4),
				4,
				xs -> Builder.mkSub(
					Builder.mkSub(
						Builder.mkAdd(Builder.mkBVConst(4, 11), Builder.mkBVConst(4, 14)),
						Builder.mkAshr(xs.get(0), Builder.mkBVConst(4, 14))),
					xs.get(1)));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkSub(
						Builder.mkSub(
							Builder.mkAdd(Builder.mkBVConst(4, 11), xs.get(2)),
							Builder.mkAshr(xs.get(0), xs.get(2))),
						xs.get(1))),
				LibraryFunction.getConst(4, 14));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatNotUnsatConst6() {
		final Specification spec = new Specification(
				Arrays.asList(4, 4, 4),
				4,
				xs -> Builder.mkOr(
					Builder.mkLshr(
						Builder.mkUMod(Builder.mkBVConst(4, 14), Builder.mkBVConst(4, 12)),
						xs.get(0)),
					Builder.mkIte(
						Builder.mkEq(
							Builder.mkUDiv(xs.get(1), xs.get(2)),
							Builder.mkShl(Builder.mkBVConst(4, 12), xs.get(0))),
						Builder.mkSDiv(Builder.mkBVConst(4, 3), Builder.mkBVConst(4, 2)),
						Builder.mkBVConst(4, 12))));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4, 4), 4, xs ->
					Builder.mkOr(
						Builder.mkLshr(Builder.mkUMod(Builder.mkBVConst(4, 14), xs.get(0)), xs.get(1)),
						Builder.mkIte(
							Builder.mkEq(
								Builder.mkUDiv(xs.get(2), xs.get(3)),
								Builder.mkShl(xs.get(0), xs.get(1))),
							Builder.mkSDiv(Builder.mkBVConst(4,3), Builder.mkBVConst(4,2)),
							xs.get(0)))),
				LibraryFunction.getConst(4, 14));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatLowerBoundCounterexample() {
		final Specification spec = new Specification(Arrays.asList(4, 4), 4, xs ->
				Builder.mkSub(
					Builder.mkSub(
						Builder.mkLshr(xs.get(1), Builder.mkBVConst(4, 10)),
						Builder.mkIte(
							Builder.mkNe(xs.get(0), Builder.mkBVConst(4, 14)),
							xs.get(0),
							Builder.mkBVConst(4, 1))),
					Builder.mkOr(
						Builder.mkAshr(xs.get(1), Builder.mkBVConst(4, 5)),
						Builder.mkBVConst(4, 5))));

		final Library lib = Library.of(
				new LibraryFunction("f", Arrays.asList(4, 4, 4), 4, xs ->
					Builder.mkSub(
						Builder.mkSub(
							Builder.mkLshr(xs.get(2), Builder.mkBVConst(4, 10)),
							Builder.mkIte(
								Builder.mkNe(xs.get(0), Builder.mkBVConst(4, 14)),
								xs.get(0),
								Builder.mkBVConst(4, 1))),
						Builder.mkOr(Builder.mkAshr(xs.get(2), xs.get(1)), xs.get(1)))),
				LibraryFunction.getConst(4, 5));

		assertFalse("Const can be plugged in function", isUnsat(spec, lib));
	}



	@Test
	public void testIsUnsatNegShl() {
		final Specification spec = new Specification(Arrays.asList(32, 32), 32, xs ->
				Builder.mkNeg(Builder.mkShl(xs.get(0), xs.get(1))));

		final Library lib = Library.of(
				LibraryFunction.getShl(32),
				LibraryFunction.getNeg(32));

		assertFalse("(neg (shl x y)) is a model", isUnsat(spec, lib));
	}
}

