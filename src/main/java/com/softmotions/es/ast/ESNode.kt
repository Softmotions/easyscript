package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
abstract class ESNode {

    var parent: Block<*>? = null

    protected val ctx by lazy(LazyThreadSafetyMode.NONE, {
        HashMap<String, Any>()
    })

    fun put(key: String, value: Any) {
        ctx.put(key, value);
    }

    fun get(key: String): Any? {
        return ctx.get(key);
    }
}
