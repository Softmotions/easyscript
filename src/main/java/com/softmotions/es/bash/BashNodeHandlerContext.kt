package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandlerContext
import com.softmotions.es.ast.AstNode

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface BashNodeHandlerContext : AstNodeHandlerContext {

    fun interpolate(v: String): String

    fun ln2escaped(v: String): String

    operator fun get(name: String): AstNode?

    operator fun set(name: String, node: AstNode?)
}