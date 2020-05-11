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

import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.math.IntMath;
import com.google.common.primitives.Booleans;

import static smt.Builder.*;

import smt.BitVector;
import smt.BitWidthMismatchException;
import smt.BoolAst;
import smt.Builder;
import smt.BVAst;
import smt.SatResult;
import smt.Solver;



abstract class AbstractBVSynthesizer implements Synthesizer {

	private Optional<SynthesisStatistics> statistics = Optional.empty();

	private static final int SAME_CEX_THRESHOLD = 16;
	private static final int SAME_CEX_HARD_THRESHOLD = 128;



	public Optional<SynthesisStatistics> getStatistics() {
		return this.statistics;
	}



	@Override
	public Optional<Program> synthesizeProgramWith(final Specification spec,
			final Library originalLibrary, final List<List<BitVector>> examples,
			final SynthesizerSettings settings) throws TimeoutException {

		if (examples.isEmpty()) {
			throw new IllegalArgumentException();
		}

		final Library library = removeUnusableOperations(spec, originalLibrary);
		if (library.isEmpty()) {
			return Optional.empty();
		}

		// There may be some inputs that cannot be used. Use this fact for an
		// optimization:
		final boolean[] inputUsable = getInputUsable(spec, library);
		final boolean notAllInputsUsable = Booleans.contains(inputUsable, false);

		final int numberOfStatements = getNumberOfStatements(spec, library);

		int inputSize = 0;
		for (final LibraryFunction libFunc : library) {
			inputSize += libFunc.getNumberOfInputs();
		}

		final Collection<Integer> outputs = ContiguousSet.create(
				Range.closedOpen(0, library.size()), DiscreteDomain.integers());
		final Collection<Integer> inputs = ContiguousSet.create(
				Range.closedOpen(library.size(), library.size() + inputSize), DiscreteDomain.integers());

		final Solver generateSolver = settings.makeGenerateSolver();
		assertWellFormednessConstraint(generateSolver, spec, library, outputs, inputs);

		final List<List<BitVector>> runningExamples = new ArrayList<>();
		final List<int[]> tempPrograms = new ArrayList<>();

		int exampleNumber = 0;
		for (final List<BitVector> example : examples) {
			assertExample(generateSolver, spec, library, outputs, inputs, exampleNumber, example);
			runningExamples.add(example);
			exampleNumber += 1;
		}
		
		int iterationNumber = 0;
		while (true) {
			switch (generateSolver.checkSat()) {
				case SAT: {
					final int[] program = getProgram(generateSolver, spec, library, outputs, inputs);
					tempPrograms.add(program);

					final List<BitVector> auxVars = getAuxiliaryVariableValues(generateSolver, library);

					// Optimization: If the last SAME_CEX_THRESHOLD counter examples
					// resulted in the same output, try to force a different output
					final Optional<BitVector> sameOutput = checkSameOutputOptimization(spec,
							runningExamples);

					final Solver verifySolver = settings.makeVerifySolver(sameOutput.isPresent());

					switch (verify(verifySolver, spec, library, outputs, inputs, program, auxVars)) {
						case SAT: {
							final List<BitVector> counterExample = getCounterExample(verifySolver, spec,
									sameOutput);

							assert !runningExamples.contains(counterExample);

							// Optimization: If not all inputs are used and all used inputs
							// already occur in runningExamples, the synthesis problem is
							// unsatisfiable.
							if (notAllInputsUsable
									&& containsMasked(runningExamples, counterExample, inputUsable)) {
								// -> unsat
								this.setStatistics(iterationNumber, runningExamples, tempPrograms);
								settings.freeSolvers();
								return Optional.empty();
							}

							assertExample(generateSolver, spec, library, outputs, inputs, exampleNumber,
									counterExample);
							runningExamples.add(counterExample);
							exampleNumber += 1;

							// Optimization: If there are more than SAME_CEX_HARD_THRESHOLD
							// examples and all yield the same result, check whether the
							// specification describes a constant function.
							if (runningExamples.size() > SAME_CEX_HARD_THRESHOLD) {
								final Optional<BitVector> allSameOutput = checkAllSameOutputOptimization(spec,
										runningExamples);
								if (allSameOutput.isPresent()) {
									try {
										final Optional<List<BitVector>> newExample = constantFunctionOptimization(
												settings, spec, allSameOutput.get());

										if (newExample.isPresent()) {
											// -> non-constant function
											assertExample(generateSolver, spec, library, outputs, inputs, exampleNumber,
													newExample.get());
											runningExamples.add(newExample.get());
											exampleNumber += 1;
										} else {
											// -> constant function; forbid using inputs
											forbidInputDependence(generateSolver, spec, library);
										}
									} catch (final TimeoutException e) {
										this.setStatistics(iterationNumber, runningExamples, tempPrograms);
										settings.freeSolvers();
										throw e;
									}
								}
							}
							break;
						}

						case UNSAT: {
							this.setStatistics(iterationNumber, runningExamples, tempPrograms);
							settings.freeSolvers();
							return Optional.of(makeResult(
									library,
									spec.getNumberOfInputs(),
									numberOfStatements,
									program,
									auxVars));
						}

						default: {
							this.setStatistics(iterationNumber, runningExamples, tempPrograms);
							settings.freeSolvers();
							throw new TimeoutException();
						}
					}
					break;
				}

				case UNSAT: {
					this.setStatistics(iterationNumber, runningExamples, tempPrograms);
					settings.freeSolvers();
					return Optional.empty();
				}

				default: {
					this.setStatistics(iterationNumber, runningExamples, tempPrograms);
					settings.freeSolvers();
					throw new TimeoutException();
				}
			}

			iterationNumber += 1;
		}
	}



