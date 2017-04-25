package com.softmotions.es.bash

import com.softmotions.es.AstNodeHandlerContext

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface BashNodeHandlerContext : AstNodeHandlerContext {

    fun interpolate(v: String): String

    fun ln2escaped(v: String): String
}