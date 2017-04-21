package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstBlock : AstNode() {


    val children: MutableList<AstNode> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstNode>()
    })


    fun lastChild(): AstNode {
        return children.last()
    }


    fun addChild(node: AstNode): Boolean {
        log.info("ADD CHILD: $node INTO ${javaClass.simpleName}")
        node.parent?.removeChild(node)
        node.parent = this;
        children.add(node)
        return true
    }

    fun removeChild(node: AstNode): Boolean {
        log.info("REMOVE CHILD: $node FROM $this")
        children.remove(node)
        return true
    }

}