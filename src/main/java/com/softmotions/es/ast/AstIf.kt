package com.softmotions.es.ast


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstIf(val bb: AstBooleanBlock) : AstIndentBlock() {

    val elifs: MutableList<AstIf> by lazy(LazyThreadSafetyMode.NONE, {
        ArrayList<AstIf>()
    })

    var el: AstIf? = null

    override val name: String
        get() = "if"

    override fun toString(): String {
        return "AstIf(bb=$bb, ${toStringChildren()}, elifs=$elifs, el=$el"
    }
}