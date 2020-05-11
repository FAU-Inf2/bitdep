package analysis.essential;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviders;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import smt.BoolAst;
import smt.Builder;
import smt.BVAst;



@RunWith(DataProviderRunner.class)
public class ApproximationBuilderTest {

	private static class PseudoApproximation implements Approximation {
		private boolean value;

		PseudoApproximation(final boolean value) {
			this.value = value;
		}

		@Override
		public Approximation and(final Approximation other) {
			final PseudoApproximation otherPseudo = (PseudoApproximation) other;
			return new PseudoApproximation(this.value & otherPseudo.value);
		}

		@Override
		public Approximation andM(final Approximation other) {
			final PseudoApproximation otherPseudo = (PseudoApproximation) other;
			this.value &= otherPseudo.value;;
			return this;
		}

		@Override
		public Approximation xor(final Approximation other) {
			final PseudoApproximation otherPseudo = (PseudoApproximation) other;
			return new PseudoApproximation(this.value != otherPseudo.value);
		}

		@Override
		public Approximation xorM(final Approximation other) {
			final PseudoApproximation otherPseudo = (PseudoApproximation) other;
			this.value = this.value != otherPseudo.value;
			return this;
		}

		@Override
		public Approximation not() {
			return new PseudoApproximation(!this.value);
		}

		@Override
		public Approximation notM() {
			this.value = !this.value;
			return this;
		}

		@Override
		public Approximation join(final Approximation other) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isConstant(final int value) {
			return (value == 0) == !this.value;
		}

		static PseudoApproximation ofBit(final int bit) {
			return new PseudoApproximation(bit != 0);
		}

		static MultiBitsApproximation<PseudoApproximation> of(final int value) {
			final List<PseudoApproximation> result = new ArrayList<>();
			for (int i = 0, v = value; i < 32; ++i, v >>>= 1) {
				result.add(ofBit(v & 1));
			}
			return new MultiBitsApproximation<>(result);
		}

		static int toInt(final MultiBitsApproximation<PseudoApproximation> approximation) {
			int v = 0;
			for (int i = 31; i >= 0; --i) {
				v <<= 1;
				v |= approximation.get(i).value ? 1 : 0;
			}
			return v;
		}

		static boolean toBool(final MultiBitsApproximation<PseudoApproximation> approximation) {
			return approximation.get(0).value;
		}
	}



	@DataProvider(format = "%m(%p[0])")
	public static Object[][] nonZeroNumbers() {
		return new Object[][] {
			{ 1 }, { 2 }, { 3 }, { 4 }, { 7 }, { 32 }, { 128 }, { 65536 }, { Integer.MAX_VALUE },
			{ -1 }, { -2 }, { -7 }, { -65536 }, { Integer.MIN_VALUE }
		};
	}



	@DataProvider(format = "%m(%p[0])")
	public static Object[][] integerNumbers() {
		final Object[][] nonZero = nonZeroNumbers();
		final Object[][] result = new Object[nonZero.length + 1][1];

		result[0][0] = 0;
		for (int i = 0; i < nonZero.length; ++i) {
			result[i + 1][0] = nonZero[i][0];
		}

		return result;
	}



	@DataProvider(format = "%m(%p[0], %p[1])")
	public static Object[][] integerNumbers2() {
		return DataProviders.crossProduct(integerNumbers(), integerNumbers());
	}



