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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class VarReplacer implements TreeTransformer<Ast> {

	private final Map<? extends Ast, Ast> replacements;



	public VarReplacer(final Map<? extends Ast, Ast> replacements) {
		this.replacements = replacements;
	}



	@Override
	public Ast visit(final BoolAllDifferent tree) {
		final List<BVAst> operandsNew = new ArrayList<>();
		for (final BVAst operand : tree.getOperands()) {
			operandsNew.add((BVAst) operand.accept(this));
		}
		return new BoolAllDifferent(operandsNew);
	}



	@Override
	public Ast visit(final BoolBinBV tree) {
		return new BoolBinBV(tree.getKind(),
				(BVAst) tree.getLeft().accept(this),
				(BVAst) tree.getRight().accept(this));
	}



	@Override
	public Ast visit(final BoolBin tree) {
		return new BoolBin(tree.getKind(),
				(BoolAst) tree.getLeft().accept(this),
				(BoolAst) tree.getRight().accept(this));
	}



	@Override
	public Ast visit(final BoolConst tree) {
		return tree;
	}



	@Override
	public Ast visit(final BoolNegate tree) {
		return new BoolNegate((BoolAst) tree.getOperand().accept(this));
	}



	@Override
	public Ast visit(final BoolVar tree) {
		return this.replacements.getOrDefault(tree, tree);
	}



	@Override
	public Ast visit(final BVBinary tree) {
		return new BVBinary(tree.getKind(),
				(BVAst) tree.getLeft().accept(this),
				(BVAst) tree.getRight().accept(this));
	}



	@Override
	public Ast visit(final BVConst tree) {
		return tree;
	}



	@Override
	public Ast visit(final BVExtract tree) {
		return new BVExtract(tree.getLow(), tree.getHigh(), (BVAst) tree.getOperand().accept(this));
	}



	@Override
	public Ast visit(final BVIte tree) {
		return new BVIte(
				(BoolAst) tree.getCondition().accept(this),
				(BVAst) tree.getThenExpr().accept(this),
				(BVAst) tree.getElseExpr().accept(this));
	}



	@Override
	public Ast visit(final BVUnary tree) {
		return new BVUnary(tree.getKind(), (BVAst) tree.getOperand().accept(this));
	}



	@Override
	public Ast visit(final BVVar tree) {
		return this.replacements.getOrDefault(tree, tree);
	}
}

