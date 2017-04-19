package com.softmotions.es.ast


enum class ReadAs {
    DEFAULT,
    LINES
}

class AstRead : AstNode() {

    var readAs = ReadAs.DEFAULT

    var data: AstData = AstEmptyData()
}