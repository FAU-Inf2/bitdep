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



public class BVConst implements BVAst {

	private final BitVector value;



	public BVConst(final BitVector value) {
		this.value = value;
	}



	public BVConst(final int width, final long value) {
		this.value = new BitVector(width, value);
	}



	public BitVector getValue() {
		return this.value;
	}



	@Override
	public int getWidth() {
		return this.value.getWidth();
	}



	@Override
	public BitVector eval(final Map<String, Ast> assignments) {
		return this.getValue();
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return this.getValue().hashCode();
	}



	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BVConst && this.value.equals(((BVConst) obj).getValue());
	}



	@Override
	public String toString() {
		if ((this.getWidth() & 0x7) == 0) {
			return "#x" + this.value.toHexadecimalString();
		} else {
			return "#b" + this.value.toBinaryString();
		}
	}
}

