package com.softmotions.es.ast

/**
 * Simple data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AtomicData(val value: TypedValue,
                 parent: Block<*>? = null) : ESNode(parent) {

    override fun toString(): String {
        return "AtomicData(" +
                "value='$value'" +
                ")"
    }
}