package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstShell
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashShellNodeHandler :
        BashExecNodeHandlerSupport(),
        AstNodeHandler<AstShell, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstShell, out: PrintWriter) {
        super.handle(ctx, node.value.first, node.children, out)
    }
}