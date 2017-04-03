package com.softmotions.es

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.MemoMismatches
import org.parboiled.annotations.SuppressSubnodes

/**
 * Easyscript parser.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@JvmSuppressWildcards
open class ESParser : BaseParser<Any>() {

    companion object {
        val LF_CHARS = "\r\n"
        val SPACE_CHARS = " \t"
        val SPACE_LF_CHARS = "${SPACE_CHARS}${LF_CHARS}"
    }

    open fun Sript(): Rule {
        return Sequence(ZeroOrMore(Block(), Spacing()), BaseParser.EOI)
    }

    open fun Block(): Rule {
        return Sequence(
                Spacing(),
                OneOrMore(
                        FirstOf(
                                Set(),
                                Unset()
                        )
                ),
                Optional(Sequence(Spacing(), ChildBlock()))
        )
    }

    open fun ChildBlock(): Rule {
        return Sequence(
                INDENT,
                OneOrMore(Block(), Spacing()),
                DEDENT)
    }

    open fun Set(): Rule {
        return Sequence(
                Action("set"),
                Optional(Sequence("env", Blank())).label("Env"),
                Identifier(),
                Blank(),
                Data())
    }

    open fun Unset(): Rule {
        return Sequence(
                Action("unset"),
                Optional(Sequence("env", Blank())).label("Env"),
                Identifier())
    }

    open fun Action(name: String): Rule {
        return Sequence(LineStart(), name, Blank());
    }

    @MemoMismatches
    open fun Action(): Rule {
        return Sequence(
                LineStart(),
                FirstOf(
                        "if", "else",
                        "set", "unset",
                        "echo", "fail",
                        "send", "read", "copy", "call", "shell",
                        "find", "append", "insert", "replace", "permit",
                        "lines"),
                Blank());
    }

    // todo
    open fun LineStart(): Rule {
        return FirstOf(Spacing(), INDENT);
    }

    @SuppressSubnodes
    open fun Data(): Rule {
        return FirstOf(
                Identifier(),
                Run(),
                StringDoubleQuoted(),
                StringSingleQuoted());
    }

    @SuppressSubnodes
    @MemoMismatches
    open fun Identifier(): Rule {
        return Sequence(
                TestNot(Action()),
                Letter(),
                ZeroOrMore(LetterOrDigit()))
    }

    @SuppressSubnodes
    open fun Run(): Rule {
        return Quoted("Run", '`')
    }

    @SuppressSubnodes
    open fun StringDoubleQuoted(): Rule {
        return Quoted("StringDoubleQuoted", '"')
    }

    @SuppressSubnodes
    open fun StringSingleQuoted(): Rule {
        return Quoted("StringSingleQuoted", '\'')
    }

    @MemoMismatches
    open fun LetterOrDigit(): Rule {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Digit(), '_')
    };

    open fun Letter(): Rule = FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');

    open fun Digit(): Rule = CharRange('0', '9')

    @SuppressSubnodes
    open fun Spacing(): Rule {
        return ZeroOrMore(FirstOf(
                // whitespace
                OneOrMore(AnyOf(SPACE_LF_CHARS).label("Whitespace")),
                /*DEDENT,*/
                Sequence(
                        "#",
                        ZeroOrMore(TestNot(AnyOf(LF_CHARS)), BaseParser.ANY),
                        FirstOf(LF_CHARS, '\r', '\n', BaseParser.EOI)
                )
        ))
    }

    @SuppressSubnodes
    open fun Blank(): Rule {
        return OneOrMore(AnyOf(SPACE_CHARS).label("Blank"))
    }

    open fun Escape(): Rule {
        return Sequence('\\', AnyOf("btnfr\"\'\\"))
    }

    ///////////////////////////////////////////////////////////////////////////
    //                            Helpers                                    //
    ///////////////////////////////////////////////////////////////////////////

    open fun Quoted(label: String, quoteChar: Char): Rule {
        return Sequence(
                quoteChar,
                ZeroOrMore(
                        FirstOf(
                                Escape(),
                                Sequence(TestNot(AnyOf("${LF_CHARS}${quoteChar}\\")), BaseParser.ANY)
                        )
                ),
                quoteChar
        ).label(label).suppressSubnodes();
    }

    override fun fromCharLiteral(c: Char): Rule {
        // turn of creation of parse tree nodes for single characters
        return super.fromCharLiteral(c).suppressNode()
    }

}