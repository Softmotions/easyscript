package com.softmotions.es

import com.softmotions.es.ast.*
import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.MemoMismatches
import org.parboiled.annotations.SuppressSubnodes
import org.parboiled.errors.ActionException
import org.parboiled.support.Var
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Easyscript parser.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@JvmSuppressWildcards
open class ESParser : BaseParser<Any>() {

    val log: Logger = LoggerFactory.getLogger(ESParser::class.java)

    companion object {
        val LF_CHAR = '\n'
        val LF_CHARS = "\r\n"
        val SPACE_CHARS = " \t"
        val SPACE_LF_CHARS = "${SPACE_CHARS}${LF_CHARS}"
    }

    val script: AstScript
        get() = peek(context.getValueStack().size() - 1) as AstScript


    fun actionLog(msg: String): Boolean {
        log.info(msg)
        return true
    }

    fun actionError(msg: String) {
        throw ActionException(msg)
    }

    fun asAstIndentBlock(n: Any): AstIndentBlock {
        if (n is AstIndentBlock) {
            return n
        } else {
            throw ActionException("Not an indent block: $n")
        }
    }

    fun asAstBlock(n: Any): AstBlock {
        if (n is AstBlock) {
            return n
        } else {
            throw ActionException("Not a block: $n")
        }
    }

    fun asAstNode(n: Any): AstNode {
        if (n is AstNode) {
            return n
        } else {
            throw ActionException("Not a node: $n")
        }
    }

    override fun push(value: Any?): Boolean {
        log.info("PUSH \t{}", value)
        return super.push(value)
    }

    override fun pop(): Any {
        return super.pop().also {
            log.info("POP  \t{}", it)
        }
    }

    open fun Script(): Rule {
        return Sequence(
                push(AstScript()),
                Spacing(),
                FirstBlock(),
                Spacing(),
                action {
                    while (context.valueStack.size() > 1 && peek() is AstIndentBlock) pop()
                    log.info("SCRIPT: {}", script)
                    log.info("VSZ: {}", context.valueStack.size())
                })
    }

    open fun FirstBlock(): Rule {
        return Sequence(
                BlockCore().label("FirstBlock::BlockCore"),
                action {
                    (peek(1) as AstScript).addChild(asAstNode(pop()))
                },
                FirstOf(Block().label("FirstBlock::Block"), Sequence(SpacingAll(), EOI))
        )
    }

    open fun Block(): Rule {
        val idn = Var<Boolean>()
        return Sequence(
                SpacingNoLF(),
                OneOrMore(AnyOf(LF_CHARS)),
                FirstOf(
                        Sequence(
                                FirstOf(
                                        Sequence(Indent(), idn.set(true)),
                                        Sequence(Dedent(), idn.set(false))
                                ),
                                BlockCore(),
                                action {
                                    if (idn.get()) {
                                        val iblock = asAstIndentBlock(asAstBlock(peek(1)).lastChild())
                                        iblock.addChild(asAstNode(pop()))
                                        push(iblock)
                                    } else {
                                        val node = asAstNode(pop())
                                        asAstIndentBlock(pop())
                                        asAstBlock(peek()).addChild(node)
                                    }
                                }),
                        Sequence(
                                SpacingNoLF(),
                                BlockCore(),
                                action {
                                    asAstBlock(peek(1)).addChild(asAstNode(pop()))
                                }
                        )
                ),
                FirstOf(Block(),
                        Sequence(SpacingAll(), EOI))
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
                Optional(ReadAs())
        )
    }

    open fun RunBlock(): Rule = Run()

