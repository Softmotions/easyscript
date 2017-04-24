package com.softmotions.es.ast

import org.apache.commons.lang3.SystemUtils
import org.parboiled.errors.ActionException

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstBlock : AstNode() {

    val TOS_PADDING_INDENT = 4

    val children: MutableList<AstNode> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstNode>()
    })

    fun lastChild(): AstNode {
        return children.last()
    }

    fun addChild(node: AstNode): Boolean {
        log.info("ADD CHILD: $node INTO ${javaClass.simpleName}")
        if (node is AstElse) {
            children.findLast { it is AstIf }?.let {
                it as AstIf
                it.els += node
            } ?: throw ActionException("Missing 'if' block before 'else'")
        }
        node.parent?.removeChild(node)
        node.parent = this
        children.add(node)
        return true
    }

    fun removeChild(node: AstNode): Boolean {
        log.info("REMOVE CHILD: $node FROM $this")
        children.remove(node)
        return true
    }

    open fun toStringChildren(): List<AstNode> {
        return children
    }

    override fun toString(padding: Int): String {
        val sb = StringBuilder()
        sb.append(super.toString(padding))
        toStringChildren().forEach({
            sb.append(SystemUtils.LINE_SEPARATOR)
                    .append(it.toString(padding + TOS_PADDING_INDENT))
        })
        return sb.toString()
    }
}