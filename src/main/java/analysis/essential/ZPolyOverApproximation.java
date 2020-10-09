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
package analysis.essential;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import smt.Ast;



/**
 * An overapproximation of the set of essential bits. This approximation uses
 * a linear Zhegalkin polynomial.
 */
public final class ZPolyOverApproximation
		implements Approximation<ZPolyOverApproximation>, Iterable<Integer>, BitContainer {

	/**
	 * Indicates whether the Zhegalkin polynomial contains a single '1'.
	 */
	private boolean containsOne;

	/**
	 * Contains a linear overapproximation of the monomials in the Zhegalkin
	 * polynomial.
	 */
	private long[] monomials;



	private ZPolyOverApproximation(final boolean containsOne, final long[] monomials) {
		this.containsOne = containsOne;
		this.monomials = Objects.requireNonNull(monomials);
	}



	public static MultiBitsApproximation<ZPolyOverApproximation> create(final Ast ast,
			final List<? extends Ast> variables) {

		return ast.accept(new ApproximationBuilder<ZPolyOverApproximation>(
				ZPolyOverApproximation::createConstant,
				ZPolyOverApproximation::createVariable,
				variables));
	}



	public static MultiBitsApproximation<ZPolyOverApproximation> create(final Ast ast,
			final Map<? extends Ast, MultiBitsApproximation<ZPolyOverApproximation>> varApproxs) {

		return ast.accept(new ApproximationBuilder<ZPolyOverApproximation>(
				ZPolyOverApproximation::createConstant,
				ZPolyOverApproximation::createVariable,
				varApproxs));
	}



	private static ZPolyOverApproximation createConstant(final int bitValue) {
		return new ZPolyOverApproximation(bitValue != 0, PackageConsts.EMPTY_LONG_ARRAY);
	}



	private static ZPolyOverApproximation createVariable(final int variableIndex) {
		final long[] monomials = new long[(variableIndex >> 6) + 1];
		monomials[variableIndex >> 6] = 1L << (variableIndex & 0x3F);
		return new ZPolyOverApproximation(false, monomials);
	}



	private boolean noMonomialSet() {
		for (int i = 0; i < this.monomials.length; ++i) {
			if (this.monomials[i] != 0) {
				return false;
			}
		}
		return true;
	}



	private static void monomialsOr(final long[] dest, final long[] other) {
		for (int i = 0; i < other.length; ++i) {
			dest[i] |= other[i];
		}
	}



	@Override
	public ZPolyOverApproximation and(final ZPolyOverApproximation other) {
		// f * g ~~> (N(f) & N(g), ((!N(f) & V(f) = {}) | (!N(g) & V(g) = {})) ? {} : V(f) | V(g))

		if ((!this.containsOne && this.noMonomialSet())
				|| (!other.containsOne && other.noMonomialSet())) {

			return new ZPolyOverApproximation(false, PackageConsts.EMPTY_LONG_ARRAY);
		}

		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsOr(newMonomials, other.monomials);

		return new ZPolyOverApproximation(this.containsOne & other.containsOne, newMonomials);
	}



	@Override
	public ZPolyOverApproximation andM(final ZPolyOverApproximation other) {
		// f * g ~~> (N(f) & N(g), ((!N(f) & V(f) = {}) | (!N(g) & V(g) = {})) ? {} : V(f) | V(g))

		if ((!this.containsOne && this.noMonomialSet())
				|| (!other.containsOne && other.noMonomialSet())) {

			this.containsOne = false;
			this.monomials = PackageConsts.EMPTY_LONG_ARRAY;
			return this;
		}

		if (this.monomials.length < other.monomials.length) {
			this.monomials = Arrays.copyOf(this.monomials, other.monomials.length);
		}

		monomialsOr(this.monomials, other.monomials);
		this.containsOne = this.containsOne & other.containsOne;

		return this;
	}



	@Override
	public ZPolyOverApproximation xor(final ZPolyOverApproximation other) {
		// f + g ~~> (N(f) ^ N(g), V(f) | V(g))
		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsOr(newMonomials, other.monomials);

		return new ZPolyOverApproximation(this.containsOne != other.containsOne, newMonomials);
	}



	@Override
	public ZPolyOverApproximation xorM(final ZPolyOverApproximation other) {
		if (this.monomials.length < other.monomials.length) {
			this.monomials = Arrays.copyOf(this.monomials, other.monomials.length);
		}

		// f + g ~~> (N(f) ^ N(g), V(f) | V(g))
		monomialsOr(this.monomials, other.monomials);
		this.containsOne = this.containsOne != other.containsOne;

		return this;
	}



	@Override
	public ZPolyOverApproximation not() {
		// this.monomials is never changed, so we can just pass it to the
		// constructor
		return new ZPolyOverApproximation(!this.containsOne, this.monomials);
	}



	@Override
	public ZPolyOverApproximation notM() {
		this.containsOne = !this.containsOne;
		return this;
	}



	@Override
	public ZPolyOverApproximation join(final ZPolyOverApproximation other) {
		// (N(f) | N(g), V(f) | V(g))
		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsOr(newMonomials, other.monomials);

		return new ZPolyOverApproximation(this.containsOne || other.containsOne, newMonomials);
	}



	@Override
	public boolean isConstant(final int value) {
		boolean isEmpty = true;
		for (int i = 0; i < this.monomials.length; ++i) {
			isEmpty &= this.monomials[i] == 0;
		}
		return isEmpty && ((value == 0) == !this.containsOne);
	}



	@Override
	public int hashCode() {
		return this.containsOne ? Arrays.hashCode(this.monomials) : ~Arrays.hashCode(this.monomials);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ZPolyOverApproximation) {
			final ZPolyOverApproximation other = (ZPolyOverApproximation) obj;
			return this.containsOne == other.containsOne
					&& Arrays.equals(this.monomials, other.monomials);
		}
		return false;
	}



	@Override
	public String toString() {
		final StringBuilder resultBuilder = new StringBuilder();

		resultBuilder.append("<ZOAP: ");
		resultBuilder.append(this.containsOne ? '1' : '0');
		resultBuilder.append("; {");

		boolean first = true;
		for (int i : this) {
			if (!first) {
				resultBuilder.append(", ");
			}
			resultBuilder.append('v').append(i);
			first = false;
		}

		resultBuilder.append("}>");
		return resultBuilder.toString();
	}



	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				int arrIdx = this.index >> 6;
				int wordIdx = this.index & 0x3F;
				while (arrIdx < ZPolyOverApproximation.this.monomials.length
						&& (ZPolyOverApproximation.this.monomials[arrIdx] & (1L << wordIdx)) == 0) {
					wordIdx += 1;
					if (wordIdx >= 64) {
						wordIdx = 0;
						arrIdx += 1;
					}
				}
				this.index = (arrIdx << 6) + wordIdx;
				return arrIdx < ZPolyOverApproximation.this.monomials.length;
			}

			@Override
			public Integer next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}
				return this.index++;
			}
		};
	}



	public Iterable<Integer> inverse(final int numBits) {
		return new Iterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int index = 0;

					@Override
					public boolean hasNext() {
						int arrIdx = this.index >> 6;
						int wordIdx = this.index & 0x3F;
						while (arrIdx < ZPolyOverApproximation.this.monomials.length
								&& (ZPolyOverApproximation.this.monomials[arrIdx] & (1L << wordIdx)) != 0) {
							wordIdx += 1;
							if (wordIdx >= 64) {
								wordIdx = 0;
								arrIdx += 1;
							}
						}
						this.index = (arrIdx << 6) + wordIdx;
						return arrIdx < ZPolyOverApproximation.this.monomials.length;
					}

					@Override
					public Integer next() {
						if (!hasNext()) {
							throw new NoSuchElementException();
						}
						return this.index++;
					}
				};
			}
		};
	}



	public boolean containsOne() {
		return this.containsOne;
	}



	@Override
	public boolean contains(final int bit) {
		final int arrIdx = bit >> 6;
		final int wordIdx = bit & 0x3F;
		return arrIdx < this.monomials.length && (this.monomials[arrIdx] & (1L << wordIdx)) != 0;
	}
}

