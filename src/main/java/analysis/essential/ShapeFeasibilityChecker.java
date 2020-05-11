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
package analysis.essential;

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.Iterables;

import smt.*;
import synth.FunctionParser;
import synth.LibraryFunction;
import synth.Specification;



public class ShapeFeasibilityChecker {

	static enum PartialOrdering {
		INCOMPARABLE,
		LESS,
		EQUAL,
		GREATER;
	}


	static enum Shape {
		CONSTANT,
		LINEAR,
		ASCENDING,
		DESCENDING,
		BLOCK;

		public Shape join(final Shape other) {
			if (this == other) {
				return this;
			}
			if (this == CONSTANT) {
				return other;
			}
			if (other == CONSTANT) {
				return this;
			}
			if (this == ASCENDING && other == DESCENDING) {
				return BLOCK;
			}
			if (this == DESCENDING && other == ASCENDING) {
				return BLOCK;
			}
			if (this.ordinal() > other.ordinal()) {
				return this;
			}
			return other;
		}

		public Shape meet(final Shape other) {
			if (this == other) {
				return this;
			}
			if (this == CONSTANT) {
				return this;
			}
			if (other == CONSTANT) {
				return other;
			}
			if (this == ASCENDING && other == DESCENDING) {
				return LINEAR;
			}
			if (this == DESCENDING && other == ASCENDING) {
				return LINEAR;
			}
			if (this.ordinal() > other.ordinal()) {
				return other;
			}
			return this;
		}

		public PartialOrdering cmpTo(final Shape other) {
			if (this == other) {
				return PartialOrdering.EQUAL;
			}
			if (this == ASCENDING && other == DESCENDING) {
				return PartialOrdering.INCOMPARABLE;
			}
			if (this == DESCENDING && other == ASCENDING) {
				return PartialOrdering.INCOMPARABLE;
			}
			if (this.ordinal() > other.ordinal()) {
				return PartialOrdering.GREATER;
			}
			return PartialOrdering.LESS;
		}

		public List<Shape> getLargerShapes() {
			switch (this) {
				case CONSTANT:
					return Arrays.asList(LINEAR, ASCENDING, DESCENDING, BLOCK);
				case LINEAR:
					return Arrays.asList(ASCENDING, DESCENDING, BLOCK);
				case ASCENDING:
				case DESCENDING:
					return Collections.singletonList(BLOCK);
				default:
					return Collections.emptyList();
			}
		}
	}


	static class Op {
		Shape[] arguments;

		public Op(final Shape[] arguments) {
			this.arguments = arguments;
		}

		public Shape maximalArgument() {
			Shape result = this.arguments[0];
			for (int i = 1; i < this.arguments.length; ++i) {
				if (result.cmpTo(this.arguments[i]) == PartialOrdering.LESS) {
					result = this.arguments[i];
				}
			}
			return result;
		}

		public int getNumberOfIncomparableOrSubShapeArguments(final Shape shape) {
			int result = 0;
			for (int i = 0; i < this.arguments.length; ++i) {
				final PartialOrdering ordering = this.arguments[i].cmpTo(shape);
				if (ordering == PartialOrdering.INCOMPARABLE || ordering == PartialOrdering.LESS) {
					result += 1;
				}
			}
			return result;
		}

		public int getShapeArguments(final Shape shape) {
			int result = 0;
			for (int i = 0; i < this.arguments.length; ++i) {
				if (this.arguments[i] == shape) {
					result += 1;
				}
			}
			return result;
		}

		public int getNonShapeArguments(final Shape shape) {
			return this.arguments.length - getShapeArguments(shape);
		}
	}



	static abstract class AscDescMergerIterable implements Iterable<Op[]> {

		private Op[] initialOps;


		AscDescMergerIterable(final Op[] initialOps) {
			this.initialOps = initialOps;
		}


		public abstract Op mergeOpArgs(final Op op, final int j, final int k);


