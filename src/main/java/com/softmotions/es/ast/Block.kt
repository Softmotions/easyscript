package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class Block<T : ESNode> : ESNode() {

    val children = ArrayList<T>()

    fun addChildren(node: T) {
        node.parent = this;
        children += node
    }

    fun removeChildren(node: T) {
        children -= node
    }

}