	@DataProvider(format = "%m(%p[0], %p[1])")
	public static Object[][] integerNumbersXNonZeroNumbers() {
		return DataProviders.crossProduct(integerNumbers(), nonZeroNumbers());
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testAddConst(final int left, final int right) {
		final BVAst tree = Builder.mkAdd(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left + right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testSubConst(final int left, final int right) {
		final BVAst tree = Builder.mkSub(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left - right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testMulConst(final int left, final int right) {
		final BVAst tree = Builder.mkMul(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left * right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbersXNonZeroNumbers")
	public void testSDivConst(final int left, final int right) {
		final BVAst tree = Builder.mkSDiv(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left / right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbersXNonZeroNumbers")
	public void testUDivConst(final int left, final int right) {
		final BVAst tree = Builder.mkUDiv(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, Integer.divideUnsigned(left, right),
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbersXNonZeroNumbers")
	public void testSRemConst(final int left, final int right) {
		final BVAst tree = Builder.mkSRem(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left % right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbersXNonZeroNumbers")
	public void testSModConst(final int left, final int right) {
		final BVAst tree = Builder.mkSMod(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		// Compute smod by:
		// rem := n % d;
		// t := (d <= 0) ? rem : -rem;
		// return (t <= 0) ? rem : rem + d;
		final int rem = left % right;
		final int mod = (((right <= 0) ? rem : -rem) <= 0) ? rem : rem + right;

		assertEquals("Error for " + tree, mod, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbersXNonZeroNumbers")
	public void testURemConst(final int left, final int right) {
		final BVAst tree = Builder.mkURem(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, Integer.remainderUnsigned(left, right),
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testShlConst(final int left, final int right) {
		final BVAst tree = Builder.mkShl(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? 0 : left << right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testShlConstFakeNoConst(final int left, final int right) {
		final BVAst tree = Builder.mkShl(Builder.mkBVConst(32, left),
				Builder.mkNot(Builder.mkNot(Builder.mkBVConst(32, right))));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? 0 : left << right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testAshrConst(final int left, final int right) {
		final BVAst tree = Builder.mkAshr(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? (left < 0 ? -1 : 0) : left >> right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testAshrConstFakeNoConst(final int left, final int right) {
		final BVAst tree = Builder.mkAshr(Builder.mkBVConst(32, left),
				Builder.mkNot(Builder.mkNot(Builder.mkBVConst(32, right))));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? (left < 0 ? -1 : 0) : left >> right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testLshrConst(final int left, final int right) {
		final BVAst tree = Builder.mkLshr(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? 0 : left >>> right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testLshrConstFakeNoConst(final int left, final int right) {
		final BVAst tree = Builder.mkLshr(Builder.mkBVConst(32, left),
				Builder.mkNot(Builder.mkNot(Builder.mkBVConst(32, right))));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				(right >= 32 || right < 0) ? 0 : left >>> right,
				PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testAndConst(final int left, final int right) {
		final BVAst tree = Builder.mkAnd(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left & right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testOrConst(final int left, final int right) {
		final BVAst tree = Builder.mkOr(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left | right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testXorConst(final int left, final int right) {
		final BVAst tree = Builder.mkXor(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left ^ right, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers")
	public void testNegConst(final int value) {
		final BVAst tree = Builder.mkNeg(Builder.mkBVConst(32, value));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, -value, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers")
	public void testNotConst(final int value) {
		final BVAst tree = Builder.mkNot(Builder.mkBVConst(32, value));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, ~value, PseudoApproximation.toInt(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testEqConst(final int left, final int right) {
		final BoolAst tree = Builder.mkEq(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left == right, PseudoApproximation.toBool(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testNeConst(final int left, final int right) {
		final BoolAst tree = Builder.mkNe(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left != right, PseudoApproximation.toBool(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testUltConst(final int left, final int right) {
		final BoolAst tree = Builder.mkULt(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				Integer.compareUnsigned(left, right) < 0,
				PseudoApproximation.toBool(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testUleConst(final int left, final int right) {
		final BoolAst tree = Builder.mkULe(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree,
				Integer.compareUnsigned(left, right) <= 0,
				PseudoApproximation.toBool(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testSltConst(final int left, final int right) {
		final BoolAst tree = Builder.mkSLt(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left < right, PseudoApproximation.toBool(result));
	}



	@Test(timeout = 3000)
	@UseDataProvider("integerNumbers2")
	public void testSleConst(final int left, final int right) {
		final BoolAst tree = Builder.mkSLe(Builder.mkBVConst(32, left), Builder.mkBVConst(32, right));

		final MultiBitsApproximation<PseudoApproximation> result
				= tree.accept(new ApproximationBuilder<PseudoApproximation>(
						PseudoApproximation::ofBit, null, Collections.emptyList()));

		assertEquals("Error for " + tree, left <= right, PseudoApproximation.toBool(result));
	}
}

