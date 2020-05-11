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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import smt.BitVector;
import smt.Builder;
import smt.BVAst;



public class LibraryFunction extends AbstractInOut {

	private final String name;
	private final List<Integer> auxiliaryVariables;
	private final boolean isIdempotent;
	private final Function<List<BVAst>, BVAst> function;



	public LibraryFunction(final String name, final int numberOfInputs,
			final Function<List<BVAst>, BVAst> function) {
		this(name, numberOfInputs, false, function);
	}



	public LibraryFunction(final String name, final int numberOfInputs, final boolean isIdempotent,
			final Function<List<BVAst>, BVAst> function) {
		super(numberOfInputs);
		this.name = name;
		this.function = function;
		this.isIdempotent = isIdempotent;
		this.auxiliaryVariables = Collections.emptyList();
	}



	public LibraryFunction(final String name, final List<Integer> inputBitWidths,
			final int outputBitWidth, final Function<List<BVAst>, BVAst> function) {
		this(name, inputBitWidths, outputBitWidth, Collections.emptyList(), function);
	}



	public LibraryFunction(final String name, final List<Integer> inputBitWidths,
			final int outputBitWidth, final boolean isIdempotent,
			final Function<List<BVAst>, BVAst> function) {
		this(name, inputBitWidths, outputBitWidth, Collections.emptyList(), isIdempotent, function);
	}



	public LibraryFunction(final String name, final List<Integer> inputBitWidths,
			final int outputBitWidth, final List<Integer> auxiliaryVariables,
			final Function<List<BVAst>, BVAst> function) {
		this(name, inputBitWidths, outputBitWidth, auxiliaryVariables, false, function);
	}



	public LibraryFunction(final String name, final List<Integer> inputBitWidths,
			final int outputBitWidth, final List<Integer> auxiliaryVariables,
			final boolean isIdempotent, final Function<List<BVAst>, BVAst> function) {
		super(inputBitWidths, outputBitWidth);
		this.name = name;
		this.auxiliaryVariables = auxiliaryVariables;
		this.isIdempotent = isIdempotent;
		this.function = function;
	}



	public String getName() {
		return this.name;
	}



	public int getNumberOfAuxiliaryVariables() {
		return this.auxiliaryVariables.size();
	}



	public int getWidthOfAuxiliaryVariable(final int index) {
		return this.auxiliaryVariables.get(index);
	}



	public boolean isIdempotent() {
		return this.isIdempotent;
	}



	public Function<List<BVAst>, BVAst> getFunction() {
		return this.function;
	}



	public static LibraryFunction getConst(final BitVector value) {
		return new LibraryFunction("const" + value, Collections.emptyList(), value.getWidth(),
				inputs -> Builder.mkBVConst(value));
	}



	public static LibraryFunction getConst(final int width, final long value) {
		return new LibraryFunction("const" + value, Collections.emptyList(), width,
				inputs -> Builder.mkBVConst(width, value));
	}



	public static LibraryFunction getArbitraryConst(final int width) {
		return new LibraryFunction("const", Collections.emptyList(), width,
				Collections.singletonList(width), inputs -> inputs.get(0));
	}



