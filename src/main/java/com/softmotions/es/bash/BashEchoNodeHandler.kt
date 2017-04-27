package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstEcho
import com.softmotions.es.ast.ValueType
import com.softmotions.es.print
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashEchoNodeHandler : AstNodeHandler<AstEcho, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstEcho, out: PrintWriter) {
        out.repeat(ctx.indent)
        val values = node.data.values
        if (values.size > 1) {
            out.print("{ ")
        }
        values.forEachIndexed({ idx, (type, value) ->
            val sep = if (idx > 0) " " else ""
            out.print(sep)
            out.print(ctx.indent, "echo ")
            if (idx < values.size - 1) {
                out.print("-n ")
            }
            when (type) {
                ValueType.IDENTIFIER -> {
                    out.print("\"${sep}\${$value}\"")
                }
                ValueType.SQUOTED, ValueType.NUMBER -> {
                    out.print("'${sep}${value}'")
                }
                ValueType.DQUOTED -> {
                    out.print("\"${sep}${ctx.interpolate(value)}\"")
                }
                ValueType.MQUOTED -> {
                    out.print("-e \"${sep}${ctx.interpolate(ctx.escapeNewLines(value))}\"")
                }
                ValueType.RUN -> {
                    out.print("${sep}\"`${ctx.interpolate(value)}`\"")
                }
            }
            out.print(';')
        })
        if (values.size > 1) {
            out.print(" }");
        }
        out.println()
    }
}