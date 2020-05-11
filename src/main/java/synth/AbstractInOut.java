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

import java.util.List;

import com.google.common.primitives.Ints;



public abstract class AbstractInOut {

	protected final int[] inputBitWidths;
	protected final int outputBitWidth;



	protected AbstractInOut(final int numberOfInputs) {
		this(Utils.makeIntArray(numberOfInputs, 32), 32);
	}



	protected AbstractInOut(final List<Integer> inputBitWidths, final int outputBitWidth) {
		this(Ints.toArray(inputBitWidths), outputBitWidth);
	}



	protected AbstractInOut(final int[] inputBitWidths, final int outputBitWidth) {
		this.inputBitWidths = inputBitWidths;
		this.outputBitWidth = outputBitWidth;
	}



	public final int getNumberOfInputs() {
		return this.inputBitWidths.length;
	}



	public final int getOutputBitWidth() {
		return this.outputBitWidth;
	}



	public final int getInputBitWidth(final int inputNumber) {
		if (inputNumber < 0 || inputNumber >= this.inputBitWidths.length) {
			throw new IllegalArgumentException();
		}
		return this.inputBitWidths[inputNumber];
	}
}

