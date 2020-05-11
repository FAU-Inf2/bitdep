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



public interface TreeTransformer<R> {
	R visit(BoolAllDifferent tree);
	R visit(BoolBinBV tree);
	R visit(BoolBin tree);
	R visit(BoolConst tree);
	R visit(BoolNegate tree);
	R visit(BoolVar tree);
	R visit(BVBinary tree);
	R visit(BVConst tree);
	R visit(BVExtract tree);
	R visit(BVIte tree);
	R visit(BVUnary tree);
	R visit(BVVar tree);
}

