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

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;


public class BoolBinBV implements BoolAst {

	private final BoolBVOp opKind;
	private final BVAst left;
	private final BVAst right;



	public BoolBinBV(final BoolBVOp opKind, final BVAst left, final BVAst right) {
		this.opKind = opKind;
		this.left = left;
		this.right = right;

		if (this.left.getWidth() != this.right.getWidth()) {
			throw new BitWidthMismatchException();
		}
	}



	public BoolBVOp getKind() {
		return this.opKind;
	}



	public BVAst getLeft() {
		return this.left;
	}



	public BVAst getRight() {
		return this.right;
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		final BitVector leftResult = this.left.eval(assignments);
		final BitVector rightResult = this.right.eval(assignments);

		switch (this.opKind) {
			case EQUALS:
				return leftResult.equals(rightResult);

			case DISTINCT:
				return !leftResult.equals(rightResult);

			case UGT:
				return leftResult.ugt(rightResult);

			case UGE:
				return leftResult.uge(rightResult);

			case ULT:
				return leftResult.ult(rightResult);

			case ULE:
				return leftResult.ule(rightResult);

			case SGT:
				return leftResult.sgt(rightResult);

			case SGE:
				return leftResult.sge(rightResult);

			case SLT:
				return leftResult.slt(rightResult);

			case SLE:
				return leftResult.sle(rightResult);

			default:
				throw new IllegalStateException();
		}
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return Objects.hash(this.opKind, this.left, this.right);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BoolBinBV) {
			final BoolBinBV other = (BoolBinBV) obj;
			return this.opKind == other.opKind
					&& this.left.equals(other.left)
					&& this.right.equals(other.right);
		}
		return false;
	}



	@Override
	public String toString() {
		return new StringJoiner(" ", "(", ")")
				.add(this.opKind.toString())
				.add(this.left.toString())
				.add(this.right.toString())
				.toString();
	}
}

