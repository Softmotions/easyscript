package com.softmotions.es.ast


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstIf(val bb: AstBooleanBlock) : AstIndentBlock(), AstNestedBodyAware {

    val els: MutableList<AstElse> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstElse>()
    })

    override val name: String
        get() = "if"

    override fun toStringOptions(): String {
        return bb.toString()
    }
}