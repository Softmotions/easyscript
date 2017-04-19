package com.softmotions.es.ast

/**
 * AstEcho operation.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstEcho(val data: AstData) : AstNode() {
    
    override fun toString(): String {
        return "AstEcho(data=$data)"
    }
}