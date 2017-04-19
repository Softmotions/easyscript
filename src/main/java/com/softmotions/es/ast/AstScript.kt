package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstScript() : AstBlock() {

    var skipIndents: Boolean = false

    override fun toString(): String {
        return "AstScript(skipIndents=$skipIndents, children=$children)"
    }

}