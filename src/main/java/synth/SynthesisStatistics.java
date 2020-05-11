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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import smt.BitVector;



public class SynthesisStatistics {

	private final int numberOfIterations;
	private final List<List<BitVector>> examples;
	private final List<int[]> tempPrograms;



	SynthesisStatistics(final int numberOfIterations, final List<List<BitVector>> examples,
			final List<int[]> tempPrograms) {
		this.numberOfIterations = numberOfIterations;
		this.examples = examples;
		this.tempPrograms = tempPrograms;
	}



	public int getNumberOfIterations() {
		return this.numberOfIterations;
	}



	public List<List<BitVector>> getExamples() {
		return Collections.unmodifiableList(this.examples);
	}



	@Override
	public String toString() {
		final StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("iterations: ").append(this.numberOfIterations).append('\n');
		resultBuilder.append("examples:\n");
		for (final List<BitVector> example : this.examples) {
			resultBuilder.append("- ");
			boolean first = true;
			for (final BitVector ev : example) {
				if (!first) {
					resultBuilder.append(", ");
				}
				resultBuilder.append(ev);
				first = false;
			}
			resultBuilder.append('\n');
		}
		resultBuilder.append("programs:\n");
		for (final int[] program : this.tempPrograms) {
			resultBuilder.append("- ").append(Arrays.toString(program)).append('\n');
		}
		return resultBuilder.toString();
	}
}

