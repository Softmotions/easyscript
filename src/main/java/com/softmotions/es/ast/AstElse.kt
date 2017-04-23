package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstElse(val astIf: AstIf? = null) : AstIndentBlock() {

    override val name: String
        get() = "else/else if"

    override fun toString(): String {
        return "AstElse(if=$astIf, ${toStringChildren()}"
    }
}