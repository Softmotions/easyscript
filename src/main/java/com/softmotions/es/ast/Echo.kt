package com.softmotions.es.ast

/**
 * Echo operation.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class Echo(val data: AtomicData) : ESNode() {

    override fun toString(): String {
        return "Echo(data=$data)"
    }
}