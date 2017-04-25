package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstEcho
import com.softmotions.es.ast.ValueType
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashEchoNodeHandler : AstNodeHandler<AstEcho, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstEcho, out: PrintWriter) {
        out.print("echo ")
        val (type, value) = node.data.first
        when (type) {
            ValueType.IDENTIFIER, ValueType.NUMBER -> {
                out.print(value)
            }
            ValueType.SQUOTED -> {
                out.print("'${value}'")
            }
            ValueType.DQUOTED -> {
                out.print("\"${ctx.interpolate(value)}\"")
            }
            ValueType.MQUOTED -> {
                out.print("-e \"${ctx.interpolate(ctx.escapeNewLines(value))}\"")
            }
            ValueType.RUN -> {
                out.print("`${ctx.interpolate(value)}`")
            }
        }
        out.println()
    }
}