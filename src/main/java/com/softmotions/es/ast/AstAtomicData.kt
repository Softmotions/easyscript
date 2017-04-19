package com.softmotions.es.ast

/**
 * Simple data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstAtomicData(val value: TypedValue) : AstNode(), Data {

    override fun toString(): String {
        return "AstAtomicData(" +
                "value='$value'" +
                ")"
    }
}