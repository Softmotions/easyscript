package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstElse(val astIf: AstIf? = null) : AstIndentBlock() {

    override val name: String
        get() = "else"

    override fun toStringOptions(): String {
        return astIf?.toString() ?: ""
    }
}