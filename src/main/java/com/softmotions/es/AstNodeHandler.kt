package com.softmotions.es

import com.softmotions.es.ast.AstNode
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface AstNodeHandler<in T : AstNode, in C : AstNodeHandlerContext> {

    fun handle(ctx: C, node: T, out: PrintWriter)
}