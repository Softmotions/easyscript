package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstEmptyData : AstNode(), AstData {

    override val name: String
        get() = "empty data"

    override val values: List<TypedValue>
        get() = emptyList()
}