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
package yices;

import static yices.Yices.*;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

import smt.*;



public final class YicesSolver implements Solver {

	private final long context;
	private final OptionalLong timeout;
	private final boolean incremental;
	private long model;

	private final Map<String, Integer> variableCache = new HashMap<>();



	private final class YicesTransformer implements TreeTransformer<Integer> {

		@Override
		public Integer visit(BoolAllDifferent tree) {
			final int[] convOperands = new int[tree.getOperands().size()];
			for (int i = 0; i < convOperands.length; ++i) {
				convOperands[i] = tree.getOperands().get(i).accept(this);
			}
			return yices_distinct(convOperands);
		}


		@Override
		public Integer visit(BoolBinBV tree) {
			switch (tree.getKind()) {
				case EQUALS:
					return yices_bveq_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case DISTINCT:
					return yices_bvneq_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case UGT:
					return yices_bvgt_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case UGE:
					return yices_bvge_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case ULT:
					return yices_bvlt_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case ULE:
					return yices_bvle_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SGT:
					return yices_bvsgt_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SGE:
					return yices_bvsge_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SLT:
					return yices_bvslt_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SLE:
					return yices_bvsle_atom(tree.getLeft().accept(this), tree.getRight().accept(this));

				default:
					throw new IllegalStateException();
			}
		}


		@Override
		public Integer visit(BoolBin tree) {
			switch (tree.getKind()) {
				case EQUALS:
					return yices_eq(tree.getLeft().accept(this), tree.getRight().accept(this));

				case DISTINCT:
					return yices_neq(tree.getLeft().accept(this), tree.getRight().accept(this));

				case IMPLIES:
					return yices_implies(tree.getLeft().accept(this), tree.getRight().accept(this));

				case AND:
					return yices_and2(tree.getLeft().accept(this), tree.getRight().accept(this));

				case OR:
					return yices_or2(tree.getLeft().accept(this), tree.getRight().accept(this));

				default:
					throw new IllegalStateException();
			}
		}


		@Override
		public Integer visit(BoolConst tree) {
			if (tree.getValue()) {
				return yices_true();
			} else {
				return yices_false();
			}
		}


		@Override
		public Integer visit(BoolNegate tree) {
			return yices_not(tree.getOperand().accept(this));
		}


		@Override
		public Integer visit(BoolVar tree) {
			return variableCache.computeIfAbsent(tree.getName(),
					name -> yices_new_uninterpreted_term(yices_bool_type()));
		}


