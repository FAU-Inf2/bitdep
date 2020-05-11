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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;

import static smt.Builder.*;

import smt.BitVector;
import smt.BoolAst;
import smt.BVAst;
import smt.Solver;



public class ListBasedSynthesizer extends AbstractBVSynthesizer {

	List<List<Integer>> typeClasses;



	@Override
	protected void assertWellFormednessConstraint(final Solver generateSolver,
			final Specification spec, final Library library,
			final Collection<Integer> outputs, final Collection<Integer> inputs) {

		this.typeClasses = Collections.singletonList(new ArrayList<>());
		for (int i = 0; i < library.size(); ++i) {
			this.typeClasses.get(0).add(i);
		}

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);
		final List<BVAst> ls = new ArrayList<>();
		for (int i = 0; i < outputs.size() + inputs.size(); ++i) {
			ls.add(mkBVVar(lbw, "l_" + i));
		}

		// Inputs -- TODO: Unnecessary?
		final int numberOfStatements = getNumberOfStatements(spec, library);
		final BVAst highInput = mkBVConst(lbw, spec.getNumberOfInputs() + numberOfStatements);
		for (final int i : inputs) {
			generateSolver.add(mkULt(ls.get(i), highInput));
		}

		// Outputs
		int low = spec.getNumberOfInputs();
		for (final List<Integer> typeClass : typeClasses) {
			final int high = low + typeClass.size();

			final BVAst lowOutput = mkBVConst(lbw, low);
			final BVAst highOutput = mkBVConst(lbw, high);

			for (final int i : typeClass) {
				generateSolver.add(mkUGe(ls.get(i), lowOutput));
				generateSolver.add(mkULt(ls.get(i), highOutput));
			}

			low = high;
		}

		// Consistency
		if (outputs.size() > 1) {
			//generateSolver.add(mkAllDifferent(ls.subList(0, outputs.size())));
			for (final List<Integer> typeClass : typeClasses) {
				if (typeClass.size() > 1) {
					generateSolver.add(mkAllDifferent(Lists.transform(typeClass, ls::get)));
				}
			}
		}

		// Acyclicity
		int argumentOffset = library.size();
		for (int i = 0; i < library.size(); ++i) {
			for (int j = 0; j < library.get(i).getNumberOfInputs(); ++j) {
				generateSolver.add(mkUGt(ls.get(i), ls.get(argumentOffset)));
				argumentOffset += 1;
			}
		}

		// Type Correctness
		for (int i = 0; i < library.size(); ++i) {
			for (int j = 0, lx = library.size(); j < library.size(); ++j) {
				if (i != j) {
					for (int k = 0; k < library.get(j).getNumberOfInputs(); ++k) {
						if (!canConnect(library, i, j, k)) {
							generateSolver.add(mkNe(ls.get(i), ls.get(lx + k)));
						}
					}
				}
				lx += library.get(j).getNumberOfInputs();
			}
		}
		for (int i = 0, lx = library.size(); i < library.size(); ++i) {
			final LibraryFunction lfi = library.get(i);
			for (int k = 0; k < lfi.getNumberOfInputs(); ++k) {
				for (int j = 0; j < spec.getNumberOfInputs(); ++j) {
					if (!canConnect(spec.getInputBitWidth(j), lfi, k)) {
						generateSolver.add(mkNe(ls.get(lx), mkBVConst(lbw, j)));
					}
				}
				lx += 1;
			}
		}
	}



	@Override
	protected void assertExample(final Solver generateSolver, final Specification spec,
			final Library library, final Collection<Integer> outputs,
			final Collection<Integer> inputs, final int exampleNumber, final List<BitVector> example) {

		if (example.size() != spec.getNumberOfInputs()) {
			throw new IllegalArgumentException();
		}
		
		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);

		final List<BVAst> ls = new ArrayList<>();
		for (int i = 0; i < outputs.size() + inputs.size(); ++i) {
			ls.add(mkBVVar(lbw, "l_" + i));
		}

		final List<BVAst> tmpvs = new ArrayList<>();
		for (int i = 0; i < ls.size(); ++i) {
			tmpvs.add(mkBVVar(getComponentBitWidth(library, i), "t_" + exampleNumber + "_" + i));
		}

		final List<BVAst> invs = new ArrayList<>();
		for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
			invs.add(mkBVVar(spec.getInputBitWidth(i), "in_" + exampleNumber + "_" + i));
		}

		final BVAst outv = mkBVVar(spec.getOutputBitWidth(), "out_" + exampleNumber);

		assertLibrary(generateSolver, library, tmpvs);
		assertConnectivity(generateSolver, spec, library, ls, invs, outv, tmpvs);

		// Specification
		//   * inputs
		final List<BVAst> arguments = new ArrayList<>();
		for (int i = 0; i < example.size(); ++i) {
			final BVAst arg = mkBVConst(example.get(i));
			generateSolver.add(mkEq(invs.get(i), arg));
			arguments.add(arg);
		}
		//   * output
		generateSolver.add(mkEq(outv,
				mkBVConst(spec.getFunction().apply(arguments).eval(Collections.emptyMap()))));
	}



	@Override
	protected int[] getProgram(final Solver generateSolver, final Specification spec,
			final Library library, final Collection<Integer> outputs,
			final Collection<Integer> inputs) {

		final int[] result = new int[outputs.size() + inputs.size()];
		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);
		for (int i = 0; i < result.length; ++i) {
			result[i] = generateSolver.getBVAssignment(mkBVVar(lbw, "l_" + i)).toUnsignedBigInteger()
					.intValue();
		}

		return result;
	}



	@Override
	protected void forbidInputDependence(final Solver generateSolver, final Specification spec,
			final Library library) {

		final int lbw = IntMath.log2(library.size() + spec.getNumberOfInputs() + 1, RoundingMode.UP);

		int argOffset = library.size();
		for (int i = 0; i < library.size(); ++i) {
			for (int k = 0; k < library.get(i).getNumberOfInputs(); ++k) {
				final BVAst lvar = mkBVVar(lbw, "l_" + argOffset);
				for (int j = 0; j < spec.getNumberOfInputs(); ++j) {
					generateSolver.add(mkNe(lvar, mkBVConst(new BitVector(lbw, j))));
				}
				argOffset += 1;
			}
		}
	}
}

