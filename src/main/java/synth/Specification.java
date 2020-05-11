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
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import smt.BoolAst;
import smt.BVAst;



public class Specification extends AbstractInOut {

	private final OptionalInt sizeRestriction;
	private final Function<List<BVAst>, BVAst> function;
	private final List<Function<List<BVAst>, BoolAst>> preconditions = new ArrayList<>();



	public Specification(final int numberOfInputs, final Function<List<BVAst>, BVAst> function) {
		this(numberOfInputs, OptionalInt.empty(), function);
	}



	public Specification(final List<Integer> inputBitWidths,
			final int outputBitWidth, final Function<List<BVAst>, BVAst> function) {
		this(inputBitWidths, outputBitWidth, OptionalInt.empty(), function);
	}



	public Specification(final int numberOfInputs, final int sizeRestriction,
			final Function<List<BVAst>, BVAst> function) {
		this(numberOfInputs, OptionalInt.of(sizeRestriction), function);
	}



	public Specification(final List<Integer> inputBitWidths, final int outputBitWidth,
			final int sizeRestriction, final Function<List<BVAst>, BVAst> function) {
		this(inputBitWidths, outputBitWidth, OptionalInt.of(sizeRestriction), function);
	}



	private Specification(final int numberOfInputs, final OptionalInt sizeRestriction,
			final Function<List<BVAst>, BVAst> function) {
		super(numberOfInputs);
		this.sizeRestriction = sizeRestriction;
		this.function = function;
	}



	private Specification(final List<Integer> inputBitWidths, final int outputBitWidth,
			final OptionalInt sizeRestriction, final Function<List<BVAst>, BVAst> function) {
		super(inputBitWidths, outputBitWidth);
		this.sizeRestriction = sizeRestriction;
		this.function = function;
	}



	public OptionalInt getSizeRestriction() {
		return this.sizeRestriction;
	}



	public Function<List<BVAst>, BVAst> getFunction() {
		return this.function;
	}



	public void addPrecondition(final Function<List<BVAst>, BoolAst> precondition) {
		this.preconditions.add(precondition);
	}



	public List<Function<List<BVAst>, BoolAst>> getPreconditions() {
		return this.preconditions;
	}
}

