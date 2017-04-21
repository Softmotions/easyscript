package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstScript : AstIndentBlock() {

    override val name: String
        get() = "script"
    
    override fun toString(): String {
        return "AstScript($children)"
    }
}