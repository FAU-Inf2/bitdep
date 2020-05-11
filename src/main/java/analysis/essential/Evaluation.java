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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import smt.*;
import synth.FunctionParser;
import yices.YicesSolver;



public class Evaluation {

	private static List<Set<Integer>> getExactEssentialBits(final Function<List<BVAst>, BVAst> f,
			final int outputBitWidth, final List<BVAst> inputs) {

		final List<Set<Integer>> result = new ArrayList<>();
		for (int o = 0; o < outputBitWidth; ++o) {
			final Set<Integer> current = new HashSet<>();
			for (int v = 0, c = 0; v < inputs.size(); ++v) {
				for (int i = 0; i < inputs.get(v).getWidth(); ++i, ++c) {
					final List<BVAst> copy = new ArrayList(inputs);
					copy.set(v, Builder.mkXor(inputs.get(v),
							Builder.mkBVConst(inputs.get(v).getWidth(), 1L << i)));
					final YicesSolver solver = new YicesSolver(false);
					solver.add(Builder.mkNe(
							Builder.mkAnd(f.apply(inputs), Builder.mkBVConst(outputBitWidth, 1L << o)),
							Builder.mkAnd(f.apply(copy), Builder.mkBVConst(outputBitWidth, 1L << o))));
					if (solver.checkSat() == SatResult.SAT) {
						current.add(c);
					}
					YicesSolver.freeAll();
				}
			}
			result.add(current);
		}
		return result;
	}



	private static double mean(final List<Long> values, final int total) {
		long accum = 0;
		for (final long v : values) {
			accum += v;
		}
		return accum / (double) total;
	}



	private static double stddev(final List<Long> values, final double mean, final int total) {
		double result = 0.0;
		for (final long v : values) {
			result += (v - mean) * (v - mean);
		}
		return Math.sqrt(result / (total - 1));
	}



	private static double range(final double stddev, final int total) {
		return 2.776 * stddev / Math.sqrt(total);
	}



	private static void write(final String name, final double mean, final double range, final int delta) {
		System.out.println("\"" + name + "\": {");
		System.out.printf("  \"time_ms\": %.2f,%n", mean / 1000000.0);
		System.out.printf("  \"time_range_ms\": %.2f,%n", range / 1000000.0);
		System.out.printf("  \"delta\": %d%n", delta);
		System.out.println("},");
	}



	public static void main(String[] args) {
		final int bitWidth = Integer.parseInt(args[0]);
		final int numInputs = Integer.parseInt(args[1]);
		final Function<List<BVAst>, BVAst> specFun = FunctionParser.parse(args[2]);

		final List<BVAst> inputs = new ArrayList<>();
		for (int i = 0; i < numInputs; ++i) {
			inputs.add(Builder.mkBVVar(bitWidth, "i" + i));
		}

		final BVAst f = specFun.apply(inputs);

		// Enforce hot start
		ZPolyUnderApproximation.create(f, inputs);
		ZPolyUnderApproximation.create(f, inputs, 2);
		ZPolyOverApproximation.create(f, inputs);

		final int iterations = 35;

		final List<Long> under1Measurements = new ArrayList<>();
		final long under1Start = System.nanoTime();
		final MultiBitsApproximation<ZPolyUnderApproximation> funcUnder1Approximation
				= ZPolyUnderApproximation.create(f, inputs);
		under1Measurements.add(System.nanoTime() - under1Start);
		for (int i = 1; i < iterations; ++i) {
			final long under1Start_ = System.nanoTime();
			ZPolyUnderApproximation.create(f, inputs);
			under1Measurements.add(System.nanoTime() - under1Start_);
		}

		final List<Long> under2Measurements = new ArrayList<>();
		final long under2Start = System.nanoTime();
		final MultiBitsApproximation<ZPolyUnderApproximation> funcUnder2Approximation
				= ZPolyUnderApproximation.create(f, inputs, 2);
		under2Measurements.add(System.nanoTime() - under2Start);
		for (int i = 1; i < iterations; ++i) {
			final long under2Start_ = System.nanoTime();
			ZPolyUnderApproximation.create(f, inputs, 2);
			under2Measurements.add(System.nanoTime() - under2Start_);
		}

		final List<Long> overMeasurements = new ArrayList<>();
		final long overStart = System.nanoTime();
		final MultiBitsApproximation<ZPolyOverApproximation> funcOverApproximation
				= ZPolyOverApproximation.create(f, inputs);
		overMeasurements.add(System.nanoTime() - overStart);
		for (int i = 1; i < iterations; ++i) {
			final long overStart_ = System.nanoTime();
			ZPolyOverApproximation.create(f, inputs);
			overMeasurements.add(System.nanoTime() - overStart_);
		}

		final List<Long> exactMeasurements = new ArrayList<>();
		final long exactStart = System.nanoTime();
		final List<Set<Integer>> exact = getExactEssentialBits(specFun, f.getWidth(), inputs);
		exactMeasurements.add(System.nanoTime() - exactStart);
		for (int i = 1; i < iterations; ++i) {
			final long exactStart_ = System.nanoTime();
			getExactEssentialBits(specFun, f.getWidth(), inputs);
			exactMeasurements.add(System.nanoTime() - exactStart_);
		}

		assert funcUnder1Approximation.bitWidth() == bitWidth;
		assert funcUnder2Approximation.bitWidth() == bitWidth;
		assert funcOverApproximation.bitWidth() == bitWidth;
		assert exact.size() == bitWidth;

		int under1Missing = 0;
		int under2Missing = 0;
		int overSurplus = 0;

		for (int i = 0; i < bitWidth; ++i) {
			final Set<Integer> under1 = new HashSet<>();
			for (final Integer x : funcUnder1Approximation.get(i)) {
				under1.add(x);
			}
			final Set<Integer> under2 = new HashSet<>();
			for (final Integer x : funcUnder2Approximation.get(i)) {
				under2.add(x);
			}
			final Set<Integer> over = new HashSet<>();
			for (final Integer x : funcOverApproximation.get(i)) {
				over.add(x);
			}

			assert exact.get(i).containsAll(under1);
			assert exact.get(i).containsAll(under2);
			assert over.containsAll(exact.get(i));

			under1Missing += exact.get(i).size() - under1.size();
			under2Missing += exact.get(i).size() - under2.size();
			overSurplus += over.size() - exact.get(i).size();
		}

		final double under1Mean = mean(under1Measurements, iterations);
		final double under2Mean = mean(under2Measurements, iterations);
		final double overMean = mean(overMeasurements, iterations);
		final double exactMean = mean(exactMeasurements, iterations);

		final double under1Stddev = stddev(under1Measurements, under1Mean, iterations);
		final double under2Stddev = stddev(under2Measurements, under2Mean, iterations);
		final double overStddev = stddev(overMeasurements, overMean, iterations);
		final double exactStddev = stddev(exactMeasurements, exactMean, iterations);

		final double under1Range = range(under1Stddev, iterations);
		final double under2Range = range(under2Stddev, iterations);
		final double overRange = range(overStddev, iterations);
		final double exactRange = range(exactStddev, iterations);

		write("under1", under1Mean, under1Range, under1Missing);
		write("under2", under2Mean, under2Range, under2Missing);
		write("over", overMean, overRange, overSurplus);
		System.out.printf("\"exact_ms\": %.2f,%n", exactMean / 1000000.0);
		System.out.printf("\"exact_range_ms\": %.2f%n", exactRange / 1000000.0);
	}
}

