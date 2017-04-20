package com.softmotions.es.ast


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstIf(indent: Int, val bblock: AstBooleanBlock) : AstIndentBlock(indent) {
    override val name: String
        get() = "if statement"
}