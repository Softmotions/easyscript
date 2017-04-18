package com.softmotions.es.ast

/**
 * Echo operation.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class Echo(val data: AtomicData,
           parent: Block<*>? = null) : ESNode(parent) {

    override fun toString(): String {
        return "Echo(data=$data)"
    }
}