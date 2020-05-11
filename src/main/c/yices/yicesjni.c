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
#include "yices.h"
#include "yicesjni.h"


JNIEXPORT void Java_yices_Yices_yices_1init(JNIEnv *env, jclass cls) {
	yices_init();
}

JNIEXPORT void Java_yices_Yices_yices_1exit(JNIEnv *env, jclass cls) {
	yices_exit();
}

JNIEXPORT void Java_yices_Yices_yices_1reset(JNIEnv *env, jclass cls) {
	yices_reset();
}

JNIEXPORT jint Java_yices_Yices_yices_1error_1code(JNIEnv *env, jclass cls) {
	return (jint) yices_error_code();
}

JNIEXPORT jstring Java_yices_Yices_yices_1error_1string(JNIEnv *env, jclass cls) {
	char *errStr = yices_error_string();
	jstring result = (*env)->NewStringUTF(env, errStr);
	yices_free_string(errStr);
	return result;
}

JNIEXPORT jint Java_yices_Yices_yices_1bool_1type(JNIEnv *env, jclass cls) {
	return (jint) yices_bool_type();
}

JNIEXPORT jint Java_yices_Yices_yices_1bv_1type(JNIEnv *env, jclass cls, jint size) {
	return (jint) yices_bv_type((uint32_t) size);
}

JNIEXPORT jint Java_yices_Yices_yices_1true(JNIEnv *env, jclass cls) {
	return (jint) yices_true();
}

JNIEXPORT jint Java_yices_Yices_yices_1false(JNIEnv *env, jclass cls) {
	return (jint) yices_false();
}

JNIEXPORT jint Java_yices_Yices_yices_1ite(JNIEnv *env, jclass cls, jint cond, jint then_term,
		jint else_term) {
	return (jint) yices_ite((term_t) cond, (term_t) then_term, (term_t) else_term);
}

JNIEXPORT jint Java_yices_Yices_yices_1eq(JNIEnv *env, jclass cls, jint left, jint right) {
	return (jint) yices_eq((term_t) left, (term_t) right);
}

JNIEXPORT jint Java_yices_Yices_yices_1neq(JNIEnv *env, jclass cls, jint left, jint right) {
	return (jint) yices_neq((term_t) left, (term_t) right);
}

JNIEXPORT jint Java_yices_Yices_yices_1not(JNIEnv *env, jclass cls, jint arg) {
	return (jint) yices_not((term_t) arg);
}

JNIEXPORT jint Java_yices_Yices_yices_1or2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_or2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1and2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_and2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1xor2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_xor2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1iff(JNIEnv *env, jclass cls, jint left, jint right) {
	return (jint) yices_iff((term_t) left, (term_t) right);
}

JNIEXPORT jint Java_yices_Yices_yices_1implies(JNIEnv *env, jclass cls, jint left, jint right) {
	return (jint) yices_implies((term_t) left, (term_t) right);
}

JNIEXPORT jint Java_yices_Yices_yices_1distinct(JNIEnv *env, jclass cls, jintArray arg) {
	term_t *arg_ptr = (*env)->GetIntArrayElements(env, arg, NULL);
	int32_t result = yices_distinct((uint32_t) (*env)->GetArrayLength(env, arg), arg_ptr);
	(*env)->ReleaseIntArrayElements(env, arg, arg_ptr, 0);
	return (jint) result;
}

