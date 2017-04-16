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
                Spacing().suppressNode(),
                FirstBlock(),
                Spacing().suppressNode())
    }

    open fun FirstBlock(): Rule {
        return Sequence(
                BlockCore().label("FirstBlock::BlockCore"),
                FirstOf(Block().label("FirstBlock::Block"), Sequence(SpacingAll(), EOI).suppressNode())
        )
    }

    open fun Block(): Rule {
        return Sequence(
                FirstOf(IndentDedent(), Spacing().suppressNode()),
                BlockCore(),
                FirstOf(Block(), Sequence(SpacingAll(), EOI).suppressNode())
        )
    }

    open fun BlockCore(): Rule {
        return FirstOf(
                Echo(),
                Set(),
                Unset(),
                Run(),
                If(),
                Else(),
                Fail(),
                RunBlock(),
                Shell(),
                Send(),
                Each()
        )
    }

    open fun Each(): Rule {
        return Sequence(Action("each"),
                Identifier(),
                Blank(),
                "in",
                Blank(),
                Optional("read", Blank().suppressNode()),
                Data(),
                Optional(Blank(), As())
        )
    }

    open fun RunBlock(): Rule = Run()

    open fun Echo(): Rule {
        return Sequence(
                Action("echo"),
                Data()
        )
    }

    open fun Shell(): Rule {
        return Sequence(
                Action("shell"),
                Data()
        )
    }

    open fun Send(): Rule {
        return Sequence(
                Action("send"),
                FirstOf(Shell(), AtomicData()),
                SpacingNoLF(),
                FirstOf(">>", '>'),
                SpacingNoLF(),
                Data()
        )
    }


    open fun Fail(): Rule {
        return Sequence(
                Action("fail"),
                Optional(
                        FirstOf(StringMultiQuoted(),
                                StringDoubleQuoted(),
                                StringSingleQuoted() /* todo call */)
                ),
                Optional(
                        SpacingNoLF().suppressNode(),
                        Action("exit"),
                        Number()
                )
        )
    }

    open fun Set(): Rule {
        return Sequence(
                Action("set"),
                Optional(Sequence("env", Blank().suppressNode())).label("Env"),
                Identifier(),
                Blank().suppressNode(),
                Optional("read", Blank().suppressNode()),
                Data(),
                Optional(Blank(), As())
        )
    }

    open fun Unset(): Rule {
        return Sequence(
                Action("unset"),
                Optional(Sequence("env", Blank().suppressNode())).label("Env"),
                Identifier())
    }


    open fun As(): Rule {
        return Sequence(
                "as",
                Blank(),
                "lines",
                SpacingNoLF()
        )
    }

    open fun If(): Rule {
        return Sequence(
                Action("if"),
                IfBody()
        )
    }

    open fun Else(): Rule {
        return Sequence(String("else"), Optional(Blank().suppressNode(), If()))
    }

    open fun IfBody(): Rule {
        return Sequence(
                FirstOf(
                        IfFile(),
                        IfCompare(),
                        IfIn()
                ),
                Optional(
                        FirstOf(JoinAnd(), JoinOr()),
                        IfBody()
                )
        )
    }

    open fun IfIn(): Rule {
        return Sequence(
                AtomicData(),
                Blank(),
                Optional("not", Blank()),
                "in",
                Blank(),
                FirstOf(
                        Array(),
                        Sequence("read", Blank(), Data(),
                                Optional(Blank(), As()))
                )

        )
    }

    open fun IfFile(): Rule {
        return Sequence(
                FirstOf("file", "dir", "link"),
                Optional(Is()),
                Optional(Not()),
                FilePredicate(),
                Blank().suppressNode(),
                Data()
        )
    }

    open fun IfCompare(): Rule {
        return Sequence(
                AtomicData(),
                SpacingNoLF().suppressNode(),
                CompareOp(),
                SpacingNoLF().suppressNode(),
                AtomicData()
        )
    }

    open fun CompareOp(): Rule {
        return FirstOf(
                '=',
                "<=",
                ">="
        )
    }

    open fun FilePredicate(): Rule {
        return Sequence(
                Blank().suppressNode(),
                FirstOf(
                        "exists",
                        "exec",
                        "readable",
                        "writable"
                ))
    }


    open fun Is(): Rule {
        return Sequence(Blank().suppressNode(), String("is"));
    }

    open fun Not(): Rule {
        return Sequence(Blank().suppressNode(), String("not"));
    }

    open fun JoinAnd(): Rule {
        return Sequence(BlankMayLF().suppressNode(), String("and"), Blank());
    }

    open fun JoinOr(): Rule {
        return Sequence(BlankMayLF().suppressNode(), String("or"), Blank());
    }

    open fun Action(name: String): Rule {
        return Sequence(name, Blank().suppressNode());
    }

    @MemoMismatches
    open fun Action(): Rule {
        return Sequence(
                FirstOf(
                        "if", "else",
                        "set", "unset",
                        "echo", "fail", "send",
                        "each", "read", "call", "shell",
                        "as", "lines"),
                Blank().suppressNode());
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
                StringMultiQuoted(),
                StringDoubleQuoted(),
                StringSingleQuoted(),
                Number());
    }

    open fun Array(): Rule {
        return Sequence(
                '[',
                Spacing().suppressNode(),
                Optional(
                        AtomicData(),
                        ZeroOrMore(',', Spacing().suppressNode(), AtomicData())
                ),
                Spacing().suppressNode(),
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

    open fun StringMultiQuoted(): Rule {
        val quoteChar = "\"\"\"";
        return Sequence(
                quoteChar,
                ZeroOrMore(
                        Sequence(TestNot(AnyOf(quoteChar)), ANY)
                ),
                quoteChar
        ).suppressSubnodes();
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
    open fun SpacingAll(): Rule = ZeroOrMore(FirstOf(AnyOf(SPACE_LF_CHARS), Indent(), Dedent()))

    @SuppressSubnodes
    open fun Spacing(): Rule = ZeroOrMore(AnyOf(SPACE_LF_CHARS))

    @SuppressSubnodes
    open fun SpacingNoLF(): Rule = ZeroOrMore(AnyOf(SPACE_CHARS))

    @SuppressSubnodes
    open fun Blank(): Rule = OneOrMore(AnyOf(SPACE_CHARS))

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