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



final class Yices {

	private Yices() { }



	static {
		System.loadLibrary("yicesjni");
	}



	native static void yices_init();
	native static void yices_exit();
	native static void yices_reset();
	native static int yices_error_code();
	native static String yices_error_string();
	native static int yices_bool_type();
	native static int yices_bv_type(int size);
	native static int yices_true();
	native static int yices_false();
	native static int yices_ite(int cond, int then_term, int else_term);
	native static int yices_eq(int left, int right);
	native static int yices_neq(int left, int right);
	native static int yices_not(int arg);
	native static int yices_or2(int t1, int t2);
	native static int yices_and2(int t1, int t2);
	native static int yices_xor2(int t1, int t2);
	native static int yices_iff(int t1, int t2);
	native static int yices_implies(int t1, int t2);
	native static int yices_distinct(int[] arg);
	native static int yices_bvconst_int32(int n, int x);
	native static int yices_bvconst_int64(int n, long x);
	native static int yices_parse_bvbin(String s);
	native static int yices_new_uninterpreted_term(int tau);
	native static int yices_bvadd(int t1, int t2);
	native static int yices_bvsub(int t1, int t2);
	native static int yices_bvneg(int t1);
	native static int yices_bvmul(int t1, int t2);
	native static int yices_bvdiv(int t1, int t2);
	native static int yices_bvrem(int t1, int t2);
	native static int yices_bvsdiv(int t1, int t2);
	native static int yices_bvsrem(int t1, int t2);
	native static int yices_bvsmod(int t1, int t2);
	native static int yices_bvnot(int t1);
	native static int yices_bvshl(int t1, int t2);
	native static int yices_bvlshr(int t1, int t2);
	native static int yices_bvashr(int t1, int t2);
	native static int yices_bvand2(int t1, int t2);
	native static int yices_bvor2(int t1, int t2);
	native static int yices_bvxor2(int t1, int t2);
	native static int yices_bvextract(int t, int i, int j);
	native static int yices_bvconcat2(int t1, int t2);
	native static int yices_sign_extend(int t, int n);
	native static int yices_zero_extend(int t, int n);
	native static int yices_bveq_atom(int t1, int t2);
	native static int yices_bvneq_atom(int t1, int t2);
	native static int yices_bvge_atom(int t1, int t2);
	native static int yices_bvgt_atom(int t1, int t2);
	native static int yices_bvle_atom(int t1, int t2);
	native static int yices_bvlt_atom(int t1, int t2);
	native static int yices_bvsge_atom(int t1, int t2);
	native static int yices_bvsgt_atom(int t1, int t2);
	native static int yices_bvsle_atom(int t1, int t2);
	native static int yices_bvslt_atom(int t1, int t2);
	native static boolean yices_term_is_bool(int t);
	native static boolean yices_term_is_bitvector(int t);
	native static int yices_term_bitsize(int t);
	native static long yices_new_config();
	native static void yices_free_config(long config);
	native static int yices_set_config(long config, String name, String value);
	native static int yices_default_config_for_logic(long config, String logic);
	native static long yices_new_context(long config);
	native static void yices_free_context(long ctx);
	native static int yices_context_status(long ctx);
	native static int yices_push(long ctx);
	native static int yices_pop(long ctx);
	native static int yices_context_enable_option(long ctx, String option);
	native static int yices_context_disable_option(long ctx, String option);
	native static int yices_assert_formula(long ctx, int t);
	native static int yices_check_context(long ctx, long params);
	native static void yices_stop_search(long ctx);
	native static long yices_new_param_record();
	native static void yices_default_params_for_context(long ctx, long params);
	native static int yices_set_param(long p, String pname, String value);
	native static void yices_free_param_record(long param);
	native static long yices_get_model(long ctx, int keep_subst);
	native static void yices_free_model(long mdl);
	native static int yices_get_bool_value(long mdl, int t, int[] val);
	native static int yices_get_bv_value(long mdl, int t, int[] val);
}

