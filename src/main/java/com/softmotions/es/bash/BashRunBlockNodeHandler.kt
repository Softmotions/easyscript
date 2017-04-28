package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ast.AstRunBlock
import java.io.PrintWriter

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashRunBlockNodeHandler :
        BashExecNodeHandlerSupport(),
        AstNodeHandler<AstRunBlock, BashNodeHandlerContext> {

    override fun handle(ctx: BashNodeHandlerContext, node: AstRunBlock, out: PrintWriter) {
        super.handle(ctx, node.value, node.children, out)
    }
}