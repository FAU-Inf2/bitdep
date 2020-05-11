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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;



public class BoolAllDifferent implements BoolAst {

	private final List<BVAst> operands;



	public BoolAllDifferent(final BVAst ... operands) {
		this.operands = Arrays.asList(operands);
		if (this.operands.size() <= 1) {
			throw new IllegalArgumentException();
		}
	}



	public BoolAllDifferent(final List<BVAst> operands) {
		this.operands = new ArrayList<>(operands);
		if (this.operands.size() <= 1) {
			throw new IllegalArgumentException();
		}
	}



	public List<BVAst> getOperands() {
		return Collections.unmodifiableList(this.operands);
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		for (int i = 0; i < this.operands.size(); ++i) {
			final BitVector ithRes = this.operands.get(i).eval(assignments);

			for (int j = i + 1; j < this.operands.size(); ++j) {
				final BitVector jthRes = this.operands.get(j).eval(assignments);
				
				if (ithRes.equals(jthRes)) {
					return false;
				}
			}
		}

		return true;
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return this.operands.hashCode();
	}



	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BoolAllDifferent
				&& this.operands.equals(((BoolAllDifferent) obj).operands);
	}



	@Override
	public String toString() {
		final StringJoiner joiner = new StringJoiner(" ", "(distinct ", ")");
		for (final BVAst operand : this.operands) {
			joiner.add(operand.toString());
		}
		return joiner.toString();
	}
}

