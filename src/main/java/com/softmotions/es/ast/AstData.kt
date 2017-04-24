package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface AstData {

    val first: TypedValue
        get() = value.first()

    val value: List<TypedValue>
}