		@Override
		public Iterator<Op[]> iterator() {
			return new Iterator<Op[]>() {
				private Op[] current = Arrays.copyOf(initialOps, initialOps.length);
				private int i = 0, j = 0;

				private Op mergeOpArgs(final Op op, final int j, final int k) {
					final Shape[] newArgs = new Shape[op.arguments.length - 1];
					for (int i = 0; i < op.arguments.length; ++i) {
						if (i == j) {
							newArgs[i] = Shape.BLOCK;
						} else if (i > k) {
							newArgs[i - 1] = op.arguments[i];
						} else if (i < k) {
							newArgs[i] = op.arguments[i];
						}
					}
					return new Op(newArgs);
				}

				@Override
				public boolean hasNext() {
					return this.current != null;
				}

				@Override
				public Op[] next() {
					final Op[] result = Arrays.copyOf(this.current, this.current.length);

					boolean quit = false;
					while (!quit && i < this.current.length) {
						if (j < this.current[i].arguments.length) {
							if (this.current[i].arguments[j] == Shape.ASCENDING) {
								int k = j + 1;
								while (k < this.current[i].arguments.length
										&& this.current[i].arguments[k] != Shape.DESCENDING) {
									k += 1;
								}
								if (k < this.current[i].arguments.length) {
									this.current[i] = mergeOpArgs(this.current[i], j, k);
									quit = true;
								}
							} else if (this.current[i].arguments[j] == Shape.DESCENDING) {
								int k = j + 1;
								while (k < this.current[i].arguments.length
										&& this.current[i].arguments[k] != Shape.ASCENDING) {
									k += 1;
								}
								if (k < this.current[i].arguments.length) {
									this.current[i] = mergeOpArgs(this.current[i], j, k);
									quit = true;
								}
							}
							j += 1;
						} else {
							this.current[i] = initialOps[i];
							i += 1;
							j = 0;
						}
					}

					if (i >= this.current.length) {
						this.current = null;
					}

					return result;
				}
			};
		}
	}



	private static List<Shape> getLeafShapes(final Op[] ops, final int[][] successors,
			final Shape[] cumulShapes) {

		cumulShapes[successors[ops.length][0]] = Shape.LINEAR;
		final List<Shape> leafShapes = new ArrayList<>();

		final Queue<Integer> queue = new ArrayDeque<>();
		queue.offer(successors[ops.length][0]);

		while (!queue.isEmpty()) {
			final int cur = queue.poll();
			for (int j = 0; j < successors[cur].length; ++j) {
				if (successors[cur][j] >= 0) {
					cumulShapes[successors[cur][j]] = cumulShapes[cur].join(ops[cur].arguments[j]);
					queue.offer(successors[cur][j]);
				} else {
					leafShapes.add(cumulShapes[cur].join(ops[cur].arguments[j]));
				}
			}
		}

		return leafShapes;
	}



	static int evaluate(final Shape[] varShapes, final Op[] ops, final int[][] successors) {

		final Shape[] propagatedShapes = new Shape[ops.length];
		final List<Shape> leaves = getLeafShapes(ops, successors, propagatedShapes);

		final int[] varsOfShape = new int[4];
		for (final Shape shape : varShapes) {
			varsOfShape[shape.ordinal() - 1] += 1;
		}

		final int[] leavesOfShape = new int[4];
		for (final Shape shape : leaves) {
			leavesOfShape[shape.ordinal() - 1] += 1;
		}

		final int[] deficiencies = new int[5];
		deficiencies[0] = leavesOfShape[0] - varsOfShape[0];
		deficiencies[1] = leavesOfShape[0] + leavesOfShape[1] - varsOfShape[0] - varsOfShape[1];
		deficiencies[2] = leavesOfShape[0] + leavesOfShape[2] - varsOfShape[0] - varsOfShape[2];
		deficiencies[3] = leavesOfShape[0] + leavesOfShape[1] + leavesOfShape[2]
				- varsOfShape[0] - varsOfShape[1] - varsOfShape[2];
		deficiencies[4] = leavesOfShape[0] + leavesOfShape[1] + leavesOfShape[2] + leavesOfShape[3]
				- varsOfShape[0] - varsOfShape[1] - varsOfShape[2] - varsOfShape[3];

		int matchingNumber = 0;
		for (int i = 0; i < deficiencies.length; ++i) {
			matchingNumber = Math.max(matchingNumber, deficiencies[i]);
		}

		return matchingNumber;
	}



