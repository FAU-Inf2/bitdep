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

import java.util.OptionalInt;

import smt.Solver;



public class SynthesizerSettings {

	private OptionalInt randomSeed;
	private OptionalInt timeout;
	private SolverType generateSolver;
	private SolverType verifySolver;



	private SynthesizerSettings() {
		this.randomSeed = OptionalInt.empty();
		this.timeout = OptionalInt.empty();
		this.generateSolver = SolverType.YICES;
		this.verifySolver = SolverType.YICES;
	}



	public OptionalInt getRandomSeed() {
		return this.randomSeed;
	}



	public void setRandomSeed(final int randomSeed) {
		this.randomSeed = OptionalInt.of(randomSeed);
	}



	public OptionalInt getTimeout() {
		return this.timeout;
	}



	public void setTimeout(final int timeout) {
		if (timeout <= 0) {
			throw new IllegalArgumentException();
		}
		this.timeout = OptionalInt.of(timeout);
	}



	public SolverType getGenerateSolverType() {
		return this.generateSolver;
	}



	public void setGenerateSolverType(final SolverType solverType ) {
		this.generateSolver = solverType;
	}



	public Solver makeGenerateSolver() {
		return makeSolver(this.generateSolver, true);
	}



	public SolverType getVerifySolverType() {
		return this.verifySolver;
	}



	public void setVerifySolverType(final SolverType solverType ) {
		this.verifySolver = solverType;
	}



	public Solver makeVerifySolver(final boolean incremental) {
		return makeSolver(this.verifySolver, incremental);
	}



	public Solver makeVerifySolver() {
		return makeVerifySolver(false);
	}



	public void freeSolvers() {
		if (this.generateSolver == SolverType.YICES || this.verifySolver == SolverType.YICES) {
			yices.YicesSolver.freeAll();
		}
	}



	private Solver makeSolver(final SolverType solverType, final boolean incremental) {
		switch (solverType) {
			case YICES:
				if (this.timeout.isPresent()) {
					return new yices.YicesSolver(this.timeout.getAsInt(), incremental);
				} else {
					return new yices.YicesSolver(incremental);
				}

			default:
				throw new IllegalStateException();
		}
	}



	public static SynthesizerSettings getDefault() {
		return new SynthesizerSettings();
	}
}

