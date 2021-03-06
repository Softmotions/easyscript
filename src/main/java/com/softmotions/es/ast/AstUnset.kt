package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstUnset : AstNode() {

    @JvmField
    var isEnv: Boolean = false

    @JvmField
    var identifier: String = ""

    override val name: String
        get() = "unset"

    override fun toStringOptions(): String {
        return "${if (isEnv) "env" else ""} ${identifier}"
    }
}