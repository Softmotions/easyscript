package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ScriptGenerator
import com.softmotions.es.ast.AstEcho
import com.softmotions.es.ast.AstNode
import com.softmotions.es.ast.AstScript
import com.softmotions.es.ast.AstSet
import java.io.PrintWriter
import java.io.Writer
import kotlin.reflect.KClass

typealias AsmBashNodeHandler = AstNodeHandler<AstNode, BashNodeHandlerContext>
typealias AsmBashNodeHandlerKClass = KClass<AsmBashNodeHandler>

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class BashScriptGenerator : ScriptGenerator, BashNodeHandlerContext {

    companion object {

        @Suppress("UNCHECKED_CAST")
        val handlers = mapOf(
                AstEcho::class.to(BashEchoNodeHandler::class as AsmBashNodeHandlerKClass),
                AstSet::class.to(BashSetNodeHandler::class as AsmBashNodeHandlerKClass)
        )

        val SUBST_RE = Regex("([^$])?\\{([^}]+)}")
    }

    override fun interpolate(v: String): String {
        return v.replace(SUBST_RE, {
            "${it.groups[1]?.value ?: ""}\${${it.groups[2]?.value}}"
        })
    }

    override fun ln2escaped(v: String): String {
        return v.replace("\n", "\\n")
    }

    override fun generate(ast: AstScript, out: Writer) {
        val pw = PrintWriter(out)
        pw.println("#!/bin/bash")
        pw.println()
        ast.forEach({
            process(it, pw)
        })
    }

    override fun process(node: AstNode, out: PrintWriter) {
        val hc = (handlers[node::class]?.java
                ?: throw IllegalStateException("Missing node handler for: ${node::class.qualifiedName}"))
        hc.newInstance().handle(this, node, out)
    }
}