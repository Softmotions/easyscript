package com.softmotions.es.ast

/**
 * Array data.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class AstArrayData : AstBlock(), AstData {

    override val name: String
        get() = "array"
}