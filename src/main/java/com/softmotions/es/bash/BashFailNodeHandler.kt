package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstFail
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashFailNodeHandler : AstNodeHandler<AstFail, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstFail, out: PrintWriter) {
        out.repeat(ctx.indent)
        val msg = node.msg
        if (msg != null) {
            BashEchoNodeHandler().echo(ctx, listOf(msg), out, inblock = false)
            out.print(' ')
        }
        out.print("exit ")
        out.print(node.exitCode?.value ?: "1")
        out.print(';')
        out.println()
    }
}