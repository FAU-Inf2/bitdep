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



public class BVExtract implements BVAst {

	private final int low;
	private final int high;
	private final BVAst operand;



	public BVExtract(final int low, final int high, final BVAst operand) {
		if (low < 0 || high < low || high >= operand.getWidth()) {
			throw new IllegalArgumentException();
		}
		this.low = low;
		this.high = high;
		this.operand = operand;
	}



	public int getLow() {
		return this.low;
	}



	public int getHigh() {
		return this.high;
	}



	public BVAst getOperand() {
		return this.operand;
	}



	@Override
	public int getWidth() {
		return this.high - this.low + 1;
	}



	@Override
	public BitVector eval(final Map<String, Ast> assignments) {
		return this.operand.eval(assignments).extract(this.low, this.high);
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return Objects.hash(this.low, this.high, this.operand);
	}



	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof BVExtract) {
			final BVExtract other = (BVExtract) obj;
			return this.low == other.low
					&& this.high == other.high
					&& this.operand.equals(other.operand);
		}
		return false;
	}



	@Override
	public String toString() {
		return new StringBuilder()
				.append("((_ extract ")
				.append(this.high)
				.append(' ')
				.append(this.low)
				.append(") ")
				.append(this.operand)
				.append(')')
				.toString();
	}
}

