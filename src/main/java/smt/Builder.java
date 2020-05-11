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
package smt;

import java.util.List;



public final class Builder {

	private Builder() { }



	public static BoolAst mkBoolVar(final String name) {
		return new BoolVar(name);
	}



	public static BoolAst mkBoolConst(final boolean value) {
		return new BoolConst(value);
	}



	public static BoolAst mkEq(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.EQUALS, left, right);
	}



	public static BoolAst mkNe(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.DISTINCT, left, right);
	}



	public static BoolAst mkUGt(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.UGT, left, right);
	}



	public static BoolAst mkUGe(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.UGE, left, right);
	}



	public static BoolAst mkULt(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.ULT, left, right);
	}



	public static BoolAst mkULe(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.ULE, left, right);
	}



	public static BoolAst mkSGt(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.SGT, left, right);
	}



	public static BoolAst mkSGe(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.SGE, left, right);
	}



	public static BoolAst mkSLt(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.SLT, left, right);
	}



	public static BoolAst mkSLe(final BVAst left, final BVAst right) {
		return new BoolBinBV(BoolBVOp.SLE, left, right);
	}



	public static BoolAst mkEq(final BoolAst left, final BoolAst right) {
		return new BoolBin(BoolBinOp.EQUALS, left, right);
	}



	public static BoolAst mkNe(final BoolAst left, final BoolAst right) {
		return new BoolBin(BoolBinOp.DISTINCT, left, right);
	}



	public static BoolAst mkImplies(final BoolAst left, final BoolAst right) {
		return new BoolBin(BoolBinOp.IMPLIES, left, right);
	}



	public static BoolAst mkAnd(final BoolAst left, final BoolAst right) {
		return new BoolBin(BoolBinOp.AND, left, right);
	}



	public static BoolAst mkAnd(final BoolAst ... children) {
		BoolAst result = children[0];
		for (int i = 1; i < children.length; ++i) {
			result = mkAnd(result, children[i]);
		}
		return result;
	}



	public static BoolAst mkOr(final BoolAst left, final BoolAst right) {
		return new BoolBin(BoolBinOp.OR, left, right);
	}



	public static BoolAst mkOr(final BoolAst ... children) {
		BoolAst result = children[0];
		for (int i = 1; i < children.length; ++i) {
			result = mkOr(result, children[i]);
		}
		return result;
	}



	public static BoolAst mkOr(final List<BoolAst> children) {
		BoolAst result = children.get(0);
		for (int i = 1; i < children.size(); ++i) {
			result = mkOr(result, children.get(i));
		}
		return result;
	}



	public static BoolAst mkNot(final BoolAst operand) {
		return new BoolNegate(operand);
	}



	public static BoolAst mkAllDifferent(final BVAst ... operands) {
		return new BoolAllDifferent(operands);
	}



	public static BoolAst mkAllDifferent(final List<BVAst> operands) {
		return new BoolAllDifferent(operands);
	}



	public static BVAst mkBVVar(final int width, final String name) {
		return new BVVar(width, name);
	}



	public static BVAst mkBVConst(final int width, final long value) {
		return new BVConst(width, value);
	}



	public static BVAst mkBVConst(final BitVector value) {
		return new BVConst(value);
	}



	public static BVAst mkAdd(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.PLUS, left, right);
	}



	public static BVAst mkSub(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.MINUS, left, right);
	}



	public static BVAst mkMul(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.MUL, left, right);
	}



	public static BVAst mkSDiv(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.SDIV, left, right);
	}



	public static BVAst mkUDiv(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.UDIV, left, right);
	}



	public static BVAst mkSRem(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.SREM, left, right);
	}



	public static BVAst mkURem(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.UREM, left, right);
	}



	public static BVAst mkSMod(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.SMOD, left, right);
	}



	public static BVAst mkUMod(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.UMOD, left, right);
	}



	public static BVAst mkAnd(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.AND, left, right);
	}



	public static BVAst mkOr(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.OR, left, right);
	}



	public static BVAst mkXor(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.XOR, left, right);
	}



	public static BVAst mkShl(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.SHL, left, right);
	}



	public static BVAst mkAshr(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.ASHR, left, right);
	}



	public static BVAst mkLshr(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.LSHR, left, right);
	}



	public static BVAst mkRol(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.ROL, left, right);
	}



	public static BVAst mkRor(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.ROR, left, right);
	}



	public static BVAst mkConcat(final BVAst left, final BVAst right) {
		return new BVBinary(BVBinOp.CONCAT, left, right);
	}



	public static BVAst mkNot(final BVAst operand) {
		return new BVUnary(BVUnaryOp.NOT, operand);
	}



	public static BVAst mkNeg(final BVAst operand) {
		return new BVUnary(BVUnaryOp.NEG, operand);
	}



	public static BVAst mkIte(final BoolAst condition, final BVAst thenExpr, final BVAst elseExpr) {
		return new BVIte(condition, thenExpr, elseExpr);
	}



	public static BVAst mkExtract(final int low, final int high, final BVAst operand) {
		return new BVExtract(low, high, operand);
	}
}

