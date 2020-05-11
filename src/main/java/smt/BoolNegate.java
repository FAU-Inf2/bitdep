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


public class BoolNegate implements BoolAst {

	private final BoolAst operand;



	public BoolNegate(final BoolAst operand) {
		this.operand = operand;
	}



	public BoolAst getOperand() {
		return this.operand;
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		return !this.operand.eval(assignments);
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return -this.operand.hashCode();
	}



	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BoolNegate && this.operand.equals(((BoolNegate) obj).getOperand());
	}



	@Override
	public String toString() {
		return "(not " + this.operand + ")";
	}
}