	private static int[][] duplicate(final int[][] array) {
		final int[][] result = new int[array.length][];
		for (int i = 0; i < array.length; ++i) {
			result[i] = Arrays.copyOf(array[i], array[i].length);
		}
		return result;
	}



	static void optimize(final Shape[] varShapes, final Op[] ops, final int[][] successors) {

		List<int[][]> solutions = new ArrayList<>();
		solutions.add(successors);

		final List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < ops.length; ++i) {
			candidates.add(i);
		}

		int numLeaves = 0;
		final List<Integer> used = new ArrayList<>();
		for (int i = 0; i < successors.length; ++i) {
			for (int j = 0; j < successors[i].length; ++j) {
				if (successors[i][j] >= 0) {
					candidates.remove(Integer.valueOf(successors[i][j]));
					numLeaves += ops[successors[i][j]].arguments.length;
				}
			}
		}
		numLeaves -= ops.length - candidates.size() + 1;

		int remainingArgs = 0;
		for (int i = 0; i < candidates.size(); ++i) {
			remainingArgs += ops[candidates.get(i)].arguments.length;
		}

		for (int i = 0; i < candidates.size(); ++i) {
			int currentBest = Integer.MAX_VALUE;

			numLeaves += ops[candidates.get(i)].arguments.length - 1;
			remainingArgs -= ops[candidates.get(i)].arguments.length;

			final List<int[][]> next = new ArrayList<>();

			for (final int[][] toExpand : solutions) {
				final Shape[] cumulShapes = new Shape[ops.length];
				getLeafShapes(ops, toExpand, cumulShapes);

				final Set<Shape> processedShapes = EnumSet.noneOf(Shape.class);
				for (int j = 0; j < cumulShapes.length; ++j) {
					if (cumulShapes[j] != null) {
						for (int k = 0; k < ops[j].arguments.length; ++k) {
							if (toExpand[j][k] < 0
									&& processedShapes.add(cumulShapes[j].join(ops[j].arguments[k]))) {
								final int[][] expanded = duplicate(toExpand);
								expanded[j][k] = candidates.get(i);

								//if (knownLeafShapes.add(getLeafShapesHashMap(ops, expanded))) {
									final int currentValue = evaluate(varShapes, ops, expanded);
									if (currentValue < currentBest) {
										currentBest = currentValue;
										if (i + 1 == candidates.size()) {
											next.clear();
										}
									}
									//if (i + 1 < candidates.size() || currentValue == currentBest) {
									//	next.add(expanded);
									//}
									if (numLeaves - currentValue + remainingArgs >= numLeaves - currentBest) {
										next.add(expanded);
									}
								//}
							}
						}
					}
				}
			}

			assert !next.isEmpty() : i + ", " + candidates.size() + ", " + solutions.size();

			solutions = next;
		}

