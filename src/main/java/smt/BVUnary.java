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



public class BVUnary implements BVAst {

	private final int width;
	private final BVUnaryOp opKind;
	private final BVAst operand;



	public BVUnary(final BVUnaryOp opKind, final BVAst operand) {
		this.width = operand.getWidth();
		this.opKind = opKind;
		this.operand = operand;
	}



	@Override
	public int getWidth() {
		return this.width;
	}



	public BVUnaryOp getKind() {
		return this.opKind;
	}



	public BVAst getOperand() {
		return this.operand;
	}



	@Override
	public BitVector eval(final Map<String, Ast> assignments) {
		switch (this.opKind) {
			case NOT:
				return this.operand.eval(assignments).not();

			case NEG:
				return this.operand.eval(assignments).neg();

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
		return Objects.hash(this.width, this.opKind, this.operand);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BVUnary) {
			final BVUnary other = (BVUnary) obj;
			return this.width == other.width
					&& this.opKind == other.opKind
					&& this.operand.equals(other.operand);
		}
		return false;
	}



	@Override
	public String toString() {
		return new StringJoiner(" ", "(", ")")
				.add(this.opKind.toString())
				.add(this.operand.toString())
				.toString();
	}
}

