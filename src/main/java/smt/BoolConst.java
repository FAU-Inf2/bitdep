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


public class BoolConst implements BoolAst {

	private final boolean value;



	public BoolConst(final boolean value) {
		this.value = value;
	}



	public boolean getValue() {
		return this.value;
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		return this.getValue();
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return Boolean.hashCode(this.value);
	}



	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BoolConst && this.value == ((BoolConst) obj).getValue();
	}



	@Override
	public String toString() {
		return Boolean.toString(this.value);
	}



	public static boolean isTrue(final BoolAst tree) {
		return tree instanceof BoolConst && ((BoolConst) tree).value;
	}



	public static boolean isFalse(final BoolAst tree) {
		return tree instanceof BoolConst && !((BoolConst) tree).value;
	}
}

