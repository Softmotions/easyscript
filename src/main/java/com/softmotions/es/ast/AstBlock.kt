package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstBlock : AstNode() {

    val children: MutableList<AstNode> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstNode>()
    })

    fun addChildren(node: AstNode): Boolean {
        node.parent?.removeChildren(node)
        node.parent = this;
        children += node
        return true
    }

    fun removeChildren(node: AstNode): Boolean {
        children.remove(node)
        return true
    }

}