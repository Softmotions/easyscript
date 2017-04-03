package com.softmotions.es

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.DontLabel
import org.parboiled.annotations.MemoMismatches
import org.parboiled.annotations.SuppressNode
import org.parboiled.annotations.SuppressSubnodes

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

    /**
     * ```
     *   varname
     *   110
     *   "test"
     *   'test'
     *   `run`
     * ```
     */
    open fun DataProducer(): Rule {
        return EMPTY;
    }


    @MemoMismatches
    open fun Keyword(): Rule {
        return FirstOf("if", "else", "set", "env", "each")
    }

    @SuppressSubnodes
    @MemoMismatches
    open fun Identifier(): Rule {
        return Sequence(TestNot(Keyword()), Letter(), ZeroOrMore(LetterOrDigit()), Spacing())
    }

    @MemoMismatches
    open fun LetterOrDigit(): Rule {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_')
    };

    open fun Letter(): Rule = FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');

    open fun Digit(): Rule = CharRange('0', '9')

    open fun Spacing(): Rule {
        return ZeroOrMore(FirstOf(
                // whitespace
                OneOrMore(AnyOf(" \t\r\n").label("Whitespace")),
                Sequence(
                        "#",
                        ZeroOrMore(TestNot(AnyOf("\r\n")), BaseParser.ANY),
                        FirstOf("\r\n", '\r', '\n', BaseParser.EOI)
                )
        ))
    }

    ///////////////////////////////////////////////////////////////////////////
    //                            Helpers                                    //
    ///////////////////////////////////////////////////////////////////////////


    override fun fromCharLiteral(c: Char): Rule {
        // turn of creation of parse tree nodes for single characters
        return super.fromCharLiteral(c).suppressNode()
    }

    @SuppressNode
    @DontLabel
    internal fun Terminal(string: String): Rule {
        return Sequence(string, Spacing()).label("'$string'")
    }

    @SuppressNode
    @DontLabel
    internal fun Terminal(string: String, mustNotFollow: Rule): Rule {
        return Sequence(string, TestNot(mustNotFollow), Spacing()).label("'$string'")
    }

}