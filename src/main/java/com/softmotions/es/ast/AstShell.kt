package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstShell(val data: AstData) : AstIndentBlock() {

    override val name: String
        get() = "shell"


    override fun toStringOptions(): String {
        return data.toString()
    }
}