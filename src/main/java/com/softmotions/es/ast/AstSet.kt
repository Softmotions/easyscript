package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstSet : AstIndentBlock() {

    @JvmField
    var isEnv: Boolean = false

    @JvmField
    var identifier: String = ""

    @JvmField
    var readAs: ReadAs? = null

    @JvmField
    var data: AstData? = null

    override val name: String
        get() = "set"

    override fun toStringOptions(): String {
        return "${if (isEnv) "env" else ""} ${identifier} ${data} ${readAs ?: ""}"
    }
}