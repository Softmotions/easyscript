package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstSet : AstIndentBlock() {

    var isEnv: Boolean = false

    var identifier: String = ""

    var readAs: ReadAs? = null

    var data: AstData? = null

    override val name: String
        get() = "set"

    override fun toStringOptions(): String {
        return "${if (isEnv) "env" else ""} ${identifier} ${data} ${readAs ?: ""}"
    }
}