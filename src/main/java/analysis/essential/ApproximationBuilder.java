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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import smt.Ast;
import smt.BitVector;
import smt.BoolAllDifferent;
import smt.BoolBinBV;
import smt.BoolBin;
import smt.BoolConst;
import smt.BoolNegate;
import smt.BoolVar;
import smt.BVAst;
import smt.BVBinary;
import smt.BVConst;
import smt.BVExtract;
import smt.BVIte;
import smt.BVUnary;
import smt.BVUnaryOp;
import smt.BVVar;
import smt.TreeTransformer;
import util.Pair;



public class ApproximationBuilder<T extends Approximation>
		implements TreeTransformer<MultiBitsApproximation<T>> {

	private IntFunction<T> constBuilder;
	private IntFunction<T> singletonBuilder;
	private Map<Ast, MultiBitsApproximation<T>> variableApproximations;

	private final Map<Ast, MultiBitsApproximation<T>> cache = new HashMap<>();



	ApproximationBuilder(final IntFunction<T> constBuilder, final IntFunction<T> singletonBuilder,
			final List<? extends Ast> variables) {

		this.constBuilder = constBuilder;
		this.singletonBuilder = singletonBuilder;

		this.variableApproximations = new HashMap<>();

		int varIndex = 0;
		for (final Ast variable : variables) {
			if (variable instanceof BVVar) {
				final List<T> result = new ArrayList<>();
				for (int i = 0; i < ((BVVar) variable).getWidth(); ++i, ++varIndex) {
					result.add(this.singletonBuilder.apply(varIndex));
				}
				this.variableApproximations.put(variable, new MultiBitsApproximation<>(result));
			} else if (variable instanceof BoolVar) {
				this.variableApproximations.put(variable, new MultiBitsApproximation<>(
						Collections.singletonList(this.singletonBuilder.apply(varIndex))));
				varIndex += 1;
			} else {
				throw new IllegalArgumentException(variable + " is not a variable");
			}
		}
	}



	ApproximationBuilder(final IntFunction<T> constBuilder, final IntFunction<T> singletonBuilder,
			final Map<? extends Ast, MultiBitsApproximation<T>> variableApproximations) {

		this.constBuilder = constBuilder;
		this.singletonBuilder = singletonBuilder;

		this.variableApproximations = new HashMap<>(variableApproximations);
	}



	private MultiBitsApproximation<T> get(final Ast tree) {
		if (this.cache.containsKey(tree)) {
			return this.cache.get(tree);
		}
		final MultiBitsApproximation<T> result = tree.accept(this);
		this.cache.put(tree, result);
		return result;
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolAllDifferent tree) {
		final List<MultiBitsApproximation<T>> childrenRes = new ArrayList<>();
		for (final BVAst operand : tree.getOperands()) {
			childrenRes.add(get(operand));
		}

		final int width = childrenRes.size();
		if (width == 1) {
			return childrenRes.get(0);
		}

		Approximation result = distinct(childrenRes.get(width - 2).toList(),
				childrenRes.get(width - 1).toList());
		for (int i = width - 3; i >= 0; --i) {
			for (int j = i + 2; j < width; ++j) {
				result = result.and(distinct(childrenRes.get(i).toList(), childrenRes.get(j).toList()));
			}
		}
		return new MultiBitsApproximation<>(Collections.singletonList((T) result));
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolBinBV tree) {
		// Optimizations
		if (tree.getLeft().equals(tree.getRight())) {
			switch (tree.getKind()) {
				case EQUALS:
				case UGE:
				case ULE:
				case SGE:
				case SLE:
					return new MultiBitsApproximation<>(Collections.singletonList(
							this.constBuilder.apply(1)));

				default:
					return new MultiBitsApproximation<>(Collections.singletonList(
							this.constBuilder.apply(0)));
			}
		} else if (tree.getRight() instanceof BVConst) {
			final BitVector rightValue = ((BVConst) tree.getRight()).getValue();
			if (rightValue.toSignedBigInteger().equals(BigInteger.ZERO)) {
				switch (tree.getKind()) {
					case ULT:
						// Return 'false'
						return new MultiBitsApproximation<>(Collections.singletonList(
								this.constBuilder.apply(0)));

					case UGE:
						// Return 'true'
						return new MultiBitsApproximation<>(Collections.singletonList(
								this.constBuilder.apply(1)));

					case SLT:
						// Return most significant bit of 'left'
						return new MultiBitsApproximation<>(Collections.singletonList(
								((MultiBitsApproximation<T>) get(tree.getLeft()))
									.get(tree.getLeft().getWidth() - 1)));

					case SGE:
						// Return inverted most significant bit of 'left'
						return new MultiBitsApproximation<>(Collections.singletonList(
								(T) ((MultiBitsApproximation<T>) get(tree.getLeft()))
									.get(tree.getLeft().getWidth() - 1).not()));
				}
			}
		}

		final MultiBitsApproximation<T> left = (MultiBitsApproximation<T>) get(tree.getLeft());
		final MultiBitsApproximation<T> right = (MultiBitsApproximation<T>) get(tree.getRight());

		Approximation result;

		switch (tree.getKind()) {
			case EQUALS:
				result = left.get(0).xor(right.get(0).not());
				for (int i = 1; i < left.bitWidth(); ++i) {
					result = result.andM(right.get(i).not().xor(left.get(i)));
				}
				break;
			case DISTINCT:
				result = distinct(left.toList(), right.toList());
				break;
			case UGT:
				result = unsignedLessThan(right.toList(), left.toList());
				break;
			case UGE:
				result = unsignedLessEquals(right.toList(), left.toList());
				break;
			case ULT:
				result = unsignedLessThan(left.toList(), right.toList());
				break;
			case ULE:
				result = unsignedLessEquals(left.toList(), right.toList());
				break;
			case SGT:
				result = signedLessThan(right.toList(), left.toList());
				break;
			case SGE:
				result = signedLessEquals(right.toList(), left.toList());
				break;
			case SLT:
				result = signedLessThan(left.toList(), right.toList());
				break;
			case SLE:
				result = signedLessEquals(left.toList(), right.toList());
				break;
			default:
				throw new UnsupportedOperationException();
		}

		return new MultiBitsApproximation<>(Collections.singletonList((T) result));
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolBin tree) {
		final MultiBitsApproximation<T> left = (MultiBitsApproximation<T>) get(tree.getLeft());
		final MultiBitsApproximation<T> right = (MultiBitsApproximation<T>) get(tree.getRight());

		switch (tree.getKind()) {
			case EQUALS:
				return new MultiBitsApproximation<>(Collections.singletonList(
						(T) left.get(0).not().xor(right.get(0))));
			case DISTINCT:
				return new MultiBitsApproximation<>(Collections.singletonList(
						(T) left.get(0).xor(right.get(0))));
			case IMPLIES:
				return new MultiBitsApproximation<>(Collections.singletonList(
						(T) left.get(0).not().xor(right.get(0)).xorM(left.get(0).not().and(right.get(0)))));
			case AND:
				return new MultiBitsApproximation<>(Collections.singletonList(
						(T) left.get(0).and(right.get(0))));
			case OR:
				return new MultiBitsApproximation<>(Collections.singletonList(
						(T) left.get(0).and(right.get(0)).xorM(left.get(0)).xorM(right.get(0))));
			default:
				throw new UnsupportedOperationException();
		}
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolConst tree) {
		return new MultiBitsApproximation<>(Collections.singletonList(
				this.constBuilder.apply(tree.getValue() ? 1 : 0)));
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolNegate tree) {
		return (MultiBitsApproximation<T>) get(tree.getOperand()).not();
	}



	@Override
	public MultiBitsApproximation<T> visit(final BoolVar tree) {
		return this.variableApproximations.get(tree);
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVBinary tree) {
		final MultiBitsApproximation<T> left = (MultiBitsApproximation<T>) get(tree.getLeft());
		final MultiBitsApproximation<T> right = (MultiBitsApproximation<T>) get(tree.getRight());

		switch (tree.getKind()) {
			case PLUS: {
				return new MultiBitsApproximation<>(add(left.toList(), right.toList()));
			}

			case MINUS: {
				return new MultiBitsApproximation<>(sub(left.toList(), right.toList()));
			}

			case MUL: {
				// Implements Peasant Multiplication
				final int width = left.bitWidth();

				final T zero = this.constBuilder.apply(0);

				// Unrolled first iteration
				final List<T> temp = new ArrayList<>();
				final List<T> toAdd = new ArrayList<T>();
				for (int i = 0; i < width - 1; ++i) {
					temp.add((T) left.get(i).and(right.get(0)));
					toAdd.add(left.get(i));
				}
				temp.add((T) left.get(width - 1).and(right.get(0)));

				for (int i = 1; i < width; ++i) {
					// (right[i] == 1) ==> temp += left << i

					// (ite right(i) left 0)
					final List<T> toAdd2 = new ArrayList<>();
					for (final T l : toAdd) {
						toAdd2.add((T) l.and(right.get(i)));
					}

					final List<T> tempPlus = add(temp.subList(i, width), toAdd2);
					for (int j = i; j < width; ++j) {
						temp.set(j, (T) tempPlus.get(j - i));
					}
					toAdd.remove(toAdd.size() - 1);
				}
				return new MultiBitsApproximation<>(temp);
			}

			case SDIV:
				return new MultiBitsApproximation<>(signedDivide(left.toList(), right.toList())
						.getFirst());
			case UDIV:
				return new MultiBitsApproximation<>(unsignedDivide(left.toList(), right.toList())
						.getFirst());
			case SREM:
				return new MultiBitsApproximation<>(signedDivide(left.toList(), right.toList())
						.getSecond());
			case SMOD:
				return signedModulo(left.toList(), right.toList());
			case UMOD:
			case UREM:
				return new MultiBitsApproximation<>(unsignedDivide(left.toList(), right.toList())
						.getSecond());
			case AND:
				return (MultiBitsApproximation<T>) left.and(right);
			case OR:
				return (MultiBitsApproximation<T>) left.and(right).xorM(left).xorM(right);
			case XOR:
				return (MultiBitsApproximation<T>) left.xor(right);
			case SHL: {
				// eq_cnst(x, c) = product_i (getbit(c, i) = 1 ? x[i] : !x[i])
				// shl_i = sum_j (f_(i-j) * eq_cnst(g, j))
				final List<T> result = new ArrayList<>();

				if (tree.getRight() instanceof BVConst) {
					constShift(result, left,
							((BVConst) tree.getRight()).getValue().toUnsignedBigInteger().longValue(),
							this.constBuilder.apply(0));
				} else {
					final List<T> eqCnsts = getEqualsConstantList(right);

					final int cap = 32 - Integer.numberOfLeadingZeros(right.bitWidth() - 1);
					T capExpr = this.constBuilder.apply(1);
					for (int j = cap; j < right.bitWidth(); ++j) {
						capExpr = (T) capExpr.andM(right.get(j).not());
					}

					for (int i = 0; i < tree.getWidth(); ++i) {
						Approximation cur = left.get(i).and(eqCnsts.get(0));

						for (int j = 1; j <= i; ++j) {
							cur = cur.xorM(left.get(i - j).and(eqCnsts.get(j)));
						}

						result.add((T) cur.andM(capExpr));
					}
				}
				return new MultiBitsApproximation<>(result);
			}

			case ASHR: {
				final List<T> result = new ArrayList<>();

				if (tree.getRight() instanceof BVConst) {
					constShift(result, left,
							-((BVConst) tree.getRight()).getValue().toUnsignedBigInteger().longValue(),
							left.get(left.bitWidth() - 1));
				} else {
					final List<T> eqCnsts = getEqualsConstantList(right);

					final int cap = 32 - Integer.numberOfLeadingZeros(right.bitWidth() - 1);
					T capExpr = this.constBuilder.apply(1);
					for (int j = cap; j < right.bitWidth(); ++j) {
						capExpr = (T) capExpr.andM(right.get(j).not());
					}

					for (int i = 0; i < tree.getWidth(); ++i) {
						Approximation cur = left.get(i).and(eqCnsts.get(0));

						for (int j = tree.getWidth() - 1; j > 0; --j) {
							cur = cur.xorM(left.get(Math.min(j + i, left.bitWidth() - 1)).and(eqCnsts.get(j)));
						}

						cur = cur.xorM(left.get(left.bitWidth() - 1)).andM(capExpr)
								.xorM(left.get(left.bitWidth() - 1));

						result.add((T) cur);
					}
				}
				return new MultiBitsApproximation<>(result);
			}

			case LSHR: {
				final List<T> result = new ArrayList<>();

				if (tree.getRight() instanceof BVConst) {
					constShift(result, left,
							-((BVConst) tree.getRight()).getValue().toUnsignedBigInteger().longValue(),
							this.constBuilder.apply(0));
				} else {
					final List<T> eqCnsts = getEqualsConstantList(right);

					final int cap = 32 - Integer.numberOfLeadingZeros(right.bitWidth() - 1);
					T capExpr = this.constBuilder.apply(1);
					for (int j = cap; j < right.bitWidth(); ++j) {
						capExpr = (T) capExpr.andM(right.get(j).not());
					}

					for (int i = 0; i < tree.getWidth(); ++i) {
						Approximation cur = left.get(i).and(eqCnsts.get(0));

						for (int j = tree.getWidth() - 1; j > i; --j) {
							cur = cur.xorM(left.get(j).and(eqCnsts.get(j - i)));
						}

						result.add((T) cur.andM(capExpr));
					}
				}
				return new MultiBitsApproximation<>(result);
			}

			case ROL:
			case ROR:
				assert false : "TODO";
			case CONCAT:
				return left.concat(right);
			default:
				throw new UnsupportedOperationException();
		}
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVConst tree) {
		final BitVector bitvec = tree.getValue();

		final List<T> result = new ArrayList<>(tree.getWidth());
		for (int i = 0; i < tree.getWidth(); ++i) {
			result.add(this.constBuilder.apply(bitvec.getBit(i) ? 1 : 0));
		}
		return new MultiBitsApproximation<>(result);
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVExtract tree) {
		return get(tree.getOperand()).extract(tree.getLow(), tree.getHigh());
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVIte tree) {
		final MultiBitsApproximation<T> condRes = (MultiBitsApproximation<T>) get(tree.getCondition());

		if (condRes.isConstant(1)) {
			return (MultiBitsApproximation<T>) get(tree.getThenExpr());
		} else if (condRes.isConstant(0)) {
			return (MultiBitsApproximation<T>) get(tree.getElseExpr());
		}

		final MultiBitsApproximation<T> thenRes = (MultiBitsApproximation<T>) get(tree.getThenExpr());
		final MultiBitsApproximation<T> elseRes = (MultiBitsApproximation<T>) get(tree.getElseExpr());

		final List<T> result = new ArrayList<>();
		for (int i = 0; i < tree.getWidth(); ++i) {
			result.add((T) condRes.get(0).and(thenRes.get(i))
					.xorM(condRes.get(0).not().and(elseRes.get(i))));
		}
		return new MultiBitsApproximation<>(result);
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVUnary tree) {
		final MultiBitsApproximation<T> opRes = (MultiBitsApproximation<T>) get(tree.getOperand());

		if (tree.getKind() == BVUnaryOp.NOT) {
			return new MultiBitsApproximation<>(not(opRes.toList()));
		} else {
			assert tree.getKind() == BVUnaryOp.NEG;
			return new MultiBitsApproximation<>(neg(opRes.toList()));
		}
	}



	@Override
	public MultiBitsApproximation<T> visit(final BVVar tree) {
		return this.variableApproximations.get(tree);
	}



	private List<T> getEqualsConstantList(final MultiBitsApproximation<T> v) {
		final int cap = 32 - Integer.numberOfLeadingZeros(v.bitWidth() - 1);

		final List<T> result = new ArrayList<>();
		result.add((T) v.get(0).not());
		result.add((T) v.get(0));

		for (int i = 1; i < cap; ++i) {
			final int sz = result.size();
			for (int j = 0; j < sz; ++j) {
				result.add((T) result.get(j).and(v.get(i)));
				result.set(j, (T) result.get(j).andM(v.get(i).not()));
			}
		}

		return result;
	}



	private void constShift(final List<T> result, final MultiBitsApproximation<T> left,
			final long right, final T fill) {

		for (int i = 0; i < left.bitWidth(); ++i) {
			if (i >= right && i - right < left.bitWidth()) {
				result.add(left.get((int) (i - right)));
			} else {
				result.add(fill);
			}
		}
	}



	private static <T extends Approximation> List<T> add(final List<T> left, final List<T> right) {
		final List<T> result = new ArrayList<>();
		result.add((T) left.get(0).xor(right.get(0)));

		T carry = (T) left.get(0).and(right.get(0));
		for (final Iterator<T> lit = left.listIterator(1), rit = right.listIterator(1);
				lit.hasNext() && rit.hasNext(); ) {
			final T l = lit.next();
			final T r = rit.next();
			final T t = (T) l.xor(r);
			result.add((T) t.xor(carry));
			carry = (T) carry.xorM(r).andM(t).xorM(r);
		}
		return result;
	}



	private static <T extends Approximation> List<T> sub(final List<T> left, final List<T> right) {
		final List<T> result = new ArrayList<>();
		result.add((T) left.get(0).xor(right.get(0)));

		T borrow = (T) left.get(0).not().and(right.get(0));
		for (final Iterator<T> lit = left.listIterator(1), rit = right.listIterator(1);
				lit.hasNext() && rit.hasNext(); ) {
			final T l = lit.next();
			final T r = rit.next();
			final T t = (T) l.xor(r);
			result.add((T) t.xor(borrow));
			borrow = (T) t.andM(r.xor(borrow)).xorM(borrow);
		}
		return result;
	}



	private List<T> inc(final List<T> operand) {
		final List<T> result = new ArrayList<>();
		result.add((T) operand.get(0).not());

		T carry = operand.get(0);
		for (final Iterator<T> it = operand.listIterator(1); it.hasNext(); ) {
			final T o = it.next();
			result.add((T) o.xor(carry));
			carry = (T) carry.and(o);
		}
		return result;
	}



	private List<T> dec(final List<T> operand) {
		final List<T> right = new ArrayList<>();
		right.add(this.constBuilder.apply(1));
		for (int i = 1; i < operand.size(); ++i) {
			right.add(this.constBuilder.apply(0));
		}
		return sub(operand, right);
	}



	private static <T extends Approximation> List<T> not(final List<T> operand) {
		final List<T> result = new ArrayList<>();
		for (final Iterator<T> it = operand.iterator(); it.hasNext(); ) {
			result.add((T) it.next().not());
		}
		return result;
	}



	private List<T> neg(final List<T> operand) {
		return inc(not(operand));
	}



	private Pair<List<T>, List<T>> unsignedDivide(final List<T> dividend, final List<T> divisor) {

		// Divide using non-restoring division
		final int n = dividend.size();
		if (divisor.size() != n) {
			throw new IllegalArgumentException();
		}

		// Apparently the remainder needs one extra bit
		final T zero = this.constBuilder.apply(0);
		final List<T> filledDivisor = new ArrayList<>(divisor);
		filledDivisor.add(zero);

		final List<T> posXorNegFilledDivisor = neg(filledDivisor);
		// Unroll first iteration to allow modifying operations in loop
		posXorNegFilledDivisor.set(0, (T) posXorNegFilledDivisor.get(0).xor(filledDivisor.get(0)));
		for (int i = 1; i < posXorNegFilledDivisor.size(); ++i) {
			posXorNegFilledDivisor.set(i, (T) posXorNegFilledDivisor.get(i).xorM(filledDivisor.get(i)));
		}

		final LinkedList<T> quotient = new LinkedList<>(dividend);
		List<T> remainder = new ArrayList<>();

		// Unroll first iteration because sign bit of remainder is always 0

		T borrow = quotient.removeLast();
		remainder.add((T) borrow.xor(divisor.get(0)));
		borrow = (T) borrow.not().and(divisor.get(0));
		for (int i = 1; i < n; ++i) {
			final T bit = (T) borrow.xor(divisor.get(i));
			remainder.add(bit);
			borrow = (T) borrow.xorM(divisor.get(i).and(bit));
		}
		remainder.add(borrow);

		quotient.addFirst((T) borrow.not());

		for (int i = 1; i < n; ++i) {
			final T sign = remainder.get(remainder.size() - 1);
			remainder.add(0, quotient.removeLast());
			remainder.remove(remainder.size() - 1);

			// (ite sign negFilledDivisor filledDivisor)
			final List<T> subtrahend = new ArrayList<>();
			for (int j = 0; j < filledDivisor.size() - 1; ++j) {
				subtrahend.add((T) posXorNegFilledDivisor.get(j).and(sign).xorM(filledDivisor.get(j)));
			}
			subtrahend.add(sign);

			remainder = sub(remainder, subtrahend);

			quotient.addFirst((T) remainder.get(remainder.size() - 1).not());
		}

		{
			final T sign = remainder.remove(remainder.size() - 1);
			final List<T> oneRem = add(remainder, divisor);

			// remainder = sign * oneRem ^ !sign * remainder
			for (int j = 0; j < n; ++j) {
				remainder.set(j, (T) oneRem.get(j).andM(sign).xorM(remainder.get(j).andM(sign.not())));
			}
		}

		return new Pair(quotient, remainder);
	}



	private Pair<List<T>, List<T>> signedDivide(final List<T> dividend, final List<T> divisor) {
		final List<T> negDividend = neg(dividend);
		final List<T> negDivisor = neg(divisor);

		final T signN = dividend.get(dividend.size() - 1);
		final T signD = divisor.get(divisor.size() - 1);

		final T notSignN = (T) signN.not();
		final T notSignD = (T) signD.not();

		// Select dividend or negated dividend based on sign bit
		final List<T> prepDividend = new ArrayList<>();
		for (int i = 0; i < dividend.size(); ++i) {
			// (ite notSignN negDividend(i) dividend(i))
			prepDividend.add((T) dividend.get(i)
					.xor(negDividend.get(i)).andM(notSignN).xorM(negDividend.get(i)));
		}

		// Select divisor or negated divisor based on sign bit
		final List<T> prepDivisor = new ArrayList<>();
		for (int i = 0; i < divisor.size(); ++i) {
			// (ite notSignD negDivisor(i) divisor(i))
			prepDivisor.add((T) divisor.get(i)
					.xor(negDivisor.get(i)).andM(notSignD).xorM(negDivisor.get(i)));
		}

		final Pair<List<T>, List<T>> ures = unsignedDivide(prepDividend, prepDivisor);

		final List<T> negUQuot = neg(ures.getFirst());
		final List<T> negURem = neg(ures.getSecond());

		final List<T> squot = new ArrayList<>();
		final List<T> srem = new ArrayList<>();

		// Select result based on sign
		final T factor1 = (T) notSignD.xor(signN);
		final T factor2 = (T) signD.xor(signN);

		// Unroll first iteration to allow modifying operations in loop
		// (ite (= signN signD) ures(1)(0) negUQuot(0))
		squot.add((T) factor1.and(ures.getFirst().get(0))
				.xorM(factor2.and(negUQuot.get(0))));

		// (ite signN negURem(0) ures(2)(0))
		srem.add((T) ures.getSecond().get(0)
				.xor(negURem.get(0)).andM(signN).xorM(ures.getSecond().get(0)));

		for (int i = 1; i < negUQuot.size(); ++i) {
			// (ite (= signN signD) ures(1)(i) negUQuot(i))
			squot.add((T) ures.getFirst().get(i).andM(factor1)
					.xorM(negUQuot.get(i).andM(factor2)));

			// (ite signN negURem(i) ures(2)(i))
			srem.add((T) negURem.get(i)
					.xorM(ures.getSecond().get(i)).andM(signN).xorM(ures.getSecond().get(i)));
		}

		return new Pair<>(squot, srem);
	}



	private MultiBitsApproximation<T> signedModulo(final List<T> dividend, final List<T> divisor) {
		final List<T> negDividend = neg(dividend);
		final List<T> negDivisor = neg(divisor);

		final T signN = dividend.get(dividend.size() - 1);
		final T signD = divisor.get(divisor.size() - 1);

		final T notSignN = (T) signN.not();
		final T notSignD = (T) signD.not();

		final Pair<List<T>, List<T>> res00 = unsignedDivide(dividend, divisor);
		final Pair<List<T>, List<T>> res01 = unsignedDivide(dividend, negDivisor);
		final Pair<List<T>, List<T>> res10 = unsignedDivide(negDividend, divisor);
		final Pair<List<T>, List<T>> res11 = unsignedDivide(negDividend, negDivisor);

		T rem01Zero = (T) res01.getSecond().get(0).not();
		for (int i = 1; i < divisor.size(); ++i) {
			rem01Zero = (T) rem01Zero.and(res01.getSecond().get(i).not());
		}

		T rem10Zero = (T) res10.getSecond().get(0).not();
		for (int i = 1; i < divisor.size(); ++i) {
			rem10Zero = (T) rem10Zero.and(res10.getSecond().get(i).not());
		}

		final List<T> sRem01 = add(divisor, res01.getSecond());
		final List<T> sRem10 = sub(divisor, res10.getSecond());
		final List<T> sRem11 = neg(res11.getSecond());

		// Modulus:
		// !signN * !signD * rem00 ^
		// !signN * signD  * (sRem01 ^ (sRem01 * rem01Zero)) ^
		// signN  * !signD * (sRem10 ^ (sRem10 * rem10Zero)) ^
		// signN  * signD  * sRem11

		final List<T> modulus = new ArrayList<>();
		for (int i = 0; i < dividend.size(); ++i) {
			modulus.add((T)
					notSignN.and(notSignD).and(res00.getSecond().get(i)).xor(
					notSignN.and(signD).and(sRem01.get(i).xor(sRem01.get(i).and(rem01Zero))).xor(
					signN.and(notSignD).and(sRem10.get(i).xor(sRem10.get(i).and(rem10Zero))).xor(
					signN.and(signD).and(sRem11.get(i))))));
		}

		return new MultiBitsApproximation<>(modulus);
	}



	private Approximation distinct(final List<T> left, final List<T> right) {
		Approximation result = left.get(0).xor(right.get(0));
		for (int i = 1; i < left.size(); ++i) {
			final Approximation curDis = left.get(i).xor(right.get(i));
			result = result.xor(curDis).xorM(result.and(curDis));
		}
		return result;
	}



	private Approximation unsignedLessThan(final List<T> left, final List<T> right) {
		Approximation result = left.get(0).not().and(right.get(0));
		for (int i = 1; i < left.size(); ++i) {
			result = right.get(i).xor(result).andM(left.get(i).xor(result)).xorM(right.get(i));
		}
		return result;
	}



	private Approximation unsignedLessEquals(final List<T> left, final List<T> right) {
		Approximation result = left.get(0).not().xor(right.get(0));
		for (int i = 1; i < left.size(); ++i) {
			result = result.andM(left.get(i).not().xor(right.get(i)));
		}
		return result.xorM(unsignedLessThan(left, right));
	}



	private Approximation signedLessThan(final List<T> left, final List<T> right) {
		Approximation result = left.get(0).not().and(right.get(0));
		for (int i = 1; i < left.size() - 1; ++i) {
			result = right.get(i).xor(result).andM(left.get(i).xor(result)).xorM(right.get(i));
		}
		result = result.xorM(
				right.get(right.size() - 1).xor(left.get(left.size() - 1))
				.andM(result.xor(left.get(left.size() - 1))));
		return result;
	}



	private Approximation signedLessEquals(final List<T> left, final List<T> right) {
		Approximation result = left.get(0).not().xor(right.get(0));
		for (int i = 1; i < left.size(); ++i) {
			result = result.andM(left.get(i).not().xor(right.get(i)));
		}
		return result.xorM(signedLessThan(left, right));
	}
}

