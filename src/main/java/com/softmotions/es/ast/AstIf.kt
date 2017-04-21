package com.softmotions.es.ast


/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstIf(val bblock: AstBooleanBlock) : AstIndentBlock() {
    override val name: String
        get() = "if statement"

    override fun toString(): String {
        return "AstIf(bblock=$bblock, ifchildren=$children)"
    }


}