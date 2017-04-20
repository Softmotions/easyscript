package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstScript() : AstBlock() {

    override val name: String
        get() = "script"
    var indent = 0
    var skipIndents: Boolean = false

    fun indent() = ++indent
    fun dedent() = if (indent > 0) --indent else null

    override fun toString(): String {
        return "AstScript(skipIndents=$skipIndents, children=$children)"
    }

}