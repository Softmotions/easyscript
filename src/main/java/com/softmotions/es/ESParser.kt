package com.softmotions.es

import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.MemoMismatches
import org.parboiled.annotations.SuppressSubnodes
import org.parboiled.support.Chars

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
        val SPACE_ALL_CHARS = "${SPACE_CHARS}${LF_CHARS}${Chars.INDENT}${Chars.DEDENT}"
    }

    open fun Script(): Rule {
        return Sequence(
                Spacing(),
                FirstBlock(),
                Spacing())
    }

    open fun FirstBlock(): Rule {
        return Sequence(
                BlockCore().label("FirstBlock::BlockCore"),
                FirstOf(Block().label("FirstBlock::Block"), Sequence(SpacingAll(), EOI))
        )
    }

    open fun Block(): Rule {
        return Sequence(
                FirstOf(IndentDedent(), Spacing()),
                BlockCore(),
                FirstOf(Block(), Sequence(SpacingAll(), EOI))
        )
    }

    open fun BlockCore(): Rule {
        return FirstOf(
                Set(),
                Unset(),
                If(),
                Echo()
        )
    }

    open fun Echo(): Rule {
        return Sequence(
                Action("echo"),
                Data()
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
                IfBody()
        )
    }

    open fun IfBody(): Rule {
        return Sequence(
                FirstOf(
                        IfFile(),
                        IfCompare()
                ),
                Optional(
                        FirstOf(JoinAnd(), JoinOr()),
                        IfBody()
                )
        )
    }

    open fun IfFile(): Rule {
        return Sequence(
                FirstOf("file", "dir", "link"),
                Optional(Is()),
                Optional(Not()),
                FilePredicate(),
                Blank(),
                Data()
        )
    }

    open fun FilePredicate(): Rule {
        return Sequence(
                Blank(),
                FirstOf(
                        "exists",
                        "exec",
                        "readable",
                        "writable"
                ))
    }

    open fun IfCompare(): Rule {
        return NOTHING
    }

    open fun Is(): Rule {
        return Sequence(Blank(), String("is"));
    }

    open fun Not(): Rule {
        return Sequence(Blank(), String("not"));
    }

    open fun JoinAnd(): Rule {
        return Sequence(BlankMayLF(), String("and"), Blank());
    }

    open fun JoinOr(): Rule {
        return Sequence(BlankMayLF(), String("or"), Blank());
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
    open fun Indent(): Rule = Sequence(Spacing(), INDENT)

    @SuppressSubnodes
    open fun Dedent(): Rule = Sequence(Spacing(), DEDENT)

    @SuppressSubnodes
    open fun IndentDedent(): Rule = OneOrMore(FirstOf(Indent(), Dedent()))

    @SuppressSubnodes
    open fun IndentDedentZeroOrMore(): Rule = ZeroOrMore(FirstOf(Indent(), Dedent()))

    @SuppressSubnodes
    open fun SpacingAll(): Rule = ZeroOrMore(AnyOf(SPACE_ALL_CHARS).label("SpacingAll"))

    @SuppressSubnodes
    open fun Spacing(): Rule = ZeroOrMore(AnyOf(SPACE_LF_CHARS).label("Spacing"))

    @SuppressSubnodes
    open fun Blank(): Rule = OneOrMore(AnyOf(SPACE_CHARS).label("Blank"))
    
    @SuppressSubnodes
    open fun BlankMayLF(): Rule {
        return Sequence(
                OneOrMore(AnyOf(SPACE_LF_CHARS)),
                IndentDedentZeroOrMore())
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