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
#ifndef YICESJNI_H
#define YICESJNI_H

#include <jni.h>

JNIEXPORT void     Java_yices_Yices_yices_1init(JNIEnv *, jclass);
JNIEXPORT void     Java_yices_Yices_yices_1exit(JNIEnv *, jclass);
JNIEXPORT void     Java_yices_Yices_yices_1reset(JNIEnv *, jclass);
JNIEXPORT jint     Java_yices_Yices_yices_1error_1code(JNIEnv *, jclass);
JNIEXPORT jstring  Java_yices_Yices_yices_1error_1string(JNIEnv *, jclass);
JNIEXPORT jint     Java_yices_Yices_yices_1bool_1type(JNIEnv *, jclass);
JNIEXPORT jint     Java_yices_Yices_yices_1bv_1type(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1true(JNIEnv *, jclass);
JNIEXPORT jint     Java_yices_Yices_yices_1false(JNIEnv *, jclass);
JNIEXPORT jint     Java_yices_Yices_yices_1ite(JNIEnv *, jclass, jint, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1eq(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1neq(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1not(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1or2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1and2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1xor2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1iff(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1implies(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1distinct(JNIEnv *, jclass, jintArray);
JNIEXPORT jint     Java_yices_Yices_yices_1bvconst_1int32(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvconst_1int64(JNIEnv *, jclass, jint, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1parse_1bvbin(JNIEnv *, jclass, jstring);
JNIEXPORT jint     Java_yices_Yices_yices_1new_1uninterpreted_1term(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvadd(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsub(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvneg(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvmul(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvdiv(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvrem(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsdiv(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsrem(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsmod(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvnot(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvshl(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvlshr(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvashr(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvand2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvor2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvxor2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvextract(JNIEnv *, jclass, jint, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvconcat2(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1sign_1extend(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1zero_1extend(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bveq_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvneq_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvge_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvgt_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvle_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvlt_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsge_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsgt_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvsle_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1bvslt_1atom(JNIEnv *, jclass, jint, jint);
JNIEXPORT jboolean Java_yices_Yices_yices_1term_1is_1bool(JNIEnv *, jclass, jint);
JNIEXPORT jboolean Java_yices_Yices_yices_1term_1is_1bitvector(JNIEnv *, jclass, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1term_1bitsize(JNIEnv *, jclass, jint);
JNIEXPORT jlong    Java_yices_Yices_yices_1new_1config(JNIEnv *, jclass);
JNIEXPORT void     Java_yices_Yices_yices_1free_1config(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1set_1config(JNIEnv *, jclass, jlong, jstring, jstring);
JNIEXPORT jint     Java_yices_Yices_yices_1default_1config_1for_1logic(JNIEnv *, jclass, jlong, jstring);
JNIEXPORT jlong    Java_yices_Yices_yices_1new_1context(JNIEnv *, jclass, jlong);
JNIEXPORT void     Java_yices_Yices_yices_1free_1context(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1context_1status(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1push(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1pop(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1context_1enable_1option(JNIEnv *, jclass, jlong, jstring);
JNIEXPORT jint     Java_yices_Yices_yices_1context_1disable_1option(JNIEnv *, jclass, jlong, jstring);
JNIEXPORT jint     Java_yices_Yices_yices_1assert_1formula(JNIEnv *, jclass, jlong, jint);
JNIEXPORT jint     Java_yices_Yices_yices_1check_1context(JNIEnv *, jclass, jlong, jlong);
JNIEXPORT void     Java_yices_Yices_yices_1stop_1search(JNIEnv *, jclass, jlong);
JNIEXPORT jlong    Java_yices_Yices_yices_1new_1param_1record(JNIEnv *, jclass);
JNIEXPORT void     Java_yices_Yices_yices_1default_1params_1for_1context(JNIEnv *, jclass, jlong, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1set_1param(JNIEnv *, jclass, jlong, jstring, jstring);
JNIEXPORT void     Java_yices_Yices_yices_1free_1param_1record(JNIEnv *, jclass, jlong);
JNIEXPORT jlong    Java_yices_Yices_yices_1get_1model(JNIEnv *, jclass, jlong, jint);
JNIEXPORT void     Java_yices_Yices_yices_1free_1model(JNIEnv *, jclass, jlong);
JNIEXPORT jint     Java_yices_Yices_yices_1get_1bool_1value(JNIEnv *, jclass, jlong, jint, jintArray);
JNIEXPORT jint     Java_yices_Yices_yices_1get_1bv_1value(JNIEnv *, jclass, jlong, jint, jintArray);

#endif

