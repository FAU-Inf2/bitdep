/*
 * This file is part of bitdep.
 *
 * Copyright (c) 2020 Lehrstuhl fuer Informatik 2,
 * Friedrich-Alexander-Universität Erlangen-Nürnberg (FAU)
 *
 * bitdep is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bitdep is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with bitdep.  If not, see <http://www.gnu.org/licenses/>.
 */
package smt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;



public class BitVector {

	private final BigInteger data;
	private final int width;



	public BitVector(final int width, final long value) {
		this(width, BigInteger.valueOf(value));
	}



	public BitVector(final String bits) {
		this(bits.length(), new BigInteger(bits, 2));
	}



	public BitVector(final int[] bits) {
		this(bits.length, fromArray(bits));
	}



	public BitVector(final int width, final BigInteger data) {
		this.width = width;
		if (this.width < 1) {
			throw new IllegalArgumentException();
		}
		this.data = constrainToWidth(width, data);
	}



	private static BigInteger fromArray(final int[] bits) {
		BigInteger r = BigInteger.ZERO;
		for (int i = 0; i < bits.length; ++i) {
			if (bits[i] != 0) {
				r = r.setBit(i);
			}
		}
		return r;
	}



	private static BigInteger constrainToWidth(final int width, final BigInteger data) {
		BigInteger r;
		if (!data.testBit(width - 1)) {
			r = BigInteger.ZERO;
		} else {
			r = BigInteger.valueOf(-1);
		}
		for (int i = 0; i < width; ++i) {
			if (data.testBit(i)) {
				r = r.setBit(i);
			} else {
				r = r.clearBit(i);
			}
		}
		return r;
	}



	public int getWidth() {
		return this.width;
	}



	public boolean getBit(final int i) {
		if (i < 0 || i >= this.width) {
			throw new IllegalArgumentException();
		}
		return this.data.testBit(i);
	}



	public BigInteger toSignedBigInteger() {
		return this.data;
	}



	public BigInteger toUnsignedBigInteger() {
		return unsigned(this.width, this.data)
				.and(BigInteger.ONE.shiftLeft(this.width).subtract(BigInteger.ONE));
	}



	private boolean unsignedComparison(final BitVector other, final boolean equalCase,
			final boolean greaterCase, final boolean smallerCase) {

		if (this.getWidth() != other.getWidth()) {
			throw new BitWidthMismatchException();
		}
		int i = this.getWidth() - 1;
		while (i >= 0) {
			if (this.getBit(i) && !other.getBit(i)) {
				return greaterCase;
			} else if (!this.getBit(i) && other.getBit(i)) {
				return smallerCase;
			}
			i -= 1;
		}
		return equalCase;
	}



	public boolean ult(final BitVector other) {
		return unsignedComparison(other, false, false, true);
	}



	public boolean ule(final BitVector other) {
		return unsignedComparison(other, true, false, true);
	}



	public boolean ugt(final BitVector other) {
		return unsignedComparison(other, false, true, false);
	}



	public boolean uge(final BitVector other) {
		return unsignedComparison(other, true, true, false);
	}



	private boolean signedComparison(final BitVector other, final boolean equalCase,
			final boolean greaterCase, final boolean smallerCase) {

		if (this.getWidth() != other.getWidth()) {
			throw new BitWidthMismatchException();
		}

		if (this.getBit(this.getWidth() - 1) && !other.getBit(this.getWidth() - 1)) {
			return smallerCase;
		} else if (!this.getBit(this.getWidth() - 1) && other.getBit(this.getWidth() - 1)) {
			return greaterCase;
		}

		int i = this.getWidth() - 2;
		while (i >= 0) {
			if (this.getBit(i) && !other.getBit(i)) {
				return greaterCase;
			} else if (!this.getBit(i) && other.getBit(i)) {
				return smallerCase;
			}
			i -= 1;
		}
		return equalCase;
	}



	public boolean slt(final BitVector other) {
		return signedComparison(other, false, false, true);
	}



