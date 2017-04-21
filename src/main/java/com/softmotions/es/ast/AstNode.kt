package com.softmotions.es.ast

import org.slf4j.LoggerFactory

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class AstNode {

    val log = LoggerFactory.getLogger(javaClass)

    var parent: AstBlock? = null

    abstract val name: String

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
