package smt;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(DataProviderRunner.class)
public class BitVectorTest {

	@DataProvider(format = "%m(%p[0], %p[1])")
	public static Object[][] integers() {
		return new Object[][] {
			{ 0, 0 },
			{ 0, 1 },
			{ 1, 0 },
			{ -1, 0 },
			{ 0, -1 },
			{ -1, 1 },
			{ 1, -1 },
			{ -1, -1 },
			{ 2, 1 },
			{ 1, 2 },
			{ 3, 3 },
			{ 4, 10 },
			{ 83, 65 },
			{ 82, 65 },
			{ -13, 37 },
			{ -1069087811, -2138175624 },
			{ Integer.MAX_VALUE, Integer.MAX_VALUE },
			{ Integer.MIN_VALUE, Integer.MAX_VALUE },
			{ Integer.MAX_VALUE, Integer.MIN_VALUE },
			{ Integer.MIN_VALUE, Integer.MIN_VALUE }
		};
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testAdd(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a + b), new BitVector(32, a).add(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testSub(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a - b), new BitVector(32, a).sub(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testMul(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a * b), new BitVector(32, a).mul(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testAnd(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a & b), new BitVector(32, a).and(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testOr(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a | b), new BitVector(32, a).or(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testXor(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a ^ b), new BitVector(32, a).xor(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testShl(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a << b),
				new BitVector(32, a).shl(new BitVector(32, b).and(new BitVector(32, 0x1F))));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testAshr(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a >> b),
				new BitVector(32, a).ashr(new BitVector(32, b).and(new BitVector(32, 0x1F))));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testLshr(final Integer a, final Integer b) {
		assertEquals(new BitVector(32, a >>> b),
				new BitVector(32, a).lshr(new BitVector(32, b).and(new BitVector(32, 0x1F))));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testUGt(final Integer a, final Integer b) {
		assertEquals(Integer.compareUnsigned(a, b) > 0,
				new BitVector(32, a).ugt(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testUGe(final Integer a, final Integer b) {
		assertEquals(Integer.compareUnsigned(a, b) >= 0,
				new BitVector(32, a).uge(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testULt(final Integer a, final Integer b) {
		assertEquals(Integer.compareUnsigned(a, b) < 0,
				new BitVector(32, a).ult(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testULe(final Integer a, final Integer b) {
		assertEquals(Integer.compareUnsigned(a, b) <= 0,
				new BitVector(32, a).ule(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testSGt(final Integer a, final Integer b) {
		assertEquals(a > b, new BitVector(32, a).sgt(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testSGe(final Integer a, final Integer b) {
		assertEquals(a >= b, new BitVector(32, a).sge(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testSLt(final Integer a, final Integer b) {
		assertEquals(a < b, new BitVector(32, a).slt(new BitVector(32, b)));
	}



	@Test(timeout = 1000)
	@UseDataProvider("integers")
	public void testSLe(final Integer a, final Integer b) {
		assertEquals(a <= b, new BitVector(32, a).sle(new BitVector(32, b)));
	}
}

