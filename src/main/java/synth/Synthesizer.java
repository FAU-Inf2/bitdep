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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import smt.BitVector;



public interface Synthesizer {
	Optional<Program> synthesizeProgramWith(Specification spec, Library library,
			List<List<BitVector>> examples, SynthesizerSettings settings) throws TimeoutException;
	


	default Optional<Program> synthesizeProgram(final Specification spec,
			final Library library, final List<List<BitVector>> examples)
			throws TimeoutException {
		return synthesizeProgramWith(spec, library, examples, SynthesizerSettings.getDefault());
	}



	default Optional<Program> synthesizeProgram(final Specification spec,
			final Library library) throws TimeoutException {
		return synthesizeProgram(spec, library, SynthesizerSettings.getDefault());
	}



	default Optional<Program> synthesizeProgram(final Specification spec,
			final Library library, final SynthesizerSettings settings) throws TimeoutException {

		final List<BitVector> initialExample = new ArrayList<>(spec.getNumberOfInputs());
		for (int i = 0; i < spec.getNumberOfInputs(); ++i) {
			initialExample.add(new BitVector(spec.getInputBitWidth(i), 0));
		}

		return synthesizeProgramWith(spec, library, Collections.singletonList(initialExample),
				settings);
	}
}

