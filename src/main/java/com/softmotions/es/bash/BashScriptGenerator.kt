package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandler
import com.softmotions.es.ScriptGenerator
import com.softmotions.es.ast.*
import com.softmotions.es.chain
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

        @JvmStatic
        val log: Logger = LoggerFactory.getLogger(BashScriptGenerator::class.java)

        @Suppress("UNCHECKED_CAST")
        val handlers = mapOf(
                AstEcho::class.to(BashEchoNodeHandler::class as AsmBashNodeHandlerKClass),
                AstSet::class.to(BashSetNodeHandler::class as AsmBashNodeHandlerKClass),
                AstRunBlock::class.to(BashRunBlockNodeHandler::class as AsmBashNodeHandlerKClass),
                AstShell::class.to(BashShellNodeHandler::class as AsmBashNodeHandlerKClass),
                AstFail::class.to(BashFailNodeHandler::class as AsmBashNodeHandlerKClass)
        )

        val SUBST_RE = Regex("([^$])?\\{([^}]+)}")
    }

    override var indent: Int = 0

    override val vars: MutableMap<String, AstNode?> = HashMap()

    val ctx: MutableMap<String, Any> = HashMap()

    override fun error(vararg vals: String) {
        log.error(vals.joinToString(" "))
    }

    override fun warning(vararg vals: String) {
        log.warn(vals.joinToString(" "))
    }

    override fun get(name: String): Any? {
        return ctx[name]
    }

    override fun set(name: String, v: Any?) {
        if (v == null) {
            ctx.remove(name)
        } else {
            ctx[name] = v
        }
    }

    override fun interpolate(v: String): String {
        return v.replace(SUBST_RE, {
            "${it.groups[1]?.value ?: ""}\${${it.groups[2]?.value}}"
        })
    }

    override fun escapeNewLines(v: String): String {
        return v.replace("\r", "").replace("\n", "\\n")
    }

    override fun escapeLinesContinue(v: String): String {
        return v.replace("\r", "").replace("\n", "\\\n")
    }

    override fun quote(v: String, sq: String, eq: String?): String {
        var sv = v.replace(sq, "\\${sq}")
        if (eq != null && sq != eq) {
            sv = sv.replace(eq, "\\${eq}")
        }
        return "${sq}${sv}${eq ?: sq}"
    }

    override fun mqoute(v: String): String {
        return v.chain(this::escapeNewLines, this::interpolate).let { quote(it, "\"") }
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