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
        get() = "bool expr"
    var join = BooleanBlockJoin.NONE
    var negate = false
    override fun toStringOptions(): String {
        return "${if (join == BooleanBlockJoin.NONE) "" else join.toString()}${if (negate) " NOT" else ""}"
    }
}

class AstFileBooleanNode(val type: AstFileType) : AstBooleanBlock() {
    var predicate: AstFilePredicate = AstFilePredicate.EXISTS
    var data: AstData = AstEmptyData()
    override fun toStringOptions(): String {
        return "${super.toStringOptions()} ${type} ${predicate} ${data}"
    }
}

class AstInBooleanNode : AstBooleanBlock() {

}

class AstCompareBooleanBlock : AstBooleanBlock() {
    var op = AstCompareOp.EQ
    override fun toStringOptions(): String {
        return "${super.toStringOptions()}, op=${op}"
    }
}