		@Override
		public Integer visit(BVBinary tree) {
			switch (tree.getKind()) {
				case PLUS:
					return yices_bvadd(tree.getLeft().accept(this), tree.getRight().accept(this));

				case MINUS:
					return yices_bvsub(tree.getLeft().accept(this), tree.getRight().accept(this));

				case MUL:
					return yices_bvmul(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SDIV:
					return yices_bvsdiv(tree.getLeft().accept(this), tree.getRight().accept(this));

				case UDIV:
					return yices_bvdiv(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SREM:
					return yices_bvsrem(tree.getLeft().accept(this), tree.getRight().accept(this));

				case UREM:
					return yices_bvrem(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SMOD:
					return yices_bvsmod(tree.getLeft().accept(this), tree.getRight().accept(this));

				case UMOD:
					return yices_bvrem(tree.getLeft().accept(this), tree.getRight().accept(this));

				case AND:
					return yices_bvand2(tree.getLeft().accept(this), tree.getRight().accept(this));

				case OR:
					return yices_bvor2(tree.getLeft().accept(this), tree.getRight().accept(this));

				case XOR:
					return yices_bvxor2(tree.getLeft().accept(this), tree.getRight().accept(this));

				case SHL:
					return yices_bvshl(tree.getLeft().accept(this), tree.getRight().accept(this));

				case ASHR:
					return yices_bvashr(tree.getLeft().accept(this), tree.getRight().accept(this));

				case LSHR:
					return yices_bvlshr(tree.getLeft().accept(this), tree.getRight().accept(this));

				case ROL: {
					final int value = tree.getLeft().accept(this);
					final int count = tree.getRight().accept(this);
					return yices_bvor2(yices_bvshl(value, count), yices_bvlshr(value,
							yices_bvsub(yices_bvconst_int32(tree.getWidth(), tree.getWidth()), count)));
				}

				case ROR: {
					final int value = tree.getLeft().accept(this);
					final int count = tree.getRight().accept(this);
					return yices_bvor2(yices_bvlshr(value, count), yices_bvshl(value,
							yices_bvsub(yices_bvconst_int32(tree.getWidth(), tree.getWidth()), count)));
				}

				case CONCAT:
					return yices_bvconcat2(tree.getLeft().accept(this), tree.getRight().accept(this));

				default:
					throw new IllegalStateException();
			}
		}


		@Override
		public Integer visit(BVConst tree) {
			return yices_parse_bvbin(tree.getValue().toBinaryString());
		}


		@Override
		public Integer visit(BVExtract tree) {
			return yices_bvextract(tree.getOperand().accept(this), tree.getLow(), tree.getHigh());
		}


		@Override
		public Integer visit(BVIte tree) {
			return yices_ite(
					tree.getCondition().accept(this),
					tree.getThenExpr().accept(this),
					tree.getElseExpr().accept(this));
		}


		@Override
		public Integer visit(BVUnary tree) {
			switch (tree.getKind()) {
				case NOT:
					return yices_bvnot(tree.getOperand().accept(this));

				case NEG:
					return yices_bvneg(tree.getOperand().accept(this));

				default:
					throw new IllegalStateException();
			}
		}


		@Override
		public Integer visit(BVVar tree) {
			return variableCache.computeIfAbsent(tree.getName(),
					name -> yices_new_uninterpreted_term(yices_bv_type(tree.getWidth())));
		}
	}



	static {
		yices_init();
	}



	public YicesSolver() {
		this(true);
	}



	public YicesSolver(final boolean incremental) {
		this(OptionalLong.empty(), incremental);
	}



	public YicesSolver(final long timeout) {
		this(timeout, true);
	}



	public YicesSolver(final long timeout, final boolean incremental) {
		this(OptionalLong.of(timeout), incremental);
		if (timeout <= 0) {
			throw new IllegalArgumentException();
		}
	}



	private YicesSolver(final OptionalLong timeout, final boolean incremental) {
		final long config = yices_new_config();
		yices_default_config_for_logic(config, "QF_BV");
		if (incremental) {
			yices_set_config(config, "mode", "multi-checks");
		} else {
			yices_set_config(config, "mode", "one-shot");
		}
		this.incremental = incremental;
		this.context = yices_new_context(config);
		yices_free_config(config);
		this.timeout = timeout;
	}



	public static void freeAll() {
		yices_reset();
	}



	@Override
	public void add(final BoolAst constraint) {
		yices_assert_formula(this.context, constraint.accept(new YicesTransformer()));
	}



	@Override
	public SatResult checkSat() {
		releaseModel();

		Thread timeoutThread = null;
		if (this.timeout.isPresent()) {
			timeoutThread = startTimeoutThread(this.timeout.getAsLong());
		}

		long params = 0;
		if (this.incremental) {
			params = yices_new_param_record();
			yices_default_params_for_context(this.context, params);
			yices_set_param(params, "var-decay", "0.91");
		}

		try {
			switch (yices_check_context(this.context, params)) {
				case 3:
					return SatResult.SAT;

				case 4:
					return SatResult.UNSAT;

				default:
					return SatResult.UNKNOWN;
			}
		} finally {
			if (params != 0) {
				yices_free_param_record(params);
			}
			if (timeoutThread != null && timeoutThread.isAlive()) {
				timeoutThread.interrupt();
			}
		}
	}



	@Override
	public BitVector getBVAssignment(final BVAst tree) {
		if (this.model == 0) {
			this.model = yices_get_model(this.context, 1);
			if (this.model == 0) {
				throw new IllegalStateException("not in SAT state");
			}
		}

		final int convertedTree = tree.accept(new YicesTransformer());
		final int[] data = new int[yices_term_bitsize(convertedTree)];

		if (yices_get_bv_value(this.model, convertedTree, data) != 0) {
			throw new IllegalArgumentException();
		}

		return new BitVector(data);
	}



	@Override
	public boolean getBoolAssignment(final BoolAst tree) {
		if (this.model == 0) {
			this.model = yices_get_model(this.context, 1);
			if (this.model == 0) {
				throw new IllegalStateException("not in SAT state");
			}
		}

		final int convertedTree = tree.accept(new YicesTransformer());
		final int[] data = new int[1];

		if (yices_get_bool_value(this.model, convertedTree, data) != 0) {
			throw new IllegalArgumentException();
		}

		return data[0] != 0;
	}



	@Override
	public void push() {
		yices_push(this.context);
	}



	@Override
	public void pop() {
		yices_pop(this.context);
	}



	private void releaseModel() {
		if (this.model != 0) {
			yices_free_model(this.model);
			this.model = 0;
		}
	}



	private Thread startTimeoutThread(final long timeoutValue) {
		final Thread timeoutThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(timeoutValue);
					yices_stop_search(YicesSolver.this.context);
				} catch (final InterruptedException e) { }
			}
		};
		timeoutThread.setDaemon(true);
		timeoutThread.start();
		return timeoutThread;
	}
}

