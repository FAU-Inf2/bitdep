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


public class BoolBin implements BoolAst {

	private final BoolBinOp opKind;
	private final BoolAst left;
	private final BoolAst right;



	public BoolBin(final BoolBinOp opKind, final BoolAst left, final BoolAst right) {
		this.opKind = opKind;
		this.left = left;
		this.right= right;
	}



	public BoolBinOp getKind() {
		return this.opKind;
	}



	public BoolAst getLeft() {
		return this.left;
	}



	public BoolAst getRight() {
		return this.right;
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		switch (this.opKind) {
			case EQUALS:
				return this.left.eval(assignments) == this.right.eval(assignments);

			case DISTINCT:
				return this.left.eval(assignments) != this.right.eval(assignments);

			case IMPLIES:
				return !this.left.eval(assignments) || this.right.eval(assignments);

			case AND:
				return this.left.eval(assignments) && this.right.eval(assignments);

			case OR:
				return this.left.eval(assignments) || this.right.eval(assignments);

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
		if (obj instanceof BoolBin) {
			final BoolBin other = (BoolBin) obj;
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

