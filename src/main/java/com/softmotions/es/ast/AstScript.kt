package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstScript(val verbose: Boolean = false) : AstIndentBlock() {

    override val name: String
        get() = "script"
}