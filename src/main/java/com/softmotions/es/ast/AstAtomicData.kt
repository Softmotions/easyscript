package com.softmotions.es.ast

/**
 * Simple data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstAtomicData(val value: TypedValue) : AstNode(), AstData {

    override val name: String
        get() = "data"
    
    override fun toStringOptions(): String {
        return value.toString()
    }
}