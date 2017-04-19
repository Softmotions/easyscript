package com.softmotions.es.ast


enum class BooleanBlockJoin {
    NONE,
    AND,
    OR
}

enum class AstFileType {
    FILE,
    DIR,
    LINK
}

enum class AstFilePredicate {
    EXISTS,
    EXEC,
    READABLE,
    WRITABLE
}

enum class AstCompareOp {
    EQ,
    LTE,
    GTE,
    GT,
    LT
}

open class AstBooleanBlock : AstBlock() {
    var join = BooleanBlockJoin.NONE
    var negate = false
}

class AstFileBooleanNode : AstBooleanBlock() {
    var type: AstFileType = AstFileType.FILE
    var predicate: AstFilePredicate = AstFilePredicate.EXISTS
    var data: AstData = AstEmptyData()
}

class AstInBooleanNode : AstBooleanBlock() {

}

class AstCompareBooleanBlock : AstBooleanBlock() {
   var op = AstCompareOp.EQ
}