	protected abstract void assertWellFormednessConstraint(final Solver generateSolver,
			final Specification spec, final Library library,
			final Collection<Integer> outputs, final Collection<Integer> inputs);



	protected abstract void assertExample(final Solver generateSolver, final Specification spec,
			final Library library, final Collection<Integer> outputs,
			final Collection<Integer> inputs, final int exampleNumber, final List<BitVector> example);



	protected abstract int[] getProgram(final Solver generateSolver, final Specification spec,
			final Library library, final Collection<Integer> outputs,
			final Collection<Integer> inputs);



	protected abstract void forbidInputDependence(final Solver generateSolver,
			final Specification spec, final Library library);



	protected SatResult verify(final Solver solver, final Specification spec,
			final Library library, final Collection<Integer> outputs,
			final Collection<Integer> inputs, final int[] program,
			final List<BitVector> auxiliaryVariables) {

		assertVerificationConstraint(solver, spec, library, outputs, inputs);
		assertProgram(solver, spec, library, program);
		assertAuxiliaryVariables(solver, library, auxiliaryVariables);

		final SatResult result = solver.checkSat();
		if (result != SatResult.UNSAT) {
			return result;
		}

		return SatResult.UNSAT;
	}



	protected void assertVerificationConstraint(final Solver verifySolver,
			final Specification spec, final Library library,
			final Collection<Integer> outputs, final Collection<Integer> inputs) {

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);

		final List<BVAst> ls = new ArrayList<>();
		for (int i = 0; i < outputs.size() + inputs.size(); ++i) {
			ls.add(mkBVVar(lbw, "l_" + i));
		}

		final List<BVAst> tmpvs = new ArrayList<>();
		for (int i = 0; i < ls.size(); ++i) {
			tmpvs.add(mkBVVar(getComponentBitWidth(library, i), "t_" + i));
		}

