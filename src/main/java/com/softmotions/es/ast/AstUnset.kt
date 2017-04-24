package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstUnset : AstNode() {

    var isEnv: Boolean = false

    var identifier: String = ""

    override val name: String
        get() = "unset"

    override fun toStringOptions(): String {
        return "${if (isEnv) "env" else ""} ${identifier}"
    }
}