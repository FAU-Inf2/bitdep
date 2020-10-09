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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.google.common.collect.Iterables;

import smt.Ast;
import smt.BitVector;
import smt.BoolAst;
import smt.Builder;
import smt.BVAst;
import smt.VarReplacer;



/**
 * An underapproximation of the set of essential bits. This approximation uses
 * a linear Zhegalkin polynomial.
 */
public final class ZPolyUnderApproximation
		implements Approximation<ZPolyUnderApproximation>, Iterable<Integer>, BitContainer {

	/**
	 * Indicates whether the Zhegalkin polynomial contains a single '1'.
	 */
	boolean containsOne;

	/**
	 * Contains the monomials of the linear part of the Zhegalkin polynomial.
	 */
	long[] monomials;



	private ZPolyUnderApproximation(final boolean containsOne, final long[] monomials) {
		this.containsOne = containsOne;
		this.monomials = Objects.requireNonNull(monomials);
	}



	public static MultiBitsApproximation<ZPolyUnderApproximation> create(final Ast ast,
			final List<? extends Ast> variables) {

		return create(ast, new ApproximationBuilder<ZPolyUnderApproximation>(
				ZPolyUnderApproximation::createConstant,
				ZPolyUnderApproximation::createVariable,
				variables));
	}



	private static MultiBitsApproximation<ZPolyUnderApproximation> create(final Ast ast,
			final ApproximationBuilder<ZPolyUnderApproximation> builder) {

		return ast.accept(builder);
	}



	public static MultiBitsApproximation<ZPolyUnderApproximation> create(final Ast ast,
			final List<? extends Ast> variables, final int order) {

		return create(
				ast,
				variables,
				new ApproximationBuilder<ZPolyUnderApproximation>(
					ZPolyUnderApproximation::createConstant,
					ZPolyUnderApproximation::createVariable,
					variables),
				order);
	}



	private static MultiBitsApproximation<ZPolyUnderApproximation> create(final Ast ast,
			final List<? extends Ast> variables,
			final ApproximationBuilder<ZPolyUnderApproximation> builder,
			final int order) {

		if (order <= 0) {
			throw new IllegalArgumentException();
		}

		if (order == 1) {
			return create(ast, builder);
		}

		final List<ZPolyUnderApproximation> bitApproximations = new ArrayList<>();

		final MultiBitsApproximation<ZPolyOverApproximation> overApprox
				= ZPolyOverApproximation.create(ast, variables);

		int numInBits = 0;
		for (final Ast v : variables) {
			if (v instanceof BVAst) {
				final BVAst bvv = (BVAst) v;

				for (int i = 0; i < bvv.getWidth(); ++i) {
					if (!bitComplete(bitApproximations, overApprox, i + numInBits)) {
						final BitVector oneShlI = new BitVector(bvv.getWidth(), 1)
								.shl(new BitVector(bvv.getWidth(), i));

						final Ast ast0 = ast.accept(new VarReplacer(Collections.singletonMap(
								v, Builder.mkAnd(bvv, Builder.mkBVConst(oneShlI.not())))));

						final Ast ast1 = ast.accept(new VarReplacer(Collections.singletonMap(
								v, Builder.mkOr(bvv, Builder.mkBVConst(oneShlI)))));

						final MultiBitsApproximation<ZPolyUnderApproximation> nextApprox
								= create(
										ast instanceof BVAst
											? Builder.mkXor((BVAst) ast0, (BVAst) ast1)
											: Builder.mkNe((BoolAst) ast0, (BoolAst) ast1),
										variables,
										builder,
										order - 1);

						for (int j = 0; j < nextApprox.bitWidth(); ++j) {
							if (j >= bitApproximations.size()) {
								bitApproximations.add(new ZPolyUnderApproximation(
										overApprox.get(j).containsOne(), PackageConsts.EMPTY_LONG_ARRAY));
							}

							if (nextApprox.get(j).containsOne() || !Iterables.isEmpty(nextApprox.get(j))) {
								bitApproximations.set(j, bitApproximations.get(j).set(numInBits + i));
								for (final Integer bit : nextApprox.get(j)) {
									bitApproximations.set(j, bitApproximations.get(j).set(bit));
								}
							}
						}
					}
				}
				numInBits += ((BVAst) v).getWidth();
			} else {
				assert false : "TODO";
				numInBits += 1;
			}
		}

		return new MultiBitsApproximation<>(bitApproximations);
	}



	private static boolean bitComplete(final List<ZPolyUnderApproximation> bitApproximations,
			final MultiBitsApproximation<ZPolyOverApproximation> overApproximation, final int bit) {

		if (bitApproximations.isEmpty()) {
			return false;
		}

		for (int i = 0; i < overApproximation.bitWidth(); ++i) {
			if (overApproximation.get(i).contains(bit)
					&& (bitApproximations.size() <= i || !bitApproximations.get(i).contains(bit))) {
				return false;
			}
		}
		return true;
	}



	private static ZPolyUnderApproximation createConstant(final int bitValue) {
		return new ZPolyUnderApproximation(bitValue != 0, PackageConsts.EMPTY_LONG_ARRAY);
	}



	private static ZPolyUnderApproximation createVariable(final int variableIndex) {
		final long[] monomials = new long[(variableIndex >> 6) + 1];
		monomials[variableIndex >> 6] = 1L << (variableIndex & 0x3F);
		return new ZPolyUnderApproximation(false, monomials);
	}



	private static void monomialsAnd(final long[] dest, final long[] other) {
		int i = 0;
		for (; i < other.length; ++i) {
			dest[i] &= other[i];
		}
		for (; i < dest.length; ++i) {
			dest[i] = 0;
		}
	}



	private static void monomialsAndn(final long[] dest, final long[] other) {
		for (int i = 0; i < other.length; ++i) {
			dest[i] &= ~other[i];
		}
	}



	private static void monomialsXor(final long[] dest, final long[] other) {
		for (int i = 0; i < other.length; ++i) {
			dest[i] ^= other[i];
		}
	}



	@Override
	public ZPolyUnderApproximation and(final ZPolyUnderApproximation other) {
		// f * g ~~> (N(f) & N(g), (V(f) & V(g)) ^ (N(f) ? V(g) : {}) ^ (N(g) ? V(f) : {}))

		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsAnd(newMonomials, other.monomials);

		if (this.containsOne) {
			monomialsXor(newMonomials, other.monomials);
		}
		if (other.containsOne) {
			monomialsXor(newMonomials, this.monomials);
		}

		return new ZPolyUnderApproximation(this.containsOne & other.containsOne, newMonomials);
	}



	@Override
	public ZPolyUnderApproximation andM(final ZPolyUnderApproximation other) {
		if (this.monomials.length < other.monomials.length) {
			this.monomials = Arrays.copyOf(this.monomials, other.monomials.length);
		}

		// f * g ~~> (N(f) & N(g), (V(f) & V(g)) ^ (N(f) ? V(g) : {}) ^ (N(g) ? V(f) : {}))

		if (other.containsOne) {
			// (x & y) ^ x = x \ y
			monomialsAndn(this.monomials, other.monomials);
		} else {
			monomialsAnd(this.monomials, other.monomials);
		}
		if (this.containsOne) {
			monomialsXor(this.monomials, other.monomials);
		}

		this.containsOne = this.containsOne & other.containsOne;

		return this;
	}



	@Override
	public ZPolyUnderApproximation xor(final ZPolyUnderApproximation other) {
		// f + g ~~> (N(f) ^ N(g), V(f) ^ V(g))
		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsXor(newMonomials, other.monomials);

		return new ZPolyUnderApproximation(this.containsOne != other.containsOne, newMonomials);
	}



	@Override
	public ZPolyUnderApproximation xorM(final ZPolyUnderApproximation other) {
		if (this.monomials.length < other.monomials.length) {
			this.monomials = Arrays.copyOf(this.monomials, other.monomials.length);
		}

		// f + g ~~> (N(f) ^ N(g), V(f) ^ V(g))
		this.containsOne = this.containsOne != other.containsOne;
		monomialsXor(this.monomials, other.monomials);

		return this;
	}



	@Override
	public ZPolyUnderApproximation not() {
		// this.monomials is never changed, so we can just pass it to the
		// constructor
		return new ZPolyUnderApproximation(!this.containsOne, this.monomials);
	}



	@Override
	public ZPolyUnderApproximation notM() {
		this.containsOne = !this.containsOne;
		return this;
	}



	@Override
	public ZPolyUnderApproximation join(final ZPolyUnderApproximation other) {
		// (N(f) | N(g), V(f) | V(g))
		final long[] newMonomials = Arrays.copyOf(this.monomials,
				Math.max(this.monomials.length, other.monomials.length));
		monomialsXor(newMonomials, other.monomials); // Dummy

		return new ZPolyUnderApproximation(this.containsOne || other.containsOne, newMonomials);
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
		if (obj instanceof ZPolyUnderApproximation) {
			final ZPolyUnderApproximation other = (ZPolyUnderApproximation) obj;
			return this.containsOne == other.containsOne
					&& Arrays.equals(this.monomials, other.monomials);
		}
		return false;
	}



	@Override
	public String toString() {
		final StringBuilder resultBuilder = new StringBuilder();

		resultBuilder.append("<ZUAP: ");
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
				while (arrIdx < ZPolyUnderApproximation.this.monomials.length
						&& (ZPolyUnderApproximation.this.monomials[arrIdx] & (1L << wordIdx)) == 0) {
					wordIdx += 1;
					if (wordIdx >= 64) {
						wordIdx = 0;
						arrIdx += 1;
					}
				}
				this.index = (arrIdx << 6) + wordIdx;
				return arrIdx < ZPolyUnderApproximation.this.monomials.length;
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



	public boolean containsOne() {
		return this.containsOne;
	}



	@Override
	public boolean contains(final int bit) {
		final int arrIdx = bit >> 6;
		final int wordIdx = bit & 0x3F;
		return arrIdx < this.monomials.length && (this.monomials[arrIdx] & (1L << wordIdx)) != 0;
	}



	protected ZPolyUnderApproximation set(final int bit) {
		final int arrIdx = bit >> 6;
		final int wordIdx = bit & 0x3F;
		if (this.monomials.length > arrIdx) {
			this.monomials[arrIdx] |= 1L << wordIdx;
			return this;
		} else {
			final long[] newMonomials = Arrays.copyOf(this.monomials, arrIdx + 1);
			newMonomials[arrIdx] = 1L << wordIdx;
			return new ZPolyUnderApproximation(this.containsOne, newMonomials);
		}
	}
}