	public static LibraryFunction getAdd() {
		return new LibraryFunction("add", 2, inputs -> Builder.mkAdd(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getAdd(final int width) {
		return new LibraryFunction("add", Collections.nCopies(2, width), width,
				inputs -> Builder.mkAdd(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSub() {
		return new LibraryFunction("sub", 2, inputs -> Builder.mkSub(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSub(final int width) {
		return new LibraryFunction("sub", Collections.nCopies(2, width), width,
				inputs -> Builder.mkSub(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getMul() {
		return new LibraryFunction("mul", 2, inputs -> Builder.mkMul(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getMul(final int width) {
		return new LibraryFunction("mul", Collections.nCopies(2, width), width,
				inputs -> Builder.mkMul(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSDiv() {
		return new LibraryFunction("sdiv", 2, inputs -> Builder.mkSDiv(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSDiv(final int width) {
		return new LibraryFunction("sdiv", Collections.nCopies(2, width), width,
				inputs -> Builder.mkSDiv(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getUDiv() {
		return new LibraryFunction("udiv", 2, inputs -> Builder.mkUDiv(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getUDiv(final int width) {
		return new LibraryFunction("udiv", Collections.nCopies(2, width), width,
				inputs -> Builder.mkUDiv(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSRem() {
		return new LibraryFunction("srem", 2, inputs -> Builder.mkSRem(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSRem(final int width) {
		return new LibraryFunction("srem", Collections.nCopies(2, width), width,
				inputs -> Builder.mkSRem(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getURem() {
		return new LibraryFunction("urem", 2, inputs -> Builder.mkURem(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getURem(final int width) {
		return new LibraryFunction("urem", Collections.nCopies(2, width), width,
				inputs -> Builder.mkURem(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSMod() {
		return new LibraryFunction("smod", 2, inputs -> Builder.mkSMod(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getSMod(final int width) {
		return new LibraryFunction("smod", Collections.nCopies(2, width), width,
				inputs -> Builder.mkSMod(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getUMod() {
		return new LibraryFunction("umod", 2, inputs -> Builder.mkUMod(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getUMod(final int width) {
		return new LibraryFunction("umod", Collections.nCopies(2, width), width,
				inputs -> Builder.mkUMod(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getAnd() {
		return new LibraryFunction("and", 2, true,
				inputs -> Builder.mkAnd(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getAnd(final int width) {
		return new LibraryFunction("and", Collections.nCopies(2, width), width, true,
				inputs -> Builder.mkAnd(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getOr() {
		return new LibraryFunction("or", 2, true,
				inputs -> Builder.mkOr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getOr(final int width) {
		return new LibraryFunction("or", Collections.nCopies(2, width), width, true,
				inputs -> Builder.mkOr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getXor() {
		return new LibraryFunction("xor", 2, inputs -> Builder.mkXor(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getXor(final int width) {
		return new LibraryFunction("xor", Collections.nCopies(2, width), width,
				inputs -> Builder.mkXor(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getShl() {
		return new LibraryFunction("shl", 2, inputs -> Builder.mkShl(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getShl(final int width) {
		return new LibraryFunction("shl", Collections.nCopies(2, width), width,
				inputs -> Builder.mkShl(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getAshr() {
		return new LibraryFunction("ashr", 2, inputs -> Builder.mkAshr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getAshr(final int width) {
		return new LibraryFunction("ashr", Collections.nCopies(2, width), width,
				inputs -> Builder.mkAshr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getLshr() {
		return new LibraryFunction("lshr", 2, inputs -> Builder.mkLshr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getLshr(final int width) {
		return new LibraryFunction("lshr", Collections.nCopies(2, width), width,
				inputs -> Builder.mkLshr(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getShlJava() {
		return new LibraryFunction("shl", 2, inputs -> Builder.mkShl(inputs.get(0),
				Builder.mkAnd(inputs.get(1), Builder.mkBVConst(inputs.get(1).getWidth(),
					inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getShlJava(final int width) {
		return new LibraryFunction("shl", Collections.nCopies(2, width), width,
				inputs -> Builder.mkShl(inputs.get(0), Builder.mkAnd(inputs.get(1),
					Builder.mkBVConst(inputs.get(1).getWidth(), inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getAshrJava() {
		return new LibraryFunction("ashr", 2, inputs -> Builder.mkAshr(inputs.get(0),
				Builder.mkAnd(inputs.get(1), Builder.mkBVConst(inputs.get(1).getWidth(),
					inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getAshrJava(final int width) {
		return new LibraryFunction("ashr", Collections.nCopies(2, width), width,
				inputs -> Builder.mkAshr(inputs.get(0), Builder.mkAnd(inputs.get(1),
					Builder.mkBVConst(inputs.get(1).getWidth(), inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getLshrJava() {
		return new LibraryFunction("lshr", 2, inputs -> Builder.mkLshr(inputs.get(0),
				Builder.mkAnd(inputs.get(1), Builder.mkBVConst(inputs.get(1).getWidth(),
					inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getLshrJava(final int width) {
		return new LibraryFunction("lshr", Collections.nCopies(2, width), width,
				inputs -> Builder.mkLshr(inputs.get(0), Builder.mkAnd(inputs.get(1),
					Builder.mkBVConst(inputs.get(1).getWidth(), inputs.get(1).getWidth() - 1))));
	}



	public static LibraryFunction getNot() {
		return new LibraryFunction("not", 1, inputs -> Builder.mkNot(inputs.get(0)));
	}



	public static LibraryFunction getNot(final int width) {
		return new LibraryFunction("not", Collections.singletonList(width), width,
				inputs -> Builder.mkNot(inputs.get(0)));
	}



	public static LibraryFunction getNeg() {
		return new LibraryFunction("neg", 1, inputs -> Builder.mkNeg(inputs.get(0)));
	}



	public static LibraryFunction getNeg(final int width) {
		return new LibraryFunction("neg", Collections.singletonList(width), width,
				inputs -> Builder.mkNeg(inputs.get(0)));
	}



	public static LibraryFunction getSExt(final int origWidth, final int resWidth) {
		if (resWidth <= origWidth) {
			throw new IllegalArgumentException();
		}
		return new LibraryFunction("sext", Collections.singletonList(origWidth), resWidth,
				inputs -> Builder.mkConcat(Builder.mkIte(
					Builder.mkSLt(inputs.get(0), Builder.mkBVConst(origWidth, 0)),
					Builder.mkBVConst(resWidth - origWidth, -1L),
					Builder.mkBVConst(resWidth - origWidth, 0L)), inputs.get(0)));
	}



	public static LibraryFunction getZExt(final int origWidth, final int resWidth) {
		if (resWidth <= origWidth) {
			throw new IllegalArgumentException();
		}
		return new LibraryFunction("zext", Collections.singletonList(origWidth), resWidth,
				inputs -> Builder.mkConcat(Builder.mkBVConst(resWidth - origWidth, 0L), inputs.get(0)));
	}



	public static LibraryFunction getConcat(final int leftWidth, final int rightWidth) {
		return new LibraryFunction("concat", Arrays.asList(leftWidth, rightWidth),
				leftWidth + rightWidth, inputs -> Builder.mkConcat(inputs.get(0), inputs.get(1)));
	}



	public static LibraryFunction getExtract(final int origWidth, final int low, final int high) {
		if (high - low + 1 >= origWidth) {
			throw new IllegalArgumentException();
		}
		return new LibraryFunction("extract(" + low + "," + high + ")",
				Collections.singletonList(origWidth),
				high - low + 1,
				inputs -> Builder.mkExtract(low, high, inputs.get(0)));
	}



	public static LibraryFunction getIte(final int width) {
		return new LibraryFunction("ite", Arrays.asList(1, width, width), width, true,
				inputs -> Builder.mkIte(Builder.mkEq(inputs.get(0), Builder.mkBVConst(1, 1)),
					inputs.get(1), inputs.get(2)));
	}



	public static LibraryFunction getEq(final int width) {
		return new LibraryFunction("eq", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkEq(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getEqBV(final int width) {
		return new LibraryFunction("eq", 2, inputs -> Builder.mkIte(
				Builder.mkEq(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getNeq(final int width) {
		return new LibraryFunction("neq", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkNe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getNeqBV(final int width) {
		return new LibraryFunction("neq", 2, inputs -> Builder.mkIte(
				Builder.mkNe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getUGt(final int width) {
		return new LibraryFunction("ugt", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkUGt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getUGtBV(final int width) {
		return new LibraryFunction("ugt", 2, inputs -> Builder.mkIte(
				Builder.mkUGt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getUGe(final int width) {
		return new LibraryFunction("uge", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkUGe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getUGeBV(final int width) {
		return new LibraryFunction("uge", 2, inputs -> Builder.mkIte(
				Builder.mkUGe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getULt(final int width) {
		return new LibraryFunction("ult", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkULt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getULtBV(final int width) {
		return new LibraryFunction("ult", 2, inputs -> Builder.mkIte(
				Builder.mkULt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getULe(final int width) {
		return new LibraryFunction("ule", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkULe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getULeBV(final int width) {
		return new LibraryFunction("ule", 2, inputs -> Builder.mkIte(
				Builder.mkULe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getSGt(final int width) {
		return new LibraryFunction("sgt", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkSGt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getSGtBV(final int width) {
		return new LibraryFunction("sgt", 2, inputs -> Builder.mkIte(
				Builder.mkSGt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getSGe(final int width) {
		return new LibraryFunction("sge", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkSGe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getSGeBV(final int width) {
		return new LibraryFunction("sge", 2, inputs -> Builder.mkIte(
				Builder.mkSGe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getSLt(final int width) {
		return new LibraryFunction("slt", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkSLt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getSLtBV(final int width) {
		return new LibraryFunction("slt", 2, inputs -> Builder.mkIte(
				Builder.mkSLt(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}



	public static LibraryFunction getSLe(final int width) {
		return new LibraryFunction("sle", Collections.nCopies(2, width), 1, inputs -> Builder.mkIte(
				Builder.mkSLe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(1, 1),
				Builder.mkBVConst(1, 0)));
	}



	public static LibraryFunction getSLeBV(final int width) {
		return new LibraryFunction("sle", 2, inputs -> Builder.mkIte(
				Builder.mkSLe(inputs.get(0), inputs.get(1)),
				Builder.mkBVConst(width, 1),
				Builder.mkBVConst(width, 0)));
	}
}

