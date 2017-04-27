package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstNode
import com.softmotions.es.ast.AstSet
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashSetNodeHandler : AstNodeHandler<AstSet, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstSet, out: PrintWriter) {

        if (node.isEnv) {
            out.print("export ")
        }
        ctx.vars[node.identifier] = node.data as AstNode



        print(node.identifier)
        val data = node.data!!
        if (data.values.size == 1) { // Single value
                        

        } else {  // Array

        }

        // todo
        
        println()
    }
}