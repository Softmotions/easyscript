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

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class AstBooleanBlock : AstBlock() {
    var join = BooleanBlockJoin.NONE
    var negate = false
}

class AstFileBooleanBlock() : AstBooleanBlock() {
    var type: AstFileType = AstFileType.FILE
    var predicate: AstFilePredicate = AstFilePredicate.EXISTS
    var data: Data = Data.EMPTY
}

class AstCompareBooleanBlock : AstBooleanBlock() {

}

class AstInBooleanBlock : AstBooleanBlock() {

}

