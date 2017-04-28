package com.softmotions.es.bash

import com.softmotions.es.ast.AstNode
import com.softmotions.es.ast.TypedValue
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class BashExecNodeHandlerSupport {

    protected fun handle(ctx: BashNodeHandlerContext,
                         tv: TypedValue,
                         children: List<AstNode>,
                         out: PrintWriter) {
        out.repeat(ctx.indent)
        val v = ctx.quote(tv.value, "(", ")")
        out.println(v)

        if (!children.isEmpty()) {
            ctx.indent++
            out.repeat(ctx.indent)
            out.println("if [[ $? != '0' ]]; then")
            ctx.indent++
            children.forEach({
                ctx.process(it, out)
            })
            ctx.indent--
            out.repeat(ctx.indent)
            out.println("fi")
            ctx.indent--
        }
    }
}