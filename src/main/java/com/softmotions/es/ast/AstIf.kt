package com.softmotions.es.ast


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstIf(val bb: AstBooleanBlock) : AstIndentBlock() {

    val els: MutableList<AstElse> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstElse>()
    })

    override val name: String
        get() = "if"

    override fun toString(): String {
        return "AstIf(bb=$bb, ${toStringChildren()}, els=$els"
    }
}