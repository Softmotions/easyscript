package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface AstData {

    val first: TypedValue
        get() = values.first()

    val values: List<TypedValue>
}