		System.arraycopy(solutions.get(0), 0, successors, 0, successors.length);
	}



	static Iterator<int[][]> combinations(final Shape[] varShapes, final Op[] ops,
			final int[][] successors) {

		if (ops.length == 1) {
			final int[][] copy = duplicate(successors);
			copy[1][0] = 0;
			return Collections.singleton(copy).iterator();
		}

		return new Iterator<int[][]>() {
			int first = 0;
			int second = 1;
			int firstArg = 0;

			@Override
			public boolean hasNext() {
				if (this.second >= ops.length) {
					this.first += 1;
					this.second = 0;
				}
				return this.first < ops.length;
			}

			@Override
			public int[][] next() {
				if (!hasNext()) {
					throw new IllegalStateException();
				}

				final int[][] copy = duplicate(successors);
				copy[ops.length][0] = this.first;
				copy[this.first][this.firstArg] = this.second;

				this.firstArg += 1;
				if (this.firstArg >= ops[this.first].arguments.length) {
					this.firstArg = 0;
					this.second += 1;
				}
				if (this.second == this.first) {
					this.second += 1;
				}

				return copy;
			}
		};
	}



	private static Shape getShape(final MultiBitsApproximation<? extends Iterable<Integer>> approx,
			final int fromBit, final int toBit) {

		Shape result = Shape.CONSTANT;
		for (int i = 0; i < approx.bitWidth(); ++i) {
			for (final int bit : approx.get(i)) {
				if (bit >= fromBit && bit < toBit) {
					if (bit - fromBit < i) {
						result = result.join(Shape.ASCENDING);
					} else if (bit - fromBit > i) {
						result = result.join(Shape.DESCENDING);
					} else {
						result = result.join(Shape.LINEAR);
					}
				}
			}
		}
		return result;
	}



	private static MultiBitsApproximation<ZPolyUnderApproximation> getVarApproximation(
			final Specification specification) {

		final List<BVAst> inputs = new ArrayList<>();
		for (int i = 0; i < specification.getNumberOfInputs(); ++i) {
			inputs.add(Builder.mkBVVar(specification.getInputBitWidth(i), "i" + i));
		}

		final BVAst func = specification.getFunction().apply(inputs);

		return ZPolyUnderApproximation.create(func, inputs, 2);
	}



	private static Shape[] getVarShapes(final Specification specification,
			final MultiBitsApproximation<ZPolyUnderApproximation> funcUnder) {

		final List<Shape> result = new ArrayList<>();
		for (int i = 0, from = 0; i < specification.getNumberOfInputs(); ++i) {
			final int to = from + specification.getInputBitWidth(i);
			final Shape shape = getShape(funcUnder, from, to);
			if (shape != Shape.CONSTANT) {
				result.add(shape);
			}
			from = to;
		}

		return result.toArray(new Shape[result.size()]);
	}



	private static Op getOp(final LibraryFunction libFunc) {
		final List<BVAst> inputs = new ArrayList<>();
		for (int i = 0; i < libFunc.getNumberOfInputs(); ++i) {
			inputs.add(Builder.mkBVVar(libFunc.getInputBitWidth(i), "i" + i));
		}

		final BVAst func = libFunc.getFunction().apply(inputs);

		final MultiBitsApproximation<ZPolyOverApproximation> funcOver
				= ZPolyOverApproximation.create(func, inputs);

		final List<Shape> arguments = new ArrayList<>();
		for (int i = 0, from = 0; i < libFunc.getNumberOfInputs(); ++i) {
			final int to = from + libFunc.getInputBitWidth(i);
			final Shape shape = getShape(funcOver, from, to);
			if (shape != Shape.CONSTANT) {
				arguments.add(shape);
			}
			from = to;
		}

		return new Op(arguments.toArray(new Shape[arguments.size()]));
	}



	private static Op[] getOps(final List<LibraryFunction> library) {
		final List<Op> result = new ArrayList<>();

		for (final LibraryFunction libFunc : library) {
			final Op op = getOp(libFunc);
			if (op.arguments.length > 0) {
				result.add(op);
			}
		}

		return result.toArray(new Op[result.size()]);
	}



	static Iterable<Op[]> getAscDescMerged(final Shape[] varShapes, final Op[] initialOps) {
		boolean varsContainBlock = false;
		for (final Shape shape : varShapes) {
			varsContainBlock |= shape == Shape.BLOCK;
		}

		if (!varsContainBlock) {
			return Collections.singletonList(initialOps);
		}

		boolean opsContainBlock = false;
		for (final Op op : initialOps) {
			for (final Shape shape : op.arguments) {
				opsContainBlock |= shape == Shape.BLOCK;
			}
		}

		if (opsContainBlock) {
			return Collections.singletonList(initialOps);
		}

		return new AscDescMergerIterable(initialOps) {
			@Override
			public Op mergeOpArgs(final Op op, final int j, final int k) {
				final Shape[] newArgs = new Shape[op.arguments.length - 1];
				for (int i = 0; i < op.arguments.length; ++i) {
					if (i == j) {
						newArgs[i] = Shape.BLOCK;
					} else if (i > k) {
						newArgs[i - 1] = op.arguments[i];
					} else if (i < k) {
						newArgs[i] = op.arguments[i];
					}
				}
				return new Op(newArgs);
			}
		};
	}



	public static boolean oneCheck(final MultiBitsApproximation<ZPolyUnderApproximation> specApprox,
			final List<LibraryFunction> library) {

		// Check condition: All inputs and outputs have the same bit width
		final int bitWidth = specApprox.bitWidth();

		for (final LibraryFunction libFunc : library) {
			if (libFunc.getOutputBitWidth() != bitWidth) {
				return true;
			}
			for (int i = 0; i < libFunc.getNumberOfInputs(); ++i) {
				if (libFunc.getInputBitWidth(i) != bitWidth) {
					return true;
				}
			}
		}

		final boolean[] destination = new boolean[bitWidth];
		boolean hasOne = false;
		for (int i = 0; i < specApprox.bitWidth(); ++i) {
			hasOne |= destination[i] = specApprox.get(i).containsOne();
		}

		if (!hasOne) {
			return true;
		}

		boolean[][] joined = new boolean[bitWidth][bitWidth];
		final List<boolean[]> starts = new ArrayList<>();

		for (final LibraryFunction libFunc : library) {
			// Copy joined into nextJoined
			final boolean[][] nextJoined = new boolean[bitWidth][];
			for (int i = 0; i < bitWidth; ++i) {
				nextJoined[i] = Arrays.copyOf(joined[i], bitWidth);
			}

			final List<BVAst> inputs = new ArrayList<>();
			for (int i = 0; i < libFunc.getNumberOfInputs(); ++i) {
				inputs.add(Builder.mkBVVar(libFunc.getInputBitWidth(i), "i" + i));
			}

			final boolean[] currentStart = new boolean[bitWidth];
			boolean hasFuncOne = false;

			for (int i = 0; i < bitWidth; ++i) {
				// Set i-th inputs to 0
				final List<BVAst> zeroedInputs = new ArrayList<>();
				for (final BVAst input : inputs) {
					zeroedInputs.add(Builder.mkAnd(input,
							Builder.mkBVConst(new BitVector(bitWidth, 1).shl(new BitVector(bitWidth, i)).not())));
				}

				final BVAst func = libFunc.getFunction().apply(zeroedInputs);

				final MultiBitsApproximation<ZPolyOverApproximation> funcOver
						= ZPolyOverApproximation.create(func, inputs);

				if (funcOver.get(i).containsOne()) {
					hasFuncOne = true;
					currentStart[i] = true;
				}

				nextJoined[i][i] = true;
				if (!inputs.isEmpty()) {
					for (final int bit : funcOver.get(i)) {
						nextJoined[i][bit % bitWidth] = true;
						if (bit % bitWidth != i) {
							for (int j = 0; j < bitWidth; ++j) {
								if (joined[bit % bitWidth][j]) {
									nextJoined[i][j] = true;
									for (int k = 0; k < bitWidth; ++k) {
										if (joined[k][i]) {
											nextJoined[k][j] = true;
										}
									}
								} else if (joined[j][i]) {
									nextJoined[j][bit % bitWidth] = true;
								}
							}
						}
					}
				}
			}

			if (hasFuncOne) {
				starts.add(currentStart);
			}

			joined = nextJoined;
		}

		if (starts.isEmpty()) {
			return false;
		}

		for (int i = 0; i < joined.length; ++i) {
			if (destination[i]) {
				boolean found = false;
				j_loop:
				for (int j = 0; j < joined[i].length; ++j) {
					for (final boolean[] start : starts) {
						if (joined[i][j] && start[j]) {
							found = true;
							break j_loop;
						}
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}



	public static boolean isUnsat(final Specification specification,
			final List<LibraryFunction> library) {

		final MultiBitsApproximation<ZPolyUnderApproximation> specApprox
				= getVarApproximation(specification);

		{
			final Shape[] varShapes = getVarShapes(specification, specApprox);
			if (varShapes.length == 0) {
				return false;
			}

			final Op[] initialOps = getOps(library);

			int bestValue = Integer.MIN_VALUE;
			int[][] bestSolution = null;

			outer:
			for (final Op[] ops : getAscDescMerged(varShapes, initialOps)) {

				int[][] successors = new int[ops.length + 1][];
				for (int i = 0; i < ops.length; ++i) {
					successors[i] = new int[ops[i].arguments.length];
					Arrays.fill(successors[i], -1);
				}
				successors[ops.length] = new int[] { -1 };

				int numTotalArgs = 0;
				for (final Op op : ops) {
					numTotalArgs += op.arguments.length;
				}

				final int numLeaves = numTotalArgs - ops.length + 1;

				final Iterator<int[][]> succIter = combinations(varShapes, ops, successors);
				while (succIter.hasNext()) {
					final int[][] curSucc = succIter.next();

					optimize(varShapes, ops, curSucc);

					final int currentValue = numLeaves - evaluate(varShapes, ops, curSucc);
					if (currentValue > bestValue) {
						bestValue = currentValue;
						bestSolution = curSucc;

						if (bestValue == varShapes.length) {
							break outer;
						}
					}
				}
			}

			assert bestValue <= varShapes.length;

			if (bestValue != varShapes.length) {
				return true;
			}
		}

		return !oneCheck(specApprox, library);
	}



	public static void main(String[] args) {
		final int bitWidth = Integer.parseInt(args[0]);
		final int numInputs = Integer.parseInt(args[1]);
		final Function<List<BVAst>, BVAst> specFun = FunctionParser.parse(args[2]);

		final List<LibraryFunction> libList = new ArrayList<>();

		for (int i = 3; i < args.length; ++i) {
			if (args[i].startsWith("(")) {
				// Custom library function specification
				final String[] splitted = args[i].split(",");

				int lfNumInputs = 0;
				try {
					lfNumInputs = Integer.parseInt(splitted[1].trim());
				} catch (final NumberFormatException e) {
					System.out.println("Unexpected input number " + splitted[1] + ". Expected number");
					return;
				}

				final StringBuilder functionBuilder = new StringBuilder();
				for (int j = 2; j < splitted.length - 1; ++j) {
					functionBuilder.append(splitted[j]).append(',');
				}
				functionBuilder.append(splitted[splitted.length - 1]
						.substring(0, splitted[splitted.length - 1].length()));

				try {
					libList.add(new LibraryFunction(
							splitted[0].substring(1).trim(),
							Collections.nCopies(lfNumInputs, bitWidth),
							bitWidth,
							FunctionParser.parse(functionBuilder.toString())));
				} catch (final IllegalArgumentException e) {
					System.out.println("Library function invalid: " + e.getMessage());
					return;
				}
			} else if (args[i].startsWith("const")) {
				// Constant values
				final String[] splitted = args[i].split(" ");
				int lfBitWidth = -1;

				if (splitted.length != 2 && splitted.length != 3) {
					System.out.println("Expected 1 or 2 arguments to const, but got " + (splitted.length - 1));
					return;
				}

				try {
					lfBitWidth = Integer.parseInt(splitted[1]);
				} catch (final NumberFormatException e) {
					System.out.println("Unexpected bit width " + splitted[1] + ". Expected number");
					return;
				}

				if (lfBitWidth <= 0) {
					System.out.println("Bit width must be positive");
					return;
				}

				if (splitted.length == 2) {
					libList.add(LibraryFunction.getArbitraryConst(lfBitWidth));
				} else {
					long constValue = 0;
					try {
						if (splitted[2].startsWith("0x")) {
							constValue = Long.parseLong(splitted[2].substring(2), 16);
						} else {
							constValue = Long.parseLong(splitted[2]);
						}
					} catch (final NumberFormatException e) {
						System.out.println("Unexpected constant value " + splitted[2] + ". Expected number");
						return;
					}

					libList.add(LibraryFunction.getConst(lfBitWidth, constValue));
				}
			} else {
				switch (args[i]) {
					case "add":
						libList.add(LibraryFunction.getAdd(bitWidth));
						break;
					case "sub":
						libList.add(LibraryFunction.getSub(bitWidth));
						break;
					case "mul":
						libList.add(LibraryFunction.getMul(bitWidth));
						break;
					case "sdiv":
						libList.add(LibraryFunction.getSDiv(bitWidth));
						break;
					case "udiv":
						libList.add(LibraryFunction.getUDiv(bitWidth));
						break;
					case "srem":
						libList.add(LibraryFunction.getSRem(bitWidth));
						break;
					case "urem":
						libList.add(LibraryFunction.getURem(bitWidth));
						break;
					case "smod":
						libList.add(LibraryFunction.getSMod(bitWidth));
						break;
					case "umod":
						libList.add(LibraryFunction.getUMod(bitWidth));
						break;
					case "and":
						libList.add(LibraryFunction.getAnd(bitWidth));
						break;
					case "or":
						libList.add(LibraryFunction.getOr(bitWidth));
						break;
					case "xor":
						libList.add(LibraryFunction.getXor(bitWidth));
						break;
					case "shl":
						libList.add(LibraryFunction.getShl(bitWidth));
						break;
					case "ashr":
						libList.add(LibraryFunction.getAshr(bitWidth));
						break;
					case "lshr":
						libList.add(LibraryFunction.getLshr(bitWidth));
						break;
					case "shljava":
						libList.add(LibraryFunction.getShlJava(bitWidth));
						break;
					case "ashrjava":
						libList.add(LibraryFunction.getAshrJava(bitWidth));
						break;
					case "lshrjava":
						libList.add(LibraryFunction.getLshrJava(bitWidth));
						break;
					case "not":
						libList.add(LibraryFunction.getNot(bitWidth));
						break;
					case "neg":
						libList.add(LibraryFunction.getNeg(bitWidth));
						break;
					case "eq":
						libList.add(LibraryFunction.getEqBV(bitWidth));
						break;
					case "neq":
						libList.add(LibraryFunction.getNeqBV(bitWidth));
						break;
					case "ugt":
						libList.add(LibraryFunction.getUGtBV(bitWidth));
						break;
					case "uge":
						libList.add(LibraryFunction.getUGeBV(bitWidth));
						break;
					case "ult":
						libList.add(LibraryFunction.getULtBV(bitWidth));
						break;
					case "ule":
						libList.add(LibraryFunction.getULeBV(bitWidth));
						break;
					case "sgt":
						libList.add(LibraryFunction.getSGtBV(bitWidth));
						break;
					case "sge":
						libList.add(LibraryFunction.getSGeBV(bitWidth));
						break;
					case "slt":
						libList.add(LibraryFunction.getSLtBV(bitWidth));
						break;
					case "sle":
						libList.add(LibraryFunction.getSLeBV(bitWidth));
						break;
					default:
						System.out.println("Unexpected library function " + args[i]);
						return;
				}
			}
		}

		final List<Integer> inputBitWidths = Collections.nCopies(numInputs, bitWidth);
		final Specification specification = new Specification(inputBitWidths, bitWidth, specFun);

		// Warm-up
		for (int i = 0; i < 5; ++i) {
			isUnsat(specification, libList);
		}

		final long start = System.nanoTime();
		final boolean result = isUnsat(specification, libList);
		final long end = System.nanoTime();

		if (result) {
			System.out.println("unsat");
		} else {
			System.out.println("unknown");
		}
		System.out.printf(Locale.ENGLISH, "%.2f%n", (end - start) / 1000000.0);
	}
}

