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


public class BoolVar implements BoolAst {

	private final String name;



	public BoolVar(final String name) {
		this.name = name;
	}



	public String getName() {
		return this.name;
	}



	@Override
	public boolean eval(final Map<String, Ast> assignments) {
		if (!assignments.containsKey(this.name)) {
			throw new IllegalStateException();
		}

		return ((BoolConst) assignments.get(this.name)).getValue();
	}



	@Override
	public final <R> R accept(final TreeTransformer<R> transformer) {
		return transformer.visit(this);
	}



	@Override
	public int hashCode() {
		return this.name.hashCode();
	}



	@Override
	public boolean equals(final Object obj) {
		return obj instanceof BoolVar && this.name.equals(((BoolVar) obj).name);
	}



	@Override
	public String toString() {
		return this.name;
	}
}

