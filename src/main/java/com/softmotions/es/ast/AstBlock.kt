package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstBlock : AstNode() {

    val children = ArrayList<AstNode>()

    fun addChildren(node: AstNode) {
        node.parent?.removeChildren(node)
        node.parent = this;
        children += node
    }

    fun removeChildren(node: AstNode) {
        children.remove(node)
    }

}