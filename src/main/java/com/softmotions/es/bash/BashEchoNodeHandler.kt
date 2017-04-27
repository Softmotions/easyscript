package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstEcho
import com.softmotions.es.ast.ValueType
import com.softmotions.es.chain
import com.softmotions.es.print
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashEchoNodeHandler : AstNodeHandler<AstEcho, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstEcho, out: PrintWriter) {

        // todo echo already set var!
        
        out.repeat(ctx.indent)
        val values = node.data.values
        if (values.size > 1) {
            out.print("{ ")
        }
        values.forEachIndexed({ idx, (type, value) ->
            val sep = if (idx > 0) " " else ""
            val dsep = if (idx > 0) "\" \"" else ""
            out.print(sep)
            out.print(ctx.indent, "echo ")
            if (idx < values.size - 1) {
                out.print("-n ")
            }
            when (type) {
                ValueType.NUMBER -> {
                    out.print("\"${sep}$value\"")
                }
                ValueType.IDENTIFIER -> {
                    out.print("\"${sep}\${$value}\"")
                }
                ValueType.SQUOTED -> {
                    out.print("\$'${sep}${value}'")
                }
                ValueType.DQUOTED -> {
                    out.print("${dsep}${ctx.dqoute(value)}")
                }
                ValueType.MQUOTED -> {
                    out.print("-e ${dsep}${ctx.mqoute(value)}")
                }
                ValueType.RUN -> {
                    out.print("`${sep}${value.chain(ctx::interpolate)}`\"")
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