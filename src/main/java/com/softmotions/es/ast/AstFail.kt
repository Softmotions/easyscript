package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstFail : AstNode() {

    @JvmField
    var msg: TypedValue? = null

    @JvmField
    var exitCode: TypedValue? = null

    override val name: String
        get() = "fail"

    override fun toStringOptions(): String {
        return "${if (exitCode != null) "exit " + exitCode else ""} ${msg ?: ""}"
    }
}