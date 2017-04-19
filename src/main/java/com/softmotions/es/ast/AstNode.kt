package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstNode {

    var parent: AstBlock? = null

    protected val ctx by lazy(LazyThreadSafetyMode.NONE, {
        HashMap<String, Any>()
    })

    fun put(key: String, value: Any) {
        ctx[key] = value
    }

    fun get(key: String): Any? {
        return ctx[key]
    }
}
