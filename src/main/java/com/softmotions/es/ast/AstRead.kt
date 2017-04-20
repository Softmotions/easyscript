package com.softmotions.es.ast


enum class ReadAs {
    DEFAULT,
    LINES
}

class AstRead : AstNode() {

    override val name: String
        get() = "read expression"
    
    var readAs = ReadAs.DEFAULT

    var data: AstData = AstEmptyData()
}