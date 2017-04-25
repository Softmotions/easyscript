package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandlerContext
import com.softmotions.es.ast.AstNode

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface BashNodeHandlerContext : AstNodeHandlerContext {

    val vars: MutableMap<String, AstNode?>

    operator fun get(name: String): Any?

    operator fun set(name: String, v: Any?)

    fun interpolate(v: String): String

    fun escapeNewLines(v: String): String
}