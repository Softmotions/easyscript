package com.softmotions.es

import com.softmotions.es.ast.AstNode
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface AstNodeHandlerContext {

    fun process(node: AstNode, out: PrintWriter)

    fun error(vararg vals: String)

    fun warning(vararg vals: String)
}