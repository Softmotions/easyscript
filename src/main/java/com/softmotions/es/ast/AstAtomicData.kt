package com.softmotions.es.ast

/**
 * Simple data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstAtomicData(_value: TypedValue) : AstNode(), AstData {

    override val name: String
        get() = "data"

    override val value: List<TypedValue> = listOf(_value)

    override fun toStringOptions(): String {
        return first.toString()
    }
}