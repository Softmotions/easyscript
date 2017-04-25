package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstEach : AstIndentBlock(), AstNestedBodyAware {

    var identifier: String = ""

    var data: AstData = AstEmptyData()

    var readAs: ReadAs = ReadAs.DEFAULT

    override val name: String
        get() = "each"

    override fun toStringOptions(): String {
        return "${identifier} in ${data} as ${readAs}"
    }
}