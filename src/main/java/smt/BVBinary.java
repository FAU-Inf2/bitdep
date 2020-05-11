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



public class BVBinary implements BVAst {

	private final int width;
	private final BVBinOp opKind;
	private final BVAst left;
	private final BVAst right;
	private final int hashCode;



	public BVBinary(final BVBinOp opKind, final BVAst left, final BVAst right) {
		if (opKind == BVBinOp.CONCAT) {
			this.width = left.getWidth() + right.getWidth();
		} else {
			if (left.getWidth() != right.getWidth()) {
				throw new BitWidthMismatchException();
			}
			this.width = left.getWidth();
		}
		this.opKind = opKind;
		this.left = left;
		this.right = right;

		this.hashCode = Objects.hash(this.width, this.opKind, this.left, this.right);
	}



	@Override
	public int getWidth() {
		return this.width;
	}



	public BVBinOp getKind() {
		return this.opKind;
	}



	public BVAst getLeft() {
		return this.left;
	}



	public BVAst getRight() {
		return this.right;
	}



	@Override
	public BitVector eval(final Map<String, Ast> assignments) {
		final BitVector leftResult = this.left.eval(assignments);
		final BitVector rightResult = this.right.eval(assignments);

		switch (this.opKind) {
			case PLUS:
				return leftResult.add(rightResult);

			case MINUS:
				return leftResult.sub(rightResult);

			case MUL:
				return leftResult.mul(rightResult);

			case SDIV:
				return leftResult.sdiv(rightResult);

			case UDIV:
				return leftResult.udiv(rightResult);

			case SREM:
				return leftResult.srem(rightResult);

			case UREM:
				return leftResult.urem(rightResult);

			case SMOD:
				return leftResult.smod(rightResult);

			case UMOD:
				return leftResult.umod(rightResult);

			case AND:
				return leftResult.and(rightResult);

			case OR:
				return leftResult.or(rightResult);

			case XOR:
				return leftResult.xor(rightResult);

			case SHL:
				return leftResult.shl(rightResult);

			case ASHR:
				return leftResult.ashr(rightResult);

			case LSHR:
				return leftResult.lshr(rightResult);

			case ROL:
				return leftResult.rol(rightResult);

			case ROR:
				return leftResult.ror(rightResult);

			case CONCAT:
				return leftResult.concat(rightResult);

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
		return this.hashCode;
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BVBinary) {
			final BVBinary other = (BVBinary) obj;

			return this.width == other.width
					&& this.opKind == other.opKind
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

