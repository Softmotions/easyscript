package com.softmotions.es

import java.io.Writer

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */

fun Writer.repeat(num: Int, ch: Char = ' ') {
    this.write(ch.toString().repeat(num))
}

fun Writer.print(num: Int, v: String, ch: Char = ' ') {
    this.write(ch.toString().repeat(num))
    this.write(v)
}