	public boolean sle(final BitVector other) {
		return signedComparison(other, true, false, true);
	}



	public boolean sgt(final BitVector other) {
		return signedComparison(other, false, true, false);
	}



	public boolean sge(final BitVector other) {
		return signedComparison(other, true, true, false);
	}



	private static BigInteger signed(final int width, final BigInteger val) {
		final int signum = !val.testBit(width - 1) ? val.signum() : -1;
		final byte[] ba = val.toByteArray();
		final byte[] ra = new byte[(width + 7) >> 3];

		Arrays.fill(ra, (byte) (signum < 0 ? -1 : 0));

		System.arraycopy(ba, Math.max(0, ba.length - ra.length),
				ra, Math.max(0, ra.length - ba.length), Math.min(ra.length, ba.length));

		return new BigInteger(ra);
	}



	private static BigInteger unsigned(final int width, final BigInteger val) {
		final byte[] ba = val.toByteArray();
		final byte[] ra = new byte[(width + 7) >> 3];

		Arrays.fill(ra, (byte) (val.signum() < 0 ? -1 : 0));

		System.arraycopy(ba, Math.max(0, ba.length - ra.length),
				ra, Math.max(0, ra.length - ba.length), ba.length);

		return new BigInteger(val.signum() == 0 ? 0 : 1, ra);
	}



	private BitVector binaryOp(final BitVector other, final BinaryOperator<BigInteger> op) {
		if (this.width != other.width) {
			throw new BitWidthMismatchException();
		}
		return new BitVector(this.width, op.apply(this.data, other.data));
	}



	private boolean checkLeftShiftOverflow(final BigInteger lhs, final BigInteger rhs) {
		return 0 < Integer.compareUnsigned(
				lhs.bitCount() + (rhs.intValue() & 0x7FFFFFFF),
				Integer.MAX_VALUE);
	}



	public BitVector add(final BitVector other) {
		return binaryOp(other, (a, b) -> a.add(b));
	}



	public BitVector sub(final BitVector other) {
		return binaryOp(other, (a, b) -> a.subtract(b));
	}



	public BitVector mul(final BitVector other) {
		return binaryOp(other, (a, b) -> a.multiply(b));
	}



	public BitVector sdiv(final BitVector other) {
		return binaryOp(other, (a, b) -> b.equals(BigInteger.ZERO)
				? b.subtract(BigInteger.ONE)
				: a.divide(b));
	}



	public BitVector udiv(final BitVector other) {
		return binaryOp(other, (a, b) -> b.equals(BigInteger.ZERO)
				? b.subtract(BigInteger.ONE)
				: signed(this.width, unsigned(this.width, a).divide(unsigned(other.getWidth(), b))));
	}



	public BitVector srem(final BitVector other) {
		return binaryOp(other, (a, b) -> b.equals(BigInteger.ZERO)
				? a
				: a.remainder(b));
	}



	public BitVector urem(final BitVector other) {
		return binaryOp(other, (a, b) -> b.equals(BigInteger.ZERO)
				? a
				: signed(this.width, unsigned(this.width, a).remainder(unsigned(other.getWidth(), b))));
	}



	public BitVector smod(final BitVector other) {
		if (this.width != other.width) {
			throw new BitWidthMismatchException();
		}
		if (other.data.equals(BigInteger.ZERO)) {
			return this;
		}
		// Implementation based on floorDiv/floorMod from OpenJDK
		BigInteger r = this.data.divide(other.data);
		if (Math.min(0, this.data.signum()) != Math.min(0, other.data.signum())
				&& !r.multiply(other.data).equals(this.data)) {
			r = r.subtract(BigInteger.ONE);
		}
		return new BitVector(this.width, this.data.subtract(r.multiply(other.data)));
	}



	public BitVector umod(final BitVector other) {
		return this.urem(other);
	}



	public BitVector and(final BitVector other) {
		return binaryOp(other, (a, b) -> a.and(b));
	}



