package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstRunBlock
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashRunBlockNodeHandler : AstNodeHandler<AstRunBlock, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstRunBlock, out: PrintWriter) {
        out.repeat(ctx.indent)
        val v = ctx.quote(node.value.value, "(", ")")
        out.println(v)

        if (!node.children.isEmpty()) {
            ctx.indent++
            out.repeat(ctx.indent)
            out.println("if [[ $? != '0' ]]; then")
            ctx.indent++
            node.children.forEach({
                ctx.process(it, out)
            })
            ctx.indent--
            out.repeat(ctx.indent)
            out.println("fi")
            ctx.indent--
        }
    }
}