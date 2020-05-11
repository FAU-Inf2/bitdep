package analysis.essential;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Test;

import smt.Builder;
import smt.BVAst;



public class ApproximationTest {

	@Test
	public void testUA1Not() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNot(vars.get(0)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testUA1Neg() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNeg(vars.get(0)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testUA1And() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAnd(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Collections.emptySet(), bitSet);
		}
	}



	@Test
	public void testUA1Or() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkOr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA1Xor() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkXor(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA1Add() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAdd(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA1Sub() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSub(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, Sets.union(Collections.singleton(i), rolling), bitSet);
		}
	}



	@Test
	public void testUA1Mul() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkMul(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Collections.emptySet(), bitSet);
		}
	}



	@Test
	public void testUA1UDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUDiv(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(4, 5, 6, 7), bitSet);
		}
	}



	@Test
	public void testUA1URem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkURem(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Collections.singleton(i), bitSet);
		}
	}



	@Test
	public void testUA1SDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSDiv(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> expected = Sets.newHashSet(4, 5, 6, 7);

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, expected, bitSet);

			expected.add(3);
		}
	}



	@Test
	public void testUA1SRem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSRem(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Collections.singleton(i), bitSet);
		}
	}



	@Test
	public void testUA1Shl() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkShl(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testUA1Lshr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkLshr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testUA1Ashr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAshr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testUA1Eq() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkEq(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1Ne() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1ULt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkULt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1ULe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkULe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1UGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUGt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1UGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUGe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1SLt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSLt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(3, 4, 5, 6), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1SLe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSLe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1SGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSGt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1SGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSGe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(3, 4, 5, 6), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA1Ite() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(1, "c"),
				Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(
						Builder.mkIte(
							Builder.mkEq(vars.get(0), Builder.mkBVConst(1, 1)),
							vars.get(1),
							vars.get(2)),
						vars);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Collections.singleton(i + 5), bitSet);
		}
	}



	@Test
	public void testUA2Not() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNot(vars.get(0)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testUA2Neg() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNeg(vars.get(0)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testUA2And() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAnd(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA2Or() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkOr(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA2Xor() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkXor(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testUA2Add() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAdd(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		Set<Integer> previous = Collections.emptySet();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			final Set<Integer> current = Sets.newHashSet(i, i + 4);
			assertEquals("Mismatch on bit " + i, Sets.union(previous, current), bitSet);

			previous = current;
		}
	}



	@Test
	public void testUA2Sub() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSub(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i);
			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testUA2Mul() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkMul(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i);
			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testUA2UDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUDiv(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, rolling, bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testUA2URem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkURem(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, Sets.union(Collections.singleton(i), rolling), bitSet);
		}
	}



	@Test
	public void testUA2SDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSDiv(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, rolling, bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testUA2SRem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSRem(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, Sets.union(Collections.singleton(i), rolling), bitSet);
		}
	}



	@Test
	public void testUA2Shl() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkShl(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			rolling.add(i);

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);
		}
	}



	@Test
	public void testUA2Lshr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkLshr(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3);

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testUA2Ashr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkAshr(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3);

		for (int i = 0; i < underApprox.bitWidth() - 1; ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);

			rolling.remove(i);
		}

		assertEquals("Mismatch on bit 3", Collections.singleton(3), Sets.newHashSet(underApprox.get(3)));
	}



	@Test
	public void testUA2Eq() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkEq(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2Ne() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkNe(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2ULt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkULt(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2ULe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkULe(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2UGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUGt(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2UGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkUGe(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2SLt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSLt(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2SLe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSLe(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2SGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSGt(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2SGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(Builder.mkSGe(vars.get(0), vars.get(1)), vars, 2);

		assertEquals("Bit width mismatch", 1, underApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(underApprox.get(0)));
	}



	@Test
	public void testUA2Ite() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(1, "c"),
				Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyUnderApproximation> underApprox
				= ZPolyUnderApproximation.create(
						Builder.mkIte(
							Builder.mkEq(vars.get(0), Builder.mkBVConst(1, 1)),
							vars.get(1),
							vars.get(2)),
						vars, 2);

		assertEquals("Bit width mismatch", 4, underApprox.bitWidth());

		for (int i = 0; i < underApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(underApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(0, i + 1, i + 5), bitSet);
		}
	}



	@Test
	public void testOANot() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkNot(vars.get(0)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i), bitSet);
		}
	}



	@Test
	public void testOANeg() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkNeg(vars.get(0)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			rolling.add(i);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testOAAnd() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkAnd(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testOAOr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkOr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testOAXor() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkXor(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(i, i + 4), bitSet);
		}
	}



	@Test
	public void testOAAdd() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkAdd(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			rolling.add(i);
			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testOASub() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSub(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			rolling.add(i);
			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testOAMul() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkMul(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			rolling.add(i);
			rolling.add(i + 4);

			assertEquals("Mismatch on bit " + i, rolling, bitSet);
		}
	}



	@Test
	public void testOAUDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkUDiv(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, rolling, bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testOAURem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkURem(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> expected = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, expected, bitSet);
		}
	}



	@Test
	public void testOASDiv() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSDiv(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> expected = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, expected, bitSet);
		}
	}



	@Test
	public void testOASRem() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSRem(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> expected = Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, expected, bitSet);
		}
	}



	@Test
	public void testOAShl() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkShl(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = new HashSet<>();

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			rolling.add(i);

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);
		}
	}



	@Test
	public void testOALshr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkLshr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testOAAshr() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkAshr(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		final Set<Integer> baseSet = Sets.newHashSet(4, 5, 6, 7);
		final Set<Integer> rolling = Sets.newHashSet(0, 1, 2, 3);

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.union(baseSet, rolling), bitSet);

			rolling.remove(i);
		}
	}



	@Test
	public void testOAEq() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkEq(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOANe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkNe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOAULt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkULt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOAULe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkULe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOAUGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkUGt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOAUGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkUGe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOASLt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSLt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOASLe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSLe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOASGt() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSGt(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOASGe() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(Builder.mkSGe(vars.get(0), vars.get(1)), vars);

		assertEquals("Bit width mismatch", 1, overApprox.bitWidth());

		assertEquals(Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7), Sets.newHashSet(overApprox.get(0)));
	}



	@Test
	public void testOAIte() {
		final List<BVAst> vars = Arrays.asList(Builder.mkBVVar(1, "c"),
				Builder.mkBVVar(4, "x"), Builder.mkBVVar(4, "y"));

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(
						Builder.mkIte(
							Builder.mkEq(vars.get(0), Builder.mkBVConst(1, 1)),
							vars.get(1),
							vars.get(2)),
						vars);

		assertEquals("Bit width mismatch", 4, overApprox.bitWidth());

		for (int i = 0; i < overApprox.bitWidth(); ++i) {
			final Set<Integer> bitSet = Sets.newHashSet(overApprox.get(i));

			assertEquals("Mismatch on bit " + i, Sets.newHashSet(0, i + 1, i + 5), bitSet);
		}
	}
}

