package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstRunBlock(val value: TypedValue) : AstIndentBlock() {

    override val name: String
        get() = "run block"

    override fun toStringOptions(): String {
        return value.toString()
    }
}