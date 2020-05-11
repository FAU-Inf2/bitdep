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



public class BVIte implements BVAst {

	private final int width;
	private final BoolAst condition;
	private final BVAst thenExpr;
	private final BVAst elseExpr;
	private final int hashCode;



	public BVIte(final BoolAst condition, final BVAst thenExpr, final BVAst elseExpr) {
		if (thenExpr.getWidth() != elseExpr.getWidth()) {
			throw new BitWidthMismatchException();
		}

		this.width = thenExpr.getWidth();
		this.condition = condition;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;

		this.hashCode = Objects.hash(this.width, this.condition, this.thenExpr, this.elseExpr);
	}



	@Override
	public int getWidth() {
		return this.width;
	}



	public BoolAst getCondition() {
		return this.condition;
	}



	public BVAst getThenExpr() {
		return this.thenExpr;
	}



	public BVAst getElseExpr() {
		return this.elseExpr;
	}



	@Override
	public BitVector eval(final Map<String, Ast> assignments) {
		if (this.condition.eval(assignments)) {
			return this.thenExpr.eval(assignments);
		} else {
			return this.elseExpr.eval(assignments);
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
		if (obj instanceof BVIte) {
			final BVIte other = (BVIte) obj;
			return this.width == other.width
					&& this.condition.equals(other.condition)
					&& this.thenExpr.equals(other.thenExpr)
					&& this.elseExpr.equals(other.elseExpr);
		}
		return false;
	}



	@Override
	public String toString() {
		return new StringJoiner(" ", "(ite ", ")")
				.add(this.condition.toString())
				.add(this.thenExpr.toString())
				.add(this.elseExpr.toString())
				.toString();
	}
}

