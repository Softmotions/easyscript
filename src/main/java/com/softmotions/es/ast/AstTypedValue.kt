package com.softmotions.es.ast

val TQ = "\"\"\""

enum class ValueType {
    IDENTIFIER,
    NUMBER,
    SQUOTED,
    DQUOTED,
    MQUOTED,
    RUN
}

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
data class TypedValue(val type: ValueType, val value: String) : AstNode() {

    override val name: String
        get() = "typed value"

    companion object {

        fun identifier(value: String): TypedValue = TypedValue(ValueType.IDENTIFIER, value.trim())
        fun number(value: String): TypedValue = TypedValue(ValueType.NUMBER, value.trim())
        fun squoted(value: String): TypedValue = TypedValue(ValueType.SQUOTED, value.trim())
        fun dquoted(value: String): TypedValue = TypedValue(ValueType.DQUOTED, value.trim())
        fun mquoted(value: String): TypedValue = TypedValue(ValueType.MQUOTED, value.trim())
        fun run(value: String): TypedValue = TypedValue(ValueType.RUN, value.trim())
    }

    fun asQuoted(): String {
        when (type) {
            ValueType.IDENTIFIER, ValueType.NUMBER -> {
                return value
            }
            ValueType.SQUOTED -> {
                return "'${value}'"
            }
            ValueType.DQUOTED -> {
                return "\"${value}\""
            }
            ValueType.MQUOTED -> {
                return "${TQ}${value}${TQ}"
            }
            ValueType.RUN -> {
                return "`${value}`"
            }
        }
    }

    override fun toString(): String {
        return "$type($value)"
    }

}