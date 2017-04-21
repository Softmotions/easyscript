package com.softmotions.es.ast


enum class ReadAs {
    DEFAULT,
    LINES
}

class AstRead(val data: AstData) : AstNode() {

    override val name: String
        get() = "read expression"
    
    var readAs = ReadAs.DEFAULT
}