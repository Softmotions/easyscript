package com.softmotions.es.ast


enum class ReadAs {
    DEFAULT,
    LINES,
    WORDS,
    CHARS
}

class AstRead(val data: AstData) : AstNode() {

    override val name: String
        get() = "read"

    @JvmField
    var readAs = ReadAs.DEFAULT

    override fun toStringOptions(): String {
        return "${data} ${readAs}"
    }
}