JNIEXPORT jint Java_yices_Yices_yices_1bvconst_1int32(JNIEnv *env, jclass cls, jint n, jint x) {
	return (jint) yices_bvconst_int32((uint32_t) n, (int32_t) x);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvconst_1int64(JNIEnv *env, jclass cls, jint n, jlong x) {
	return (jint) yices_bvconst_int64((uint32_t) n, (int64_t) x);
}

JNIEXPORT jint Java_yices_Yices_yices_1parse_1bvbin(JNIEnv *env, jclass cls, jstring s) {
	const char *sNative = (*env)->GetStringUTFChars(env, s, 0);
	const term_t result = yices_parse_bvbin(sNative);
	(*env)->ReleaseStringUTFChars(env, s, sNative);
	return (jint) result;
}

JNIEXPORT jint Java_yices_Yices_yices_1new_1uninterpreted_1term(JNIEnv *env, jclass cls, jint tau) {
	return (jint) yices_new_uninterpreted_term((type_t) tau);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvadd(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvadd((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsub(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsub((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvneg(JNIEnv *env, jclass cls, jint t1) {
	return (jint) yices_bvneg((term_t) t1);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvmul(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvmul((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvdiv(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvdiv((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvrem(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvrem((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsdiv(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsdiv((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsrem(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsrem((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsmod(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsmod((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvnot(JNIEnv *env, jclass cls, jint t1) {
	return (jint) yices_bvnot((term_t) t1);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvshl(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvshl((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvlshr(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvlshr((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvashr(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvashr((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvand2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvand2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvor2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvor2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvxor2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvxor2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvextract(JNIEnv *env, jclass cls, jint t, jint i, jint j) {
	return (jint) yices_bvextract((term_t) t, (uint32_t) i, (uint32_t) j);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvconcat2(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvconcat2((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1sign_1extend(JNIEnv *env, jclass cls, jint t, jint n) {
	return (jint) yices_sign_extend((term_t) t, (uint32_t) n);
}

JNIEXPORT jint Java_yices_Yices_yices_1zero_1extend(JNIEnv *env, jclass cls, jint t, jint n) {
	return (jint) yices_zero_extend((term_t) t, (uint32_t) n);
}

JNIEXPORT jint Java_yices_Yices_yices_1bveq_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bveq_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvneq_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvneq_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvge_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvge_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvgt_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvgt_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvle_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvle_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvlt_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvlt_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsge_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsge_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsgt_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsgt_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvsle_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvsle_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jint Java_yices_Yices_yices_1bvslt_1atom(JNIEnv *env, jclass cls, jint t1, jint t2) {
	return (jint) yices_bvslt_atom((term_t) t1, (term_t) t2);
}

JNIEXPORT jboolean Java_yices_Yices_yices_1term_1is_1bool(JNIEnv *env, jclass cls, jint t) {
	return (jboolean) yices_term_is_bool((term_t) t);
}

JNIEXPORT jboolean Java_yices_Yices_yices_1term_1is_1bitvector(JNIEnv *env, jclass cls, jint t) {
	return (jboolean) yices_term_is_bitvector((term_t) t);
}

JNIEXPORT jint Java_yices_Yices_yices_1term_1bitsize(JNIEnv *env, jclass cls, jint t) {
	return (jint) yices_term_bitsize((term_t) t);
}

JNIEXPORT jlong Java_yices_Yices_yices_1new_1config(JNIEnv *env, jclass cls) {
	return (jlong) yices_new_config();
}

JNIEXPORT void Java_yices_Yices_yices_1free_1config(JNIEnv *env, jclass cls, jlong config) {
	yices_free_config((ctx_config_t *) config);
}

JNIEXPORT jint Java_yices_Yices_yices_1set_1config(JNIEnv *env, jclass cls, jlong config,
		jstring name, jstring value) {

	const char *nameNative = (*env)->GetStringUTFChars(env, name, 0);
	const char *valueNative = (*env)->GetStringUTFChars(env, value, 0);
	const int32_t result = yices_set_config((ctx_config_t *) config, nameNative, valueNative);
	(*env)->ReleaseStringUTFChars(env, name, nameNative);
	(*env)->ReleaseStringUTFChars(env, value, valueNative);
	return (jint) result;
}

JNIEXPORT jint Java_yices_Yices_yices_1default_1config_1for_1logic(JNIEnv *env, jclass cls,
		jlong config, jstring logic) {

	const char *logicNative = (*env)->GetStringUTFChars(env, logic, 0);
	const int32_t result = yices_default_config_for_logic((ctx_config_t *) config, logicNative);
	(*env)->ReleaseStringUTFChars(env, logic, logicNative);
	return (jint) result;
}

JNIEXPORT jlong Java_yices_Yices_yices_1new_1context(JNIEnv *env, jclass cls, jlong config) {
	return (jlong) yices_new_context((ctx_config_t *) config);
}

JNIEXPORT void Java_yices_Yices_yices_1free_1context(JNIEnv *env, jclass cls, jlong ctx) {
	yices_free_context((context_t *) ctx);
}

JNIEXPORT jint Java_yices_Yices_yices_1context_1status(JNIEnv *env, jclass cls, jlong ctx) {
	return (jint) yices_context_status((context_t *) ctx);
}

JNIEXPORT jint Java_yices_Yices_yices_1push(JNIEnv *env, jclass cls, jlong ctx) {
	return (jint) yices_push((context_t *) ctx);
}

JNIEXPORT jint Java_yices_Yices_yices_1pop(JNIEnv *env, jclass cls, jlong ctx) {
	return (jint) yices_pop((context_t *) ctx);
}

JNIEXPORT jint Java_yices_Yices_yices_1context_1enable_1option(JNIEnv *env, jclass cls, jlong ctx,
		jstring option) {

	const char *optionNative = (*env)->GetStringUTFChars(env, option, 0);
	const int32_t result = yices_context_enable_option((context_t *) ctx, optionNative);
	(*env)->ReleaseStringUTFChars(env, option, optionNative);
	return (jint) result;
}

JNIEXPORT jint Java_yices_Yices_yices_1context_1disable_1option(JNIEnv *env, jclass cls, jlong ctx,
		jstring option) {

	const char *optionNative = (*env)->GetStringUTFChars(env, option, 0);
	const int32_t result = yices_context_disable_option((context_t *) ctx, optionNative);
	(*env)->ReleaseStringUTFChars(env, option, optionNative);
	return (jint) result;
}

JNIEXPORT jint Java_yices_Yices_yices_1assert_1formula(JNIEnv *env, jclass cls, jlong ctx, jint t) {
	return (jint) yices_assert_formula((context_t *) ctx, (term_t) t);
}

JNIEXPORT jint Java_yices_Yices_yices_1check_1context(JNIEnv *env, jclass cls, jlong ctx,
		jlong params) {

	return (jint) yices_check_context((context_t *) ctx, (param_t *) params);
}

JNIEXPORT void Java_yices_Yices_yices_1stop_1search(JNIEnv *env, jclass cls, jlong ctx) {
	yices_stop_search((context_t *) ctx);
}

JNIEXPORT jlong Java_yices_Yices_yices_1new_1param_1record(JNIEnv *env, jclass cls) {
	return (jlong) yices_new_param_record();
}

JNIEXPORT void Java_yices_Yices_yices_1default_1params_1for_1context(JNIEnv *env, jclass cls,
		jlong ctx, jlong params) {
	yices_default_params_for_context((context_t *) ctx, (param_t *) params);
}

JNIEXPORT jint Java_yices_Yices_yices_1set_1param(JNIEnv *env, jclass cls, jlong p, jstring pname,
		jstring value) {
	const char *pnameNative = (*env)->GetStringUTFChars(env, pname, 0);
	const char *valueNative = (*env)->GetStringUTFChars(env, value, 0);
	const int32_t result = yices_set_param((param_t *) p, pnameNative, valueNative);
	(*env)->ReleaseStringUTFChars(env, value, valueNative);
	(*env)->ReleaseStringUTFChars(env, pname, pnameNative);
	return (jint) result;
}

JNIEXPORT void Java_yices_Yices_yices_1free_1param_1record(JNIEnv * env, jclass cls, jlong param) {
	yices_free_param_record((param_t *) param);
}

JNIEXPORT jlong Java_yices_Yices_yices_1get_1model(JNIEnv *env, jclass cls, jlong ctx,
		jint keep_subst) {

	return (jlong) yices_get_model((context_t *) ctx, (int32_t) keep_subst);
}

JNIEXPORT void Java_yices_Yices_yices_1free_1model(JNIEnv *env, jclass cls, jlong mdl) {
	yices_free_model((model_t *) mdl);
}

JNIEXPORT jint Java_yices_Yices_yices_1get_1bool_1value(JNIEnv *env, jclass cls, jlong mdl, jint t,
		jintArray val) {

	int32_t *val_ptr = (*env)->GetIntArrayElements(env, val, NULL);
	int32_t result = yices_get_bool_value((model_t *) mdl, (term_t) t, val_ptr);
	(*env)->ReleaseIntArrayElements(env, val, val_ptr, 0);
	return result;
}

JNIEXPORT jint Java_yices_Yices_yices_1get_1bv_1value(JNIEnv *env, jclass cls, jlong mdl, jint t,
		jintArray val) {

	int32_t *val_ptr = (*env)->GetIntArrayElements(env, val, NULL);
	int32_t result = yices_get_bv_value((model_t *) mdl, (term_t) t, val_ptr);
	(*env)->ReleaseIntArrayElements(env, val, val_ptr, 0);
	return result;
}