    open fun Echo(): Rule {
        return Sequence(
                Action("echo"),
                Data(),
                action {
                    push(AstEcho(pop() as AstData))
                }
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
                        SpacingNoLF(),
                        Action("exit"),
                        Number()
                )
        )
    }

    open fun Set(): Rule {
        return Sequence(
                Action("set"),
                Optional(Sequence("env", Blank())).label("Env"),
                Identifier(),
                Blank().suppressNode(),
                Optional(ReadAs())
        )
    }

    open fun Unset(): Rule {
        return Sequence(
                Action("unset"),
                Optional(Sequence("env", Blank())).label("Env"),
                Identifier())
    }


    open fun As(): Rule {
        val vnode = Var<ReadAs>(ReadAs.DEFAULT)
        return Sequence(
                "as",
                Blank(),
                "lines",
                vnode.set(ReadAs.LINES),
                SpacingNoLF(),
                push(vnode.getAndSet(null))
        )
    }

    open fun If(): Rule {
        return Sequence(
                Action("if"),
                BooleanExp(),
                action {
                    push(AstIf(pop() as AstBooleanBlock))
                }
        )
    }

    open fun Else(): Rule {
        return Sequence(
                String("else"),
                Optional(Blank(), If())
                // todo
        )
    }

    open fun BooleanExp(): Rule {
        return Sequence(
                // todo handle NOT
                FirstOf(
                        IfFile(),
                        IfCompare(),
                        IfIn()),
                Optional(
                        FirstOf(JoinAnd(), JoinOr()),
                        BooleanExp(),
                        action {
                            val nexp = pop() as AstBooleanBlock
                            nexp.join = pop() as BooleanBlockJoin
                            asAstBlock(peek()).addChild(nexp)
                        }
                )
        )
    }

    open fun IfIn(): Rule {
        val vnode = Var<AstInBooleanNode>()
        return Sequence(
                AtomicData(),
                Blank(),
                vnode.set(AstInBooleanNode()),
                Optional("not", Blank(), action {
                    vnode.get().negate = !vnode.get().negate
                }),
                "in",
                Blank(),
                FirstOf(
                        ArrayData(),
                        ReadAs()
                ),
                action {
                    vnode.get().addChild(asAstNode(pop()))
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun IfFile(): Rule {
        val vnode = Var<AstFileBooleanNode>()
        return Sequence(
                Sequence(FirstOf(
                        "file", "dir", "link"),
                        action {
                            vnode.set(AstFileBooleanNode(
                                    when (match()) {
                                        "file" -> AstFileType.FILE
                                        "dir" -> AstFileType.DIR
                                        "link" -> AstFileType.LINK
                                        else -> throw ActionException("IfFile")
                                    }
                            ))
                        }),
                Optional(Is()),
                Optional(Not(), action {
                    vnode.get().negate = vnode.get().negate.not()
                }),
                FilePredicate(),
                action {
                    vnode.get().predicate = pop() as AstFilePredicate
                },
                Blank(),
                Data(),
                action {
                    vnode.get().data = pop() as AstData
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun IfCompare(): Rule {
        val vnode = Var<AstCompareBooleanBlock>()
        return Sequence(
                AtomicData(),
                vnode.setAndGet(AstCompareBooleanBlock()).addChild(pop() as AstAtomicData),
                SpacingNoLF(),
                CompareOp(),
                action {
                    vnode.get().op = pop() as AstCompareOp
                },
                SpacingNoLF(),
                AtomicData(),
                vnode.get().addChild(pop() as AstAtomicData),
                push(vnode.getAndSet(null))
        )
    }

    open fun CompareOp(): Rule {
        return Sequence(FirstOf(
                '=',
                "<=",
                ">=",
                '>',
                '<'),
                action {
                    push(when (match()) {
                        "=" -> AstCompareOp.EQ
                        "<=" -> AstCompareOp.LTE
                        ">=" -> AstCompareOp.GTE
                        "<" -> AstCompareOp.LT
                        ">" -> AstCompareOp.GT
                        else -> actionError("CompareOp")
                    })
                }
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
                ),
                action {
                    push(when (match()) {
                        "exists" -> AstFilePredicate.EXISTS
                        "exec" -> AstFilePredicate.EXEC
                        "readable" -> AstFilePredicate.READABLE
                        "writable" -> AstFilePredicate.WRITABLE
                        else -> actionError("FilePredicate")
                    })
                }
        )
    }


    open fun Is(): Rule {
        return Sequence(Blank(), String("is"));
    }

    open fun Not(): Rule {
        return Sequence(Blank(), String("not"));
    }

    open fun JoinAnd(): Rule {
        return Sequence(BlankMayLF(), String("and"), Blank(), push(BooleanBlockJoin.AND));
    }

    open fun JoinOr(): Rule {
        return Sequence(BlankMayLF(), String("or"), Blank(), push(BooleanBlockJoin.OR));
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
                        "echo", "fail", "send",
                        "each", "read", "call", "shell",
                        "as", "lines"),
                Blank());
    }

    @SuppressSubnodes
    open fun ReadAs(): Rule {
        val vnode = Var<AstRead>()
        return Sequence(
                "read",
                Blank(),
                Data(),
                vnode.set(AstRead(pop() as AstData)),
                Optional(
                        Blank(),
                        As(),
                        action {
                            vnode.get().readAs = pop() as ReadAs
                        }
                ),
                push(vnode.getAndSet(null))
        )
    }

    @SuppressSubnodes
    open fun Data(): Rule {
        return FirstOf(
                AtomicData(),
                ArrayData());
    }

    @SuppressSubnodes
    open fun AtomicData(): Rule {
        return Sequence(
                FirstOf(
                        Identifier(),
                        Run(),
                        StringMultiQuoted(),
                        StringDoubleQuoted(),
                        StringSingleQuoted(),
                        Number()),
                push(AstAtomicData(pop() as TypedValue))
        );
    }

    open fun ArrayData(): Rule {
        val vnode = Var<AstArrayData>();
        return Sequence(
                '[',
                Spacing(),
                vnode.set(AstArrayData()),
                Optional(
                        AtomicData(),
                        action {
                            vnode.get().addChild(pop() as AstAtomicData)
                        },
                        ZeroOrMore(',',
                                Spacing(),
                                AtomicData(),
                                action {
                                    vnode.get().addChild(pop() as AstAtomicData)
                                })
                ),
                Spacing(),
                ']',
                push(vnode.getAndSet(null))
        )
    }

    @SuppressSubnodes
    open fun Identifier(): Rule {
        return Sequence(
                TestNot(Action()),
                Letter(),
                ZeroOrMore(LetterOrDigit()),
                push(TypedValue.identifier(match()))
        )
    }

    @SuppressSubnodes
    open fun Number(): Rule {
        return Sequence(
                OneOrMore(Digit()),
                push(TypedValue.number(match()))
        )
    }

    @SuppressSubnodes
    open fun Run(): Rule {
        return Sequence(
                Quoted("Run", '`'),
                push(TypedValue.run(match()))
        )
    }

    open fun StringMultiQuoted(): Rule {
        val quoteChar = "\"\"\"";
        return Sequence(
                quoteChar,
                ZeroOrMore(
                        Sequence(TestNot(AnyOf(quoteChar)), ANY)
                ),
                quoteChar,
                push(TypedValue.mquoted(match()))
        ).suppressSubnodes();
    }


    @SuppressSubnodes
    open fun StringDoubleQuoted(): Rule {
        return Sequence(
                Quoted("StringDoubleQuoted", '"'),
                push(TypedValue.dquoted(match()))
        )
    }

    @SuppressSubnodes
    open fun StringSingleQuoted(): Rule {
        return Sequence(
                Quoted("StringSingleQuoted", '\''),
                push(TypedValue.squoted(match()))
        )
    }

    @MemoMismatches
    open fun LetterOrDigit(): Rule {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Digit(), '_')
    };

    @SuppressSubnodes
    open fun Letter(): Rule = FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');

    @SuppressSubnodes
    open fun Digit(): Rule = CharRange('0', '9')

    @SuppressSubnodes
    open fun Indent(): Rule = OneOrMore(INDENT)

    @SuppressSubnodes
    open fun Dedent(): Rule = OneOrMore(DEDENT)

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

    fun ESParser.action(block: ESParser.() -> Unit): Boolean {
        block()
        return true
    }

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