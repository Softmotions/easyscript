package com.softmotions.es.ast

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface Data {

    companion object {
        val EMPTY: Data = object : Data {
        }
    }
}