		final List<BVAst> invs = new ArrayList<>();
		for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
			invs.add(mkBVVar(spec.getInputBitWidth(i), "in_" + i));
		}

		final BVAst outv = mkBVVar(spec.getOutputBitWidth(), "out");

		assertLibrary(verifySolver, library, tmpvs);
		assertConnectivity(verifySolver, spec, library, ls, invs, outv, tmpvs);

		// Preconditions
		for (final Function<List<BVAst>, BoolAst> precondition : spec.getPreconditions()) {
			verifySolver.add(precondition.apply(invs));
		}

		verifySolver.add(mkNe(outv, spec.getFunction().apply(invs)));
	}



	protected void assertLibrary(final Solver solver, final List<LibraryFunction> library,
			final List<BVAst> tmpvs) {

		int argumentOffset = library.size();
		for (int i = 0; i < library.size(); ++i) {
			final int argumentCount = library.get(i).getNumberOfInputs();
			List<BVAst> funcInputs = tmpvs.subList(argumentOffset, argumentOffset + argumentCount);

			if (library.get(i).getNumberOfAuxiliaryVariables() > 0) {
				funcInputs = new ArrayList<>(funcInputs);
				for (int j = 0; j < library.get(i).getNumberOfAuxiliaryVariables(); ++j) {
					funcInputs.add(getAuxiliaryVariable(library, i, j));
				}
			}

			try {
				solver.add(mkEq(tmpvs.get(i), library.get(i).getFunction().apply(funcInputs)));
			} catch (final BitWidthMismatchException e) {
				throw new BitWidthMismatchException(library.get(i).getName()
						+ " (expected: " + tmpvs.get(i).getWidth()
						+ ", but was: " + library.get(i).getFunction().apply(funcInputs).getWidth() + ")");
			}

			argumentOffset += argumentCount;
		}
	}



	protected void assertConnectivity(final Solver solver, final Specification spec,
			final Library library, final List<BVAst> ls, final List<BVAst> invs,
			final BVAst outv, final List<BVAst> tmpvs) {

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);

		assertConnectivity(solver, spec, library, ls, invs, outv, tmpvs,
				mkBVConst(lbw, spec.getNumberOfInputs() + getNumberOfStatements(spec, library) - 1));
	}



	protected void assertConnectivity(final Solver solver, final Specification spec,
			final Library library, final List<BVAst> ls, final List<BVAst> invs,
			final BVAst outv, final List<BVAst> tmpvs, final BVAst outConst) {

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);

		// Connectivity between the inputs and outputs of the same library
		// function can be omitted. To find these, we construct a map from library
		// inputs to library outputs.
		final int[] libMap = new int[tmpvs.size()];
		Arrays.fill(libMap, -1);
		for (int i = 0, j = library.size(); i < library.size(); ++i) {
			for (int k = 0; k < library.get(i).getNumberOfInputs(); ++k) {
				libMap[j++] = i;
			}
		}

		for (int i = 0; i < tmpvs.size(); ++i) {
			if (i < library.size()) {
				// Connectivity with outv
				if (getComponentBitWidth(library, i) == spec.getOutputBitWidth()) {
					solver.add(mkImplies(mkEq(ls.get(i), outConst), mkEq(tmpvs.get(i), outv)));
				}
				// Connectivity with other tmpvs
				for (int j = Math.max(library.size(), i + 1); j < tmpvs.size(); ++j) {
					if (libMap[j] != i && canConnect(library, i, libMap[j], getInputIndex(j, libMap))) {
						solver.add(mkImplies(
								mkEq(ls.get(j), ls.get(i)),
								mkEq(tmpvs.get(j), tmpvs.get(i))));
					}
				}
			} else {
				// Connectivity with invs
				for (int j = 0; j < invs.size(); ++j) {
					if (canConnect(spec.getInputBitWidth(j), library.get(libMap[i]),
							getInputIndex(i, libMap))) {

						solver.add(mkImplies(
								mkEq(ls.get(i), mkBVConst(lbw, j)),
								mkEq(tmpvs.get(i), invs.get(j))));
					}
				}
			}
		}
	}



	protected void assertProgram(final Solver verifySolver, final Specification spec,
			final List<LibraryFunction> library, final int[] program) {

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);
		for (int i = 0; i < program.length; ++i) {
			verifySolver.add(mkEq(mkBVVar(lbw, "l_" + i), mkBVConst(lbw, program[i])));
		}
	}



	protected void assertAuxiliaryVariables(final Solver verifySolver,
			final List<LibraryFunction> library, final List<BitVector> auxiliaryVariables) {

		for (int i = 0, k = 0; i < library.size() && k < auxiliaryVariables.size(); ++i) {
			for (int j = 0; j < library.get(i).getNumberOfAuxiliaryVariables(); ++j) {
				verifySolver.add(mkEq(
						getAuxiliaryVariable(library, i, j),
						mkBVConst(auxiliaryVariables.get(k))));
				k += 1;
			}
		}
	}



	protected List<BitVector> getAuxiliaryVariableValues(final Solver solver, final Library library) {
		final List<BitVector> result = new ArrayList<>();

		for (int i = 0; i < library.size(); ++i) {
			final LibraryFunction libFunc = library.get(i);
			for (int j = 0; j < libFunc.getNumberOfAuxiliaryVariables(); ++j) {
				result.add(solver.getBVAssignment(getAuxiliaryVariable(library, i, j)));
			}
		}

		return result;
	}



	protected List<BitVector> getCounterExample(final Solver verifySolver, final Specification spec) {
		final List<BitVector> result = new ArrayList<>();
		for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
			result.add(verifySolver.getBVAssignment(
					mkBVVar(spec.getInputBitWidth(i), "in_" + i)));
		}
		return result;
	}



	private List<BitVector> getCounterExample(final Solver verifySolver, final Specification spec,
			final Optional<BitVector> sameOutput) {

		final List<BitVector> oldCounterExample = getCounterExample(verifySolver, spec);

		if (sameOutput.isPresent()) {
			final BitVector curOutput = evalSpec(spec, oldCounterExample);
			if (curOutput.equals(sameOutput.get())) {
				// Try to assert that outputs differ
				final List<BVAst> invs = new ArrayList<>();
				for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
					invs.add(mkBVVar(spec.getInputBitWidth(i), "in_" + i));
				}
				verifySolver.add(mkNe(mkBVConst(curOutput), spec.getFunction().apply(invs)));

				if (verifySolver.checkSat() == SatResult.SAT) {
					return getCounterExample(verifySolver, spec);
				}
			}
		}

		return oldCounterExample;
	}



	protected Program makeResult(final List<LibraryFunction> library, final int numInputs,
			final int numStmts, final int[] program, final List<BitVector> auxiliaryVariables) {

		final int[] argumentOffsets = new int[library.size() + 1];
		argumentOffsets[0] = library.size();
		for (int i = 1; i <= library.size(); ++i) {
			argumentOffsets[i] = argumentOffsets[i - 1] + library.get(i - 1).getNumberOfInputs();
		}

		// Find and remove unused statements
		final boolean[] covered = new boolean[library.size()];
		final Queue<Integer> toVisit = new ArrayDeque<>();
		for (int i = 0; i < library.size(); ++i) {
			if (program[i] == numStmts + numInputs - 1) {
				toVisit.offer(i);
				break;
			}
		}

		while (!toVisit.isEmpty()) {
			final int current = toVisit.poll();

			if (covered[current]) {
				continue;
			}
			covered[current] = true;

			for (int j = argumentOffsets[current]; j < argumentOffsets[current + 1]; ++j) {
				for (int i = 0; i < library.size(); ++i) {
					if (program[i] == program[j]) {
						if (!covered[i]) {
							toVisit.offer(i);
						}
						break;
					}
				}
			}
		}

		// First assign numbers to covered statements
		int numCovered = 0;
		for (int i = 0; i < covered.length; ++i) {
			if (covered[i]) {
				numCovered += 1;
			}
		}

		final int[] newProgram = new int[program.length];
		final int[] rewriteMap = new int[library.size()];
		int bound = numInputs;
		for (int j = 0; j < numCovered; ++j) {
			// Find smallest covered i with program[i] >= bound
			int i = -1;
			int progval = Integer.MAX_VALUE;
			for (int k = 0; k < library.size(); ++k) {
				if (covered[k] && program[k] >= bound && program[k] < progval) {
					i = k;
					progval = program[k];
				}
			}

			newProgram[i] = j + numInputs;
			rewriteMap[progval - numInputs] = j + numInputs;
			bound = progval + 1;
		}

		// Now add the rest
		bound = numInputs;
		for (int j = numCovered; j < library.size(); ++j) {
			// Find smallest uncovered i with program[i] >= bound
			int i = -1;
			int progval = Integer.MAX_VALUE;
			for (int k = 0; k < library.size(); ++k) {
				if (!covered[k] && program[k] >= bound && program[k] < progval) {
					i = k;
					progval = program[k];
				}
			}

			newProgram[i] = j + numInputs;
			rewriteMap[progval - numInputs] = j + numInputs;
			bound = progval + 1;
		}

		// Rewrite the arguments
		for (int i = library.size(); i < program.length; ++i) {
			if (program[i] < numInputs) {
				newProgram[i] = program[i];
			} else {
				newProgram[i] = rewriteMap[program[i] - numInputs];
			}
		}

		return new Program(library, numInputs, numCovered, newProgram, auxiliaryVariables);
	}



	protected int getNumberOfStatements(final Specification spec, final Library library) {
		return Utils.map(spec.getSizeRestriction(), library::getMaximalSizeForCostLimit)
				.orElse(library.size());
	}



	protected int getComponentBitWidth(final List<LibraryFunction> library, final int number) {
		if (number < library.size()) {
			return library.get(number).getOutputBitWidth();
		}
		for (int i = 0, k = library.size(); i < library.size(); ++i) {
			final int nextK = k + library.get(i).getNumberOfInputs();
			if (number < nextK) {
				return library.get(i).getInputBitWidth(number - k);
			}
			k = nextK;
		}
		throw new IllegalStateException();
	}



	// Returns true if outIndex can be the argIndex'th argument of inIndex
	protected boolean canConnect(final List<LibraryFunction> library, final int outIndex,
			final int inIndex, final int argIndex) {

		return canConnect(getComponentBitWidth(library, outIndex), library.get(inIndex), argIndex);
	}



	protected boolean canConnect(final int bitWidth, final LibraryFunction libraryFunction,
			final int argIndex) {

		return libraryFunction.getInputBitWidth(argIndex) == bitWidth;
	}



	private BVAst getAuxiliaryVariable(final List<LibraryFunction> library, final int libIdx,
			final int varIdx) {

		return mkBVVar(library.get(libIdx).getWidthOfAuxiliaryVariable(varIdx),
				"av_" + libIdx + "_" + varIdx);
	}



	private void setStatistics(final int iterationNumber,
			final List<List<BitVector>> runningExamples, final List<int[]> tempPrograms) {

		this.statistics = Optional.of(new SynthesisStatistics(
				iterationNumber + 1, runningExamples, tempPrograms));
	}



	private static int getInputIndex(final int j, final int[] libMap) {
		int r = 0;
		for (int k = j - 1; k >= 0 && libMap[k] == libMap[j]; --k) {
			r += 1;
		}
		return r;
	}



	private Library removeUnusableOperations(final Specification spec, final Library library) {

		if (!spec.getSizeRestriction().isPresent() || library.hasUniformCosts()) {
			return library;
		}

		// Remove operations for which there is no path that does not violate the
		// cost constraint

		final Map<Integer, Integer> minCosts = new HashMap<>();
		final ArrayDeque<Integer> toProcess = new ArrayDeque<>();
		for (int i = 0; i < library.size(); ++i) {
			if (library.get(i).getOutputBitWidth() == spec.getOutputBitWidth()) {
				toProcess.offer(i);
				minCosts.put(i, library.getCost(i));
			}
		}

		while (!toProcess.isEmpty()) {
			final int cur = toProcess.poll();

			for (int k = 0; k < library.get(cur).getNumberOfInputs(); ++k) {
				for (int i = 0; i < library.size(); ++i) {
					if (library.get(i).getOutputBitWidth() == library.get(cur).getInputBitWidth(k)) {
						final int costs = minCosts.get(cur) + library.getCost(cur, i) + library.getCost(i);
						if (!minCosts.containsKey(i) || minCosts.get(i) > costs) {
							toProcess.offer(i);
							minCosts.put(i, costs);
						}
					}
				}
			}
		}

		final List<Integer> used = new ArrayList<>();
		for (int i = 0; i < library.size(); ++i) {
			if (minCosts.containsKey(i) && minCosts.get(i) <= spec.getSizeRestriction().getAsInt()) {
				used.add(i);
			}
		}

		final List<LibraryFunction> resultList = new ArrayList<>();
		final int[][] resultCosts = new int[used.size()][used.size()];
		for (int i = 0; i < used.size(); ++i) {
			resultList.add(library.get(used.get(i)));

			for (int j = 0; j < used.size(); ++j) {
				resultCosts[i][j] = library.getCost(used.get(i), used.get(j));
			}
		}

		return Library.of(resultList, resultCosts);
	}



	private static boolean[] getInputUsable(final Specification spec, final Library library) {
		final Set<Integer> usableBitWidths = new HashSet<>();
		for (final LibraryFunction libFunc : library) {
			for (int k = 0; k < libFunc.getNumberOfInputs(); ++k) {
				usableBitWidths.add(libFunc.getInputBitWidth(k));
			}
		}

		final boolean[] result = new boolean[spec.getNumberOfInputs()];
		for (int i = 0; i < result.length; ++i) {
			result[i] = usableBitWidths.contains(spec.getInputBitWidth(i));
		}

		return result;
	}



	private static boolean containsMasked(final List<List<BitVector>> runningExamples,
			final List<BitVector> counterExample, final boolean[] mask) {

		for (final List<BitVector> previousExample : runningExamples) {
			boolean equal = true;
			for (int i = 0; equal && i < mask.length; ++i) {
				if (mask[i] && !previousExample.get(i).equals(counterExample.get(i))) {
					equal = false;
				}
			}

			if (equal) {
				return true;
			}
		}

		return false;
	}



	private static Optional<BitVector> checkSameOutputOptimization(final Specification spec,
			final List<List<BitVector>> runningExamples) {

		final int numExamples = runningExamples.size();
		if (numExamples < SAME_CEX_THRESHOLD) {
			return Optional.empty();
		}

		final BitVector firstOutput = evalSpec(spec, runningExamples
				.get(numExamples - SAME_CEX_THRESHOLD));

		for (int i = numExamples - SAME_CEX_THRESHOLD + 1; i < numExamples; ++i) {
			final BitVector curOutput = evalSpec(spec, runningExamples.get(i));

			if (!firstOutput.equals(curOutput)) {
				return Optional.empty();
			}
		}

		return Optional.of(firstOutput);
	}



	private static Optional<BitVector> checkAllSameOutputOptimization(final Specification spec,
			final List<List<BitVector>> runningExamples) {

		final BitVector firstOutput = evalSpec(spec, runningExamples.get(0));

		final int numExamples = runningExamples.size();
		for (int i = 1; i < numExamples; ++i) {
			final BitVector curOutput = evalSpec(spec, runningExamples.get(i));

			if (!firstOutput.equals(curOutput)) {
				return Optional.empty();
			}
		}

		return Optional.of(firstOutput);
	}



	protected static BitVector evalSpec(final Specification spec, final List<BitVector> inputs) {
		return spec.getFunction().apply(Lists.transform(inputs, Builder::mkBVConst))
				.eval(Collections.emptyMap());
	}



	private Optional<List<BitVector>> constantFunctionOptimization(final SynthesizerSettings settings,
			final Specification spec, final BitVector output) throws TimeoutException {

		final Solver checkConstSolver = settings.makeVerifySolver();

		final List<BVAst> invs = new ArrayList<>();
		for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
			invs.add(mkBVVar(spec.getInputBitWidth(i), "in_" + i));
		}

		// Preconditions
		for (final Function<List<BVAst>, BoolAst> precondition : spec.getPreconditions()) {
			checkConstSolver.add(precondition.apply(invs));
		}

		checkConstSolver.add(mkNe(mkBVConst(output), spec.getFunction().apply(invs)));

		switch (checkConstSolver.checkSat()) {
			case SAT: {
				// -> non-constant function
				return Optional.of(getCounterExample(checkConstSolver, spec));
			}
			case UNSAT: {
				// -> constant function
				return Optional.empty();
			}
			default: {
				throw new TimeoutException();
			}
		}
	}
}

