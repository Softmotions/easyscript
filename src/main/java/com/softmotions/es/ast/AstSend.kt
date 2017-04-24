package com.softmotions.es.ast

enum class SendOp {
    REPLACE,
    APPEND;

    fun toOpString(): String {
        return when (this) {
            SendOp.REPLACE -> ">"
            SendOp.APPEND -> ">>"
        }
    }
}

class AstSend : AstIndentBlock() {

    @JvmField
    var sendOp: SendOp = SendOp.REPLACE

    @JvmField
    var srcShell: AstShell? = null

    @JvmField
    var srcData: AstData? = null

    @JvmField
    var target: AstData? = null

    override val name: String
        get() = "send"

    override fun toStringOptions(): String {
        fun source2string(): String {
            return if (srcData != null) srcData.toString()
            else if (srcShell != null) srcShell.toString()
            else ""
        }
        return "${source2string()} ${sendOp.toOpString()} ${target}"
    }
}