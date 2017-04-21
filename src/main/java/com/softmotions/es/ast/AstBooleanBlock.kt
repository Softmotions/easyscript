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
    override val name: String
        get() = "boolean expression"
    var join = BooleanBlockJoin.NONE
    var negate = false
}

class AstFileBooleanNode(val type: AstFileType) : AstBooleanBlock() {     
    var predicate: AstFilePredicate = AstFilePredicate.EXISTS
    var data: AstData = AstEmptyData()
    override fun toString(): String {
        return "AstFileBooleanNode(type=$type, predicate=$predicate, data=$data, ${toStringChildren()}"
    }
}

class AstInBooleanNode : AstBooleanBlock() {

}

class AstCompareBooleanBlock : AstBooleanBlock() {
   var op = AstCompareOp.EQ
}



