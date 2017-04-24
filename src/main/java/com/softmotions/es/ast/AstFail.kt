package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstFail : AstNode() {

    var msg: TypedValue? = null

    var exitCode: TypedValue? = null

    override val name: String
        get() = "fail"

    override fun toStringOptions(): String {
        return "${if (exitCode != null) "exit " + exitCode else ""} ${msg ?: ""}"
    }
}