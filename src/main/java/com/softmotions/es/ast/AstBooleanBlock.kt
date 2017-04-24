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
    LT,
    IN
}

open class AstBooleanBlock : AstNode() {
    override val name: String
        get() = "bool expr"

    @JvmField
    var join = BooleanBlockJoin.NONE

    @JvmField
    var negate = false

    var next: AstBooleanBlock? = null

    override fun toString(padding: Int): String {
        return "${" ".repeat(padding)}${toStringOptions()}"
    }

    override fun toStringOptions(): String {
        return "${if (join == BooleanBlockJoin.NONE) "" else join.toString()} ${if (negate) " NOT" else ""}"
    }
}

class AstFileBooleanNode(val type: AstFileType) : AstBooleanBlock() {

    @JvmField
    var predicate: AstFilePredicate = AstFilePredicate.EXISTS

    @JvmField
    var data: AstData = AstEmptyData()

    override fun toStringOptions(): String {
        return "${super.toStringOptions()} ${type} ${predicate} ${data} ${next?.toString() ?: ""}"
    }
}


class AstCompareBooleanBlock : AstBooleanBlock() {

    @JvmField
    var left: AstNode? = null

    @JvmField
    var right: AstNode? = null

    @JvmField
    var op = AstCompareOp.EQ

    override fun toStringOptions(): String {
        return "${super.toStringOptions()} ${left} ${op} ${right} ${next?.toString() ?: ""}"
    }
}
