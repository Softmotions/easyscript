package com.softmotions.es.ast

/**
 * AstEcho operation.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstEcho(val data: AstData) : AstNode() {

    override val name: String
        get() = "echo"

    override fun toStringOptions(): String {
        return data.toString()
    }
}