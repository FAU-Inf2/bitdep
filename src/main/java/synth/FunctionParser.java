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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static smt.Builder.*;

import smt.BoolAst;
import smt.Builder;
import smt.BVAst;



public class FunctionParser {

	public static Function<List<BVAst>, BVAst> parse(final String functionString) {

		try {
			final List<String> tokens = tokenize(new StringReader(functionString));

			final Map<String, Integer> varMap = new HashMap<>();
			final int idx = parseInputs(tokens, 0, varMap);

			if (idx >= tokens.size() || !"->".equals(tokens.get(idx))) {
				throw new IllegalArgumentException("parse error");
			}

			return inputs -> parseExpression(tokens, idx + 1, varMap, inputs);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}



	public static Function<List<BVAst>, BoolAst> parseBool(final String functionString) {

		try {
			final List<String> tokens = tokenize(new StringReader(functionString));

			final Map<String, Integer> varMap = new HashMap<>();
			final int idx = parseInputs(tokens, 0, varMap);

			if (idx >= tokens.size() || !"->".equals(tokens.get(idx))) {
				throw new IllegalArgumentException("parse error");
			}

			return inputs -> parseBoolExpression(tokens, idx + 1, varMap, inputs);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}



	public static Function<List<BVAst>, BVAst> concat(final Function<List<BVAst>, BVAst> f,
			final Function<List<BVAst>, BVAst> g) {
		return inputs -> f.apply(Arrays.asList(g.apply(inputs)));
	}



	public static Function<List<BVAst>, BVAst> concat2(final Function<List<BVAst>, BVAst> f,
			final Function<List<BVAst>, BVAst> g, final Function<List<BVAst>, BVAst> h) {
		return inputs -> f.apply(Arrays.asList(
				g.apply(Arrays.asList(inputs.get(0))),
				h.apply(Arrays.asList(inputs.get(1)))));
	}



	public static Function<List<BVAst>, BVAst> bitReduceR(
			final Function<List<BVAst>, BVAst> function, final BVAst start) {

		return inputs -> applyBitReduceR(function, start, inputs);
	}



	private static BVAst applyBitReduceR(final Function<List<BVAst>, BVAst> function,
			final BVAst start, final List<BVAst> inputs) {

		BVAst result = start;
		for (int i = 31; i >= 0; --i) {
			result = function.apply(Arrays.asList(inputs.get(0), result, mkBVConst(32, i)));
		}
		return result;
	}



	public static Function<List<BVAst>, BVAst> bitReduceL(
			final Function<List<BVAst>, BVAst> function, final BVAst start) {

		return inputs -> applyBitReduceL(function, start, inputs);
	}



	private static BVAst applyBitReduceL(final Function<List<BVAst>, BVAst> function,
			final BVAst start, final List<BVAst> inputs) {

		BVAst result = start;
		for (int i = 0; i < 32; ++i) {
			result = function.apply(Arrays.asList(inputs.get(0), result, mkBVConst(32, i)));
		}
		return result;
	}



	private static List<String> tokenize(final Reader r) throws IOException {
		final List<String> result = new ArrayList<>();

		StringBuilder tokenBuilder = new StringBuilder();

		String delimiters = "";

		int next;
		while ((next = r.read()) >= 0) {
			if (Character.isWhitespace((char) next)) {
				if (tokenBuilder.length() != 0) {
					result.add(tokenBuilder.toString());
					tokenBuilder = new StringBuilder();
				}
				continue;
			} else if (delimiters.indexOf(next) >= 0) {
				if (tokenBuilder.length() != 0) {
					result.add(tokenBuilder.toString());
					tokenBuilder = new StringBuilder();
				}
				delimiters = "";
			}
			tokenBuilder.append((char) next);

			if (delimiters.isEmpty()) {
				switch (next) {
					case '-':
						delimiters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()";
						break;

					case '(':
					case ')':
						delimiters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-()";
						break;

					default:
						delimiters = "-()";
				}
			}
		}

		if (tokenBuilder.length() != 0) {
			result.add(tokenBuilder.toString());
		}

		return result;
	}



	private static int parseInputs(final List<String> tokens, final int start,
			final Map<String, Integer> varMap) {
		
		int idx = start;
		for (int i = 0; idx < tokens.size() && !"->".equals(tokens.get(idx)); ++idx, ++i) {
			varMap.put(tokens.get(idx), i);
		}

		return idx;
	}



	private static BVAst parseExpression(final List<String> tokens, final int idx,
			final Map<String, Integer> varMap, final List<BVAst> inputs) {

		final int[] idxContainer = { idx };
		return parseExpression(tokens, idxContainer, varMap, inputs);
	}



	private static BVAst parseExpression(final List<String> tokens, final int[] idx,
			final Map<String, Integer> varMap, final List<BVAst> inputs) {

		if (idx[0] >= tokens.size()) {
			throw new IllegalArgumentException("parse error: reached end of String: " + idx[0]);
		}

		if ("(".equals(tokens.get(idx[0]))) {
			idx[0] += 1;

			if (idx[0] >= tokens.size()) {
				throw new IllegalArgumentException("parse error");
			}

			BVAst result = null;

			switch (tokens.get(idx[0])) {
				case "not":
					idx[0] += 1;
					result = mkNot(parseExpression(tokens, idx, varMap, inputs));
					break;

				case "neg":
					idx[0] += 1;
					result = mkNeg(parseExpression(tokens, idx, varMap, inputs));
					break;

				case "ite":
					idx[0] += 1;
					result = mkIte(
							parseBoolExpression(tokens, idx, varMap, inputs),
							parseExpression(tokens, idx, varMap, inputs),
							parseExpression(tokens, idx, varMap, inputs));
					break;

				case "extract": {
					idx[0] += 1;
					int low = -1;
					int high = -1;
					try {
						low = Integer.parseInt(tokens.get(idx[0]));
						high = Integer.parseInt(tokens.get(idx[0] + 1));
					} catch (final Exception e) {
						throw new IllegalArgumentException("parse error", e);
					}
					idx[0] += 2;
					result = mkExtract(low, high, parseExpression(tokens, idx, varMap, inputs));
					break;
				}

				default: {
					BinaryOperator<BVAst> operator = null;

					switch (tokens.get(idx[0])) {
						case "add":
							operator = Builder::mkAdd;
							break;
						case "sub":
							operator = Builder::mkSub;
							break;
						case "mul":
							operator = Builder::mkMul;
							break;
						case "sdiv":
							operator = Builder::mkSDiv;
							break;
						case "srem":
							operator = Builder::mkSRem;
							break;
						case "smod":
							operator = Builder::mkSMod;
							break;
						case "udiv":
							operator = Builder::mkUDiv;
							break;
						case "urem":
							operator = Builder::mkURem;
							break;
						case "umod":
							operator = Builder::mkUMod;
							break;
						case "and":
							operator = Builder::mkAnd;
							break;
						case "or":
							operator = Builder::mkOr;
							break;
						case "xor":
							operator = Builder::mkXor;
							break;
						case "shl":
							operator = Builder::mkShl;
							break;
						case "ashr":
							operator = Builder::mkAshr;
							break;
						case "lshr":
							operator = Builder::mkLshr;
							break;
						case "concat":
							operator = Builder::mkConcat;
							break;
						default:
							throw new IllegalArgumentException("parse error: unexpected " + tokens.get(idx[0]));
					}
					idx[0] += 1;

					result = operator.apply(
							parseExpression(tokens, idx, varMap, inputs),
							parseExpression(tokens, idx, varMap, inputs));
				}
			}

			if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
				throw new IllegalArgumentException("parse error: expected ) at index " + idx[0]);
			}
			idx[0] += 1;

			return result;
		} else if (tokens.get(idx[0]).contains(":")) {
			final String[] splitted = tokens.get(idx[0]).split(":");
			idx[0] += 1;

			try {
				final long value;
				if (splitted[0].startsWith("0x")) {
					value = Long.parseLong(splitted[0].substring(2), 16);
				} else if (splitted[0].startsWith("0b")) {
					value = Long.parseLong(splitted[0].substring(2), 2);
				} else {
					value = Long.parseLong(splitted[0]);
				}
				final int width = Integer.parseInt(splitted[1]);

				return mkBVConst(width, value);
			} catch (final Exception e) {
				throw new IllegalArgumentException("parse error", e);
			}
		} else {
			if (!varMap.containsKey(tokens.get(idx[0]))) {
				throw new IllegalArgumentException("parse error: variable not found: " + tokens.get(idx[0]));
			}

			return inputs.get(varMap.get(tokens.get(idx[0]++)));
		}
	}



	private static BoolAst parseBoolExpression(final List<String> tokens, final int idx,
			final Map<String, Integer> varMap, final List<BVAst> inputs) {

		final int[] idxContainer = { idx };
		return parseBoolExpression(tokens, idxContainer, varMap, inputs);
	}



	private static BoolAst parseBoolExpression(final List<String> tokens, final int[] idx,
			final Map<String, Integer> varMap, final List<BVAst> inputs) {

		if (idx[0] >= tokens.size()) {
			throw new IllegalArgumentException("parse error");
		}

		switch (tokens.get(idx[0])) {
			case "true":
				idx[0] += 1;
				return mkBoolConst(true);

			case "false":
				idx[0] += 1;
				return mkBoolConst(false);

			case "(": {
				idx[0] += 1;

				if (idx[0] >= tokens.size()) {
					throw new IllegalArgumentException("parse error");
				}

				switch (tokens.get(idx[0])) {
					case "and": {
						idx[0] += 1;
						final BoolAst result = mkAnd(
								parseBoolExpression(tokens, idx, varMap, inputs),
								parseBoolExpression(tokens, idx, varMap, inputs));

						if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
							throw new IllegalArgumentException("parse error: expected ) at index " + idx[0]);
						}
						idx[0] += 1;

						return result;
					}

					case "or": {
						idx[0] += 1;
						final BoolAst result = mkOr(
								parseBoolExpression(tokens, idx, varMap, inputs),
								parseBoolExpression(tokens, idx, varMap, inputs));

						if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
							throw new IllegalArgumentException("parse error: expected ) at index " + idx[0]);
						}
						idx[0] += 1;

						return result;
					}

					case "not": {
						idx[0] += 1;
						final BoolAst result = mkNot(parseBoolExpression(tokens, idx, varMap, inputs));

						if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
							throw new IllegalArgumentException("parse error: expected ) at index " + idx[0]);
						}
						idx[0] += 1;

						return result;
					}

					default: {
						BiFunction<BVAst, BVAst, BoolAst> operator = null;

						switch (tokens.get(idx[0])) {
							case "eq":
								operator = Builder::mkEq;
								break;

							case "neq":
								operator = Builder::mkNe;
								break;

							case "sgt":
								operator = Builder::mkSGt;
								break;

							case "sge":
								operator = Builder::mkSGe;
								break;

							case "slt":
								operator = Builder::mkSLt;
								break;

							case "sle":
								operator = Builder::mkSLe;
								break;

							case "ugt":
								operator = Builder::mkUGt;
								break;

							case "uge":
								operator = Builder::mkUGe;
								break;

							case "ult":
								operator = Builder::mkULt;
								break;

							case "ule":
								operator = Builder::mkULe;
								break;

							default:
								throw new IllegalArgumentException("parse error: " + tokens.get(idx[0]));
						}

						idx[0] += 1;
						final BoolAst result = operator.apply(
								parseExpression(tokens, idx, varMap, inputs),
								parseExpression(tokens, idx, varMap, inputs));

						if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
							throw new IllegalArgumentException("parse error: expected ) at index " + idx[0]);
						}
						idx[0] += 1;

						return result;
					}
				}
			}

			default:
				throw new IllegalArgumentException("parse error");
		}
	}
}

