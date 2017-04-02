package com.softmotions.es

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.Label
import org.parboiled.annotations.MemoMismatches

/**
 * Easyscript parser.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@JvmSuppressWildcards
open class ESParser : BaseParser<Any>() {

    open fun Sript(): Rule {
        return EMPTY;
    }

    @MemoMismatches
    open fun LetterOrDigit(): Rule {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_');
    }

    @Label("Digit")
    open fun Digit(): Rule = CharRange('0', '9')

    @Label("Whitespace")
    open fun Spacing(): Rule = ZeroOrMore(AnyOf(" \t\r\n"))

}