	public BitVector or(final BitVector other) {
		return binaryOp(other, (a, b) -> a.or(b));
	}



	public BitVector xor(final BitVector other) {
		return binaryOp(other, (a, b) -> a.xor(b));
	}



	public BitVector shl(final BitVector other) {
		return binaryOp(other,
				(a, b) -> b.bitCount() >= 31 || b.bitLength() >= 31 || b.signum() < 0
						|| checkLeftShiftOverflow(a, b)
					? BigInteger.ZERO
					: a.shiftLeft(b.intValue() & 0x7FFFFFFF));
	}



	public BitVector ashr(final BitVector other) {
		return binaryOp(other,
				(a, b) -> b.bitCount() >= 31 || b.bitLength() >= 31 || b.signum() < 0
					? BigInteger.ZERO
					: a.shiftRight(b.intValue() & 0x7FFFFFFF));
	}



	public BitVector lshr(final BitVector other) {
		return binaryOp(other,
				(a, b) -> b.bitCount() >= 31 || b.bitLength() >= 31 || b.signum() < 0
					? BigInteger.ZERO
					: signed(this.width, unsigned(this.width, a).shiftRight(b.intValue() & 0x7FFFFFFF)));
	}



	public BitVector rol(final BitVector other) {
		return this.shl(other).or(this.lshr(new BitVector(this.width, this.width).sub(other)));
	}



	public BitVector ror(final BitVector other) {
		return this.lshr(other).or(this.shl(new BitVector(this.width, this.width).sub(other)));
	}



	public BitVector concat(final BitVector other) {
		BigInteger r = BigInteger.ZERO;
		for (int i = 0; i < other.width; ++i) {
			if (other.data.testBit(i)) {
				r = r.setBit(i);
			}
		}
		for (int i = 0; i < this.width; ++i) {
			if (this.data.testBit(i)) {
				r = r.setBit(other.width + i);
			}
		}
		return new BitVector(this.width + other.width, r);
	}



	public BitVector extract(final int low, final int high) {
		if (low < 0 || low > high) {
			throw new IllegalArgumentException();
		}
		BigInteger r = BigInteger.ZERO;
		for (int i = low; i <= high; ++i) {
			if (this.data.testBit(i)) {
				r = r.setBit(i - low);
			}
		}
		return new BitVector(high - low + 1, r);
	}



	public BitVector not() {
		return new BitVector(this.width, this.data.not());
	}



	public BitVector neg() {
		return new BitVector(this.width, this.data.negate());
	}



	public BitVector signExtend(final int width) {
		if (width <= this.width) {
			throw new IllegalArgumentException();
		}
		if (this.data.signum() < 0) {
			BigInteger current = this.data;
			for (int i = this.width; i < width; ++i) {
				current = current.setBit(i);
			}
			return new BitVector(width, current);
		} else {
			return new BitVector(width, this.data);
		}
	}



	@Override
	public int hashCode() {
		return Objects.hash(this.width, this.data);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BitVector) {
			final BitVector other = (BitVector) obj;

			return this.getWidth() == other.getWidth()
					&& this.data.equals(other.data);
		}
		return false;
	}



	public String toDecimalString() {
		return this.data.toString();
	}



	public String toBinaryString() {
		final StringBuilder builder = new StringBuilder();
		for (int i = this.width - 1; i >= 0; --i) {
			builder.append(this.data.testBit(i) ? '1' : '0');
		}
		return builder.toString();
	}



	public String toHexadecimalString() {
		final StringBuilder builder = new StringBuilder();
		for (int i = ((this.width - 1) | 7) - 3; i >= 0; i -= 4) {
			int v = 0;
			for (int j = 3; j >= 0; --j) {
				v <<= 1;
				v |= this.data.testBit(i + j) ? 1 : 0;
			}
			assert 0 <= v && v < 16;
			builder.append(Integer.toString(v, 16));
		}
		return builder.toString();
	}



	@Override
	public String toString() {
		return this.toDecimalString();
	}
}

