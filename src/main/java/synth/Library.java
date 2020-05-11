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
package synth;

import java.util.AbstractList;
import java.util.List;



public class Library extends AbstractList<LibraryFunction> {

	private final LibraryFunction[] components;
	private final int[][] costs;



	private Library(final LibraryFunction[] components) {
		this(components, null);
	}



	private Library(final LibraryFunction[] components, final int[][] costs) {
		this.components = components;
		this.costs = costs;
	}



	@Override
	public int size() {
		return this.components.length;
	}



	@Override
	public LibraryFunction get(final int index) {
		return this.components[index];
	}



	public boolean hasUniformCosts() {
		return this.costs == null;
	}



	public int getCost(final int operatorIndex, final int operandIndex) {
		if (this.costs == null) {
			return 1;
		} else {
			return this.costs[operatorIndex][operandIndex];
		}
	}



	public int getCost(final int operatorIndex) {
		if (this.costs == null) {
			return 1;
		} else {
			return this.costs[operatorIndex][operatorIndex]; // TODO: What about Inputs?
		}
	}



	public int getMaximalCosts() {
		if (this.costs == null) {
			return 1;
		}

		int maxCosts = 0;
		for (int i = 0; i < this.costs.length; ++i) {
			for (int j = 0; j < this.costs.length; ++j) {
				maxCosts = Math.max(maxCosts, this.costs[i][j]);
			}
		}
		return maxCosts;
	}



	public int getMaximalSizeForCostLimit(final int costLimit) {
		if (this.costs == null) {
			return Math.min(costLimit, this.components.length);
		}

		// Find minimal costs per component
		final int[] minimalCosts = new int[this.components.length];
		for (int i = 0; i < this.components.length; ++i) {
			// TODO: If getCost is changed, change the following access(es) too:
			minimalCosts[i] = this.costs[i][i];
		}

		// Greedily select components with smallest minimalCosts
		final boolean[] used = new boolean[this.components.length];
		int accum = 0;
		int result = 0;
		while (accum < costLimit) {
			int idx = -1;
			for (int i = 0; i < this.components.length; ++i) {
				if (!used[i] && (idx < 0 || minimalCosts[idx] > minimalCosts[i])) {
					idx = i;
				}
			}
			if (idx < 0) {
				break;
			}
			used[idx] = true;
			accum += minimalCosts[idx];
			result += 1;
		}
		if (accum > costLimit) {
			result -= 1;
		}
		return result;
	}



	int getMaximalNumberOfInputs() {
		int result = 0;
		for (final LibraryFunction libFunc : this.components) {
			result = Math.max(result, libFunc.getNumberOfInputs());
		}
		return result;
	}



	public static Library of(final LibraryFunction... components) {
		return new Library(components);
	}



	public static Library of(final List<LibraryFunction> components) {
		return new Library(components.toArray(new LibraryFunction[components.size()]));
	}



	public static Library of(final List<LibraryFunction> components, final int[][] costs) {
		return new Library(components.toArray(new LibraryFunction[components.size()]), costs);
	}
}

