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

    open fun Script(): Rule {
        return Sequence(
                Spacing(),
                Optional(FirstBlock()),
                Spacing())
    }

    open fun FirstBlock(): Rule {
        return Sequence(
                BlockCore().label("FirstBlock::Core"),
                Optional(Block().label("FirstBlock::Block"))
        )
    }

    open fun Block(): Rule {
        return Sequence(
                FirstOf(IndentDedent(), BlankWithLF()),
                BlockCore(),
                Optional(Block()),
                Optional(IndentDedent())
        )
    }

    @SuppressSubnodes
    open fun IndentDedent(): Rule = OneOrMore(FirstOf(Indent(), Dedent()))

    @SuppressSubnodes
    open fun Indent(): Rule = Sequence(Spacing(), INDENT)

    @SuppressSubnodes
    open fun Dedent(): Rule = Sequence(Spacing(), DEDENT)


    open fun BlockCore(): Rule {
        return FirstOf(
                Set(),
                Unset()
        )
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

    open fun If(): Rule {
        return Sequence(
                Action("if"),
                FirstOf(
                        IfFile(),
                        IfCompare()
                )
        )
    }

    open fun IfFile(): Rule {
        return Sequence(
                FirstOf("file", "dir", "link"),
                ANY
        )
    }

    open fun IfCompare(): Rule {
        return ANY
    }

    open fun Action(name: String): Rule {
        return Sequence(name, Blank());
    }

    @MemoMismatches
    open fun Action(): Rule {
        return Sequence(
                FirstOf(
                        "if", "else",
                        "set", "unset",
                        "echo", "fail",
                        "send", "read", "copy", "call", "shell",
                        "find", "append", "insert", "replace", "permit",
                        "lines"),
                Blank());
    }

    @SuppressSubnodes
    open fun Data(): Rule {
        return FirstOf(
                AtomicData(),
                Array());
    }

    @SuppressSubnodes
    open fun AtomicData(): Rule {
        return FirstOf(
                Identifier(),
                Run(),
                StringDoubleQuoted(),
                StringSingleQuoted(),
                Number());
    }

    open fun Array(): Rule {
        return Sequence(
                '[',
                Spacing(),
                Optional(
                        AtomicData(),
                        ZeroOrMore(',', Spacing(), AtomicData())
                ),
                Spacing(),
                ']'
        )
    }

    @SuppressSubnodes
    open fun Identifier(): Rule {
        return Sequence(
                TestNot(Action()),
                Letter(),
                ZeroOrMore(LetterOrDigit()))
    }

    @SuppressSubnodes
    open fun Number(): Rule = OneOrMore(Digit())

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
        return ZeroOrMore(AnyOf(SPACE_LF_CHARS).label("Spacing"))
    }

    @SuppressSubnodes
    open fun SpacingNoLF(): Rule {
        return ZeroOrMore(AnyOf(SPACE_CHARS).label("SpacingNoLF"))
    }

    @SuppressSubnodes
    open fun Blank(): Rule {
        return OneOrMore(AnyOf(SPACE_CHARS).label("Blank"))
    }

    @SuppressSubnodes
    open fun BlankWithLF(): Rule {
        return OneOrMore(
                Optional(AnyOf(SPACE_CHARS)),
                AnyOf(LF_CHARS),
                Optional(AnyOf(SPACE_CHARS))
        )
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
                                Sequence(TestNot(AnyOf("${LF_CHARS}${quoteChar}\\")), ANY)
                        )
                ),
                quoteChar
        ).label(label).suppressSubnodes();
    }

    override fun fromCharLiteral(c: Char): Rule {
        // turn of creation of parse tree nodes for single characters
        return super.fromCharLiteral(c).suppressNode()
    }

    fun debug(vararg s: String): Boolean {
        System.err.println(s.toList()) // set breakpoint here if required
        return true
    }

}