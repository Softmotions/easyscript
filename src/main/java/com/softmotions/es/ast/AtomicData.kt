package com.softmotions.es.ast

/**
 * Simple data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AtomicData(val value: TypedValue) : ESNode() {

    override fun toString(): String {
        return "AtomicData(" +
                "value='$value'" +
                ")"
    }
}