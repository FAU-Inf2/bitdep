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
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;



class Utils {

	static int[] makeIntArray(final int length, final int value) {
		final int[] result = new int[length];
		Arrays.fill(result, value);
		return result;
	}



	static OptionalInt map(final OptionalInt optInt, final IntUnaryOperator operator) {
		if (optInt.isPresent()) {
			return OptionalInt.of(operator.applyAsInt(optInt.getAsInt()));
		}
		return optInt; // is empty
	}
}

