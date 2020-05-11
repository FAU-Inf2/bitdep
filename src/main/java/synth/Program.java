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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;

import smt.BitVector;
import smt.Builder;
import smt.BVAst;



public class Program implements Iterable<Program.Stmt> {

	private final List<LibraryFunction> library;
	private final int numberOfInputs;
	private final int numberOfStatements;
	private final int[] program;
	private final List<BitVector> auxiliaryVariables;



	/** Class used for iterating over program statements. */
	public static final class Stmt {

		private final LibraryFunction function;
		private final List<Integer> arguments;
		private final List<BitVector> auxiliaryVariables;


		Stmt(final LibraryFunction function, final List<Integer> arguments) {
			this(function, arguments, Collections.emptyList());
		}


		Stmt(final LibraryFunction function, final List<Integer> arguments,
				final List<BitVector> auxiliaryVariables) {
			this.function = function;
			this.arguments = arguments;
			this.auxiliaryVariables = auxiliaryVariables;
		}


		public LibraryFunction getFunction() {
			return this.function;
		}


		public List<Integer> getArguments() {
			return this.arguments;
		}


		public List<BitVector> getAuxiliaryVariables() {
			return this.auxiliaryVariables;
		}
	}



	Program(final List<LibraryFunction> library, final int numberOfInputs,
			final int numberOfStatements, final int[] program,
			final List<BitVector> auxiliaryVariables) {
		this.library = library;
		this.numberOfInputs = numberOfInputs;
		this.numberOfStatements = numberOfStatements;
		this.program = program;
		this.auxiliaryVariables = auxiliaryVariables;
	}



	public int getNumberOfInputs() {
		return this.numberOfInputs;
	}



	public int getNumberOfStatements() {
		return this.numberOfStatements;
	}



	public BitVector execute(final List<BitVector> inputs) {
		if (inputs.size() != this.numberOfInputs) {
			throw new IllegalArgumentException();
		}

		final NavigableMap<Integer, Integer> orderMap = getOrderMap();
		final int[] argumentOffsets = accumulateArgumentCounts();

		final Map<Integer, List<BitVector>> auxMap = getAuxiliaryVariableMap();
		
		final NavigableMap<Integer, BVAst> results = new TreeMap<>();
		for (int i = 0; i < inputs.size(); ++i) {
			results.put(i, Builder.mkBVConst(inputs.get(i)));
		}

		for (Integer p = orderMap.firstKey(); p != null; p = orderMap.higherKey(p)) {
			if (p < this.numberOfInputs + this.numberOfStatements) {
				final int f = orderMap.get(p);

				final int argumentOffset = this.library.size() + argumentOffsets[f];

				final List<BVAst> arguments = new ArrayList<>();
				for (int i = 0; i < this.library.get(f).getNumberOfInputs(); ++i) {
					assert results.containsKey(this.program[argumentOffset + i]);

					arguments.add(results.get(this.program[argumentOffset + i]));
				}
				if (auxMap.containsKey(f)) {
					for (final BitVector auxVal : auxMap.get(f)) {
						arguments.add(Builder.mkBVConst(auxVal));
					}
				}

				results.put(p, this.library.get(f).getFunction().apply(arguments));
			}
		}

		return results.lastEntry().getValue().eval(Collections.emptyMap());
	}



	@Override
	public Iterator<Program.Stmt> iterator() {
		final NavigableMap<Integer, Integer> orderMap = getOrderMap();
		final Map<Integer, List<BitVector>> auxiliaryVariables = getAuxiliaryVariableMap();
		final int[] argumentOffsets = accumulateArgumentCounts();

		return new Iterator<Program.Stmt>() {
			private Integer p = orderMap.firstKey();

			@Override
			public boolean hasNext() {
				return this.p != null
						&& this.p < Program.this.numberOfInputs + Program.this.numberOfStatements;
			}

			@Override
			public Program.Stmt next() {
				final int f = orderMap.get(p);
				final LibraryFunction function = Program.this.library.get(f);
				final List<Integer> arguments = new ArrayList<>(function.getNumberOfInputs());
				final int offset = argumentOffsets[f] + Program.this.library.size();
				for (int i = 0; i < function.getNumberOfInputs(); ++i) {
					arguments.add(Program.this.program[i + offset]);
				}

				final Program.Stmt result = auxiliaryVariables.containsKey(f)
						? new Program.Stmt(function, arguments, auxiliaryVariables.get(f))
						: new Program.Stmt(function, arguments);
				this.p = orderMap.higherKey(this.p);
				return result;
			}
		};
	}



	@Override
	public String toString() {
		final NavigableMap<Integer, Integer> orderMap = getOrderMap();
		final Map<Integer, List<BitVector>> auxiliaryVariables = getAuxiliaryVariableMap();
		final int[] argumentOffsets = accumulateArgumentCounts();

		final StringBuilder builder = new StringBuilder();
		builder.append("# ").append(this.numberOfInputs).append(" inputs\n# ")
				.append(this.numberOfStatements).append(" statements\n");

		for (Integer p = orderMap.firstKey(); p != null; p = orderMap.higherKey(p)) {
			if (p < this.numberOfInputs + this.numberOfStatements) {
				final int f = orderMap.get(p);
				builder.append('v').append(p).append(" := ");
				builder.append(this.library.get(f).getName());

				if (auxiliaryVariables.containsKey(f)) {
					final StringJoiner auxVarJoiner = new StringJoiner(",", "<", ">");
					for (final BitVector auxVal : auxiliaryVariables.get(f)) {
						auxVarJoiner.add(auxVal.toString());
					}
					builder.append(auxVarJoiner);
				}

				builder.append('(');
				printArgs(builder,
						argumentOffsets[f] + this.library.size(),
						this.library.get(f).getNumberOfInputs());
				builder.append(")\n");
			}
		}

		return builder.toString();
	}



	/**
	 * Returns a map from program position to library function index.
	 */
	private NavigableMap<Integer, Integer> getOrderMap() {
		final NavigableMap<Integer, Integer> result = new TreeMap<>();
		for (int i = 0; i < this.library.size(); ++i) {
			result.put(this.program[i], i);
		}
		return result;
	}



	/**
	 * Returns a map from library function index to a list of auxiliary
	 * variable values.
	 */
	private Map<Integer, List<BitVector>> getAuxiliaryVariableMap() {
		final Map<Integer, List<BitVector>> result = new HashMap<>();
		for (int i = 0, k = 0; i < this.library.size() && k < this.auxiliaryVariables.size(); ++i) {
			final int begin = k;
			for (int j = 0; j < this.library.get(i).getNumberOfAuxiliaryVariables(); ++j) {
				k += 1;
			}
			if (k > begin) {
				result.put(i, this.auxiliaryVariables.subList(begin, k));
			}
		}
		return result;
	}



	private int[] accumulateArgumentCounts() {
		final int[] result = new int[this.library.size() + 1];

		int accumulator = 0;
		for (int i = 0; i < this.library.size(); ++i) {
			result[i] = accumulator;
			accumulator += this.library.get(i).getNumberOfInputs();
		}
		result[this.library.size()] = accumulator;

		return result;
	}



	private void printArgs(final StringBuilder builder, final int argumentOffset,
			final int argumentCount) {
		
		for (int i = 0; i < argumentCount; ++i) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append('v')
					.append(this.program[i + argumentOffset]);
		}
	}
}

