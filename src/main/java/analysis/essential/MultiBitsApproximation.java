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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



public final class MultiBitsApproximation<T extends Approximation> implements Approximation {

	private List<T> bitApproximations;



	MultiBitsApproximation(final List<T> bitApproximations) {
		this.bitApproximations = Objects.requireNonNull(bitApproximations);
	}



	public int bitWidth() {
		return this.bitApproximations.size();
	}



	public T get(final int i) {
		return this.bitApproximations.get(i);
	}



	List<T> toList() {
		return this.bitApproximations;
	}



	@Override
	public Approximation and(final Approximation other) {
		final MultiBitsApproximation<T> otherMulti = (MultiBitsApproximation<T>) other;
		final int width = this.bitWidth();

		if (width != otherMulti.bitWidth()) {
			throw new IllegalArgumentException("Bit widths do not match");
		}

		final List<T> result = new ArrayList<>(width);
		for (int i = 0; i < width; ++i) {
			result.add((T) this.bitApproximations.get(i).and(otherMulti.bitApproximations.get(i)));
		}

		return new MultiBitsApproximation<>(result);
	}



	@Override
	public Approximation andM(final Approximation other) {
		final MultiBitsApproximation<T> otherMulti = (MultiBitsApproximation<T>) other;
		final int width = this.bitWidth();

		if (width != otherMulti.bitWidth()) {
			throw new IllegalArgumentException("Bit widths do not match");
		}

		for (int i = 0; i < width; ++i) {
			this.bitApproximations.get(i).andM(otherMulti.bitApproximations.get(i));
		}

		return this;
	}



	@Override
	public Approximation xor(final Approximation other) {
		final MultiBitsApproximation<T> otherMulti = (MultiBitsApproximation<T>) other;
		final int width = this.bitWidth();

		if (width != otherMulti.bitWidth()) {
			throw new IllegalArgumentException("Bit widths do not match");
		}

		final List<T> result = new ArrayList<>(width);
		for (int i = 0; i < width; ++i) {
			result.add((T) this.bitApproximations.get(i).xor(otherMulti.bitApproximations.get(i)));
		}

		return new MultiBitsApproximation<>(result);
	}



	@Override
	public Approximation xorM(final Approximation other) {
		final MultiBitsApproximation<T> otherMulti = (MultiBitsApproximation<T>) other;
		final int width = this.bitWidth();

		if (width != otherMulti.bitWidth()) {
			throw new IllegalArgumentException("Bit widths do not match");
		}

		for (int i = 0; i < width; ++i) {
			this.bitApproximations.get(i).xorM(otherMulti.bitApproximations.get(i));
		}

		return this;
	}



	@Override
	public Approximation not() {
		final int width = this.bitWidth();

		final List<T> result = new ArrayList<>(width);
		for (int i = 0; i < width; ++i) {
			result.add((T) this.bitApproximations.get(i).not());
		}

		return new MultiBitsApproximation<>(result);
	}



	@Override
	public Approximation notM() {
		final int width = this.bitWidth();

		for (int i = 0; i < width; ++i) {
			this.bitApproximations.get(i).notM();
		}

		return this;
	}



	@Override
	public Approximation join(final Approximation other) {
		final MultiBitsApproximation<T> otherMulti = (MultiBitsApproximation<T>) other;
		final int width = this.bitWidth();

		if (width != otherMulti.bitWidth()) {
			throw new IllegalArgumentException("Bit widths do not match");
		}

		final List<T> result = new ArrayList<>(width);
		for (int i = 0; i < width; ++i) {
			result.add((T) this.bitApproximations.get(i).join(otherMulti.bitApproximations.get(i)));
		}

		return new MultiBitsApproximation<>(result);
	}



	@Override
	public boolean isConstant(final int value) {
		final int width = this.bitWidth();
		int v = value;
		for (int i = 0; i < width; ++i, v >>= 1) {
			if (!this.bitApproximations.get(i).isConstant(v & 1)) {
				return false;
			}
		}
		return v == 0;
	}



	public MultiBitsApproximation<T> extract(final int from, final int to) {
		if (to < from) {
			throw new IllegalArgumentException();
		}

		return new MultiBitsApproximation<>(this.bitApproximations.subList(from, to + 1));
	}



	public MultiBitsApproximation<T> concat(final MultiBitsApproximation<T> other) {
		final List<T> result = new ArrayList<>(this.bitWidth() + other.bitWidth());
		result.addAll(other.bitApproximations);
		result.addAll(this.bitApproximations);
		return new MultiBitsApproximation<>(result);
	}



	@Override
	public String toString() {
		final StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("{\n");
		for (int i = 0; i < this.bitApproximations.size(); ++i) {
			resultBuilder.append('\t').append(i).append(" -> ")
					.append(this.bitApproximations.get(i))
					.append('\n');
		}
		resultBuilder.append('}');
		return resultBuilder.toString();
	}
}

