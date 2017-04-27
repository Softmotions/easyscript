package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandlerContext
import com.softmotions.es.ast.AstNode

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface BashNodeHandlerContext : AstNodeHandlerContext {

    var indent: Int

    val vars: MutableMap<String, AstNode?>

    operator fun get(name: String): Any?

    operator fun set(name: String, v: Any?)

    fun interpolate(v: String): String

    fun escape(v: String): String

    fun quote(v: String, qc: String = "\'"): String

    fun mqoute(v: String): String

    fun dqoute(v: String): String
}