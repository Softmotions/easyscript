package com.softmotions.es.ast

/**
 * Array data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstArrayData : AstBlock(), AstData {

    override val name: String
        get() = "array"

    override val values: List<TypedValue>
        get() = children.map {
            (it as AstAtomicData).first
        }.toList()

    override fun toStringOptions(): String {
        return values.toString()
    }
}