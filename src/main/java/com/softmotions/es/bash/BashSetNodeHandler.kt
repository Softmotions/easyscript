package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.*
import com.softmotions.es.repeat
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashSetNodeHandler : AstNodeHandler<AstSet, BashNodeHandlerContext> {
    
    override fun handle(ctx: BashNodeHandlerContext, node: AstSet, out: PrintWriter) {
        val data = node.data as AstData
        ctx.vars[node.identifier] = node.data as AstNode
        
        fun printValue(v: TypedValue) {
            when (v.type) {
                ValueType.IDENTIFIER -> {
                    out.print("\${${v.value}}")
                }
                ValueType.DQUOTED -> {
                    out.print(ctx.dqoute(v.value))
                }
                ValueType.SQUOTED -> {
                    out.print("$'${v.value}'");
                }
                ValueType.NUMBER,
                ValueType.RUN -> {
                    out.print(v.asQuoted())
                }
                ValueType.MQUOTED -> {
                    out.print(ctx.mqoute(v.value))
                }
            }
        }
        out.repeat(ctx.indent)
        if (node.isEnv) {
            out.print("export ")
        }
        out.print(node.identifier)
        out.print('=')
        if (data.values.size > 1) {
            out.print('(')
            data.values.forEachIndexed({ idx, tv ->
                if (idx > 0) {
                    out.print(' ')
                }
                printValue(tv)
            })
            out.print(')')
        } else {
            printValue(data.first)
        }
        out.println()
    }
}