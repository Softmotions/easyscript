package com.softmotions.es

import com.softmotions.es.ast.*
import org.parboiled.BaseParser
import org.parboiled.Rule
import org.parboiled.annotations.MemoMismatches
import org.parboiled.annotations.SuppressSubnodes
import org.parboiled.support.Var
import org.slf4j.LoggerFactory

/**
 * Easyscript parser.
 *
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@JvmSuppressWildcards
open class ESParser : BaseParser<Any>() {

    val log = LoggerFactory.getLogger(ESParser::class.java)

    companion object {
        val LF_CHAR = '\n'
        val LF_CHARS = "\r\n"
        val SPACE_CHARS = " \t"
        val SPACE_LF_CHARS = "${SPACE_CHARS}${LF_CHARS}"
    }

    val script: AstScript
        get() = peek(context.getValueStack().size() - 1) as AstScript


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
                Spacing().suppressNode(),
                FirstBlock(),
                Spacing().suppressNode(),
                action {
                    log.info("SCRIPT: {}", script)
                })
    }

    open fun FirstBlock(): Rule {
        return Sequence(
                BlockCore().label("FirstBlock::BlockCore"),
                FirstOf(Block().label("FirstBlock::Block"), Sequence(SpacingAll(), EOI).suppressNode())
        )
    }

    open fun Block(): Rule {
        return Sequence(
                SpacingNoLF(),
                OneOrMore(AnyOf(LF_CHARS)),
                Optional(IndentDedent()),
                Spacing(),
                BlockCore(),
                FirstOf(Block(), Sequence(SpacingAll(), EOI).suppressNode())
        )
    }

    open fun BlockCore(): Rule {
        return Sequence(
                FirstOf(
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
                ),
                action {
                    val node = pop() as AstNode
                    (peek() as AstBlock).addChildren(node)
                })
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
                Optional(ReadAs())
        )
    }

    open fun Unset(): Rule {
        return Sequence(
                Action("unset"),
                Optional(Sequence("env", Blank().suppressNode())).label("Env"),
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
                push(vnode.get())
        )
    }

    open fun If(): Rule {
        val vindent = Var<Int>(0)
        return Sequence(
                Action("if"),
                vindent.set(script.indent),
                BooleanExp(),
                action {
                    val bb = pop() as AstBooleanBlock
                    push(AstIf(vindent.get(), bb))
                }
        )
    }
    
    open fun Else(): Rule {
        val vindent = Var<Int>(0)
        return Sequence(
                String("else"),
                vindent.set(script.indent),
                Optional(Blank(), If())
                // todo
        )
    }

    open fun BooleanExp(): Rule {
        val vnode = Var<AstBooleanBlock>()
        return Sequence(
                // todo handle NOT
                Sequence(FirstOf(
                        IfFile(),
                        IfCompare(),
                        IfIn()),
                        action {
                            vnode.set(pop() as AstBooleanBlock)
                        }
                ),
                Optional(
                        FirstOf(JoinAnd(), JoinOr()),
                        BooleanExp(),
                        action {
                            val nexp = pop() as AstBooleanBlock
                            nexp.join = pop() as BooleanBlockJoin
                            vnode.get().addChildren(nexp)
                        }
                ),
                push(vnode.get())
        )
    }

    open fun IfIn(): Rule {
        val node = AstInBooleanNode()
        return Sequence(
                AtomicData(),
                Blank(),
                Optional("not", Blank(), action {
                    node.negate = !node.negate
                }),
                "in",
                Blank(),
                FirstOf(
                        ArrayData(),
                        ReadAs()
                ),
                action {
                    node.addChildren(pop() as AstNode)
                    push(node)
                }
        )
    }

    open fun IfFile(): Rule {
        val node = AstFileBooleanNode()
        return Sequence(
                Sequence(FirstOf(
                        "file", "dir", "link"),
                        action {
                            node.type = when (match()) {
                                "file" -> AstFileType.FILE
                                "dir" -> AstFileType.DIR
                                "link" -> AstFileType.LINK
                                else -> error("IfFile")
                            }
                        }),
                Optional(Is()),
                Optional(Not(), action {
                    node.negate = node.negate.not()
                }),
                FilePredicate(),
                action {
                    node.predicate = pop() as AstFilePredicate
                },
                Blank().suppressNode(),
                Data(),
                action {
                    node.data = pop() as AstData
                    push(node)
                }
        )
    }

    open fun IfCompare(): Rule {
        val node = AstCompareBooleanBlock()
        return Sequence(
                AtomicData(),
                node.addChildren(pop() as AstAtomicData),
                SpacingNoLF().suppressNode(),
                CompareOp(),
                action {
                    node.op = pop() as AstCompareOp
                },
                SpacingNoLF().suppressNode(),
                AtomicData(),
                node.addChildren(pop() as AstAtomicData),
                push(node)
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
                        else -> error("CompareOp")
                    })
                }
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
                ),
                action {
                    push(when (match()) {
                        "exists" -> AstFilePredicate.EXISTS
                        "exec" -> AstFilePredicate.EXEC
                        "readable" -> AstFilePredicate.READABLE
                        "writable" -> AstFilePredicate.WRITABLE
                        else -> error("FilePredicate")
                    })
                }
        )
    }


    open fun Is(): Rule {
        return Sequence(Blank().suppressNode(), String("is"));
    }

    open fun Not(): Rule {
        return Sequence(Blank().suppressNode(), String("not"));
    }

    open fun JoinAnd(): Rule {
        return Sequence(BlankMayLF().suppressNode(), String("and"), Blank(), push(BooleanBlockJoin.AND));
    }

    open fun JoinOr(): Rule {
        return Sequence(BlankMayLF().suppressNode(), String("or"), Blank(), push(BooleanBlockJoin.OR));
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
    open fun ReadAs(): Rule {
        val node = AstRead()
        return Sequence(
                "read",
                Blank().suppressNode(),
                Data(),
                action {
                    node.data = pop() as AstData
                },
                Optional(
                        Blank().suppressNode(),
                        As(),
                        action {
                            node.readAs = pop() as ReadAs
                        }
                ),
                push(node)
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
        val array = AstArrayData()
        return Sequence(
                '[',
                Spacing().suppressNode(),
                Optional(
                        AtomicData(),
                        action {
                            array.addChildren(pop() as AstAtomicData)
                        },
                        ZeroOrMore(',',
                                Spacing().suppressNode(),
                                AtomicData(),
                                action {
                                    array.addChildren(pop() as AstAtomicData)
                                })
                ),
                Spacing().suppressNode(),
                ']',
                push(array)

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

    open fun Letter(): Rule = FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_');

    open fun Digit(): Rule = CharRange('0', '9')


    @SuppressSubnodes
    open fun Indent(): Rule = Sequence(Spacing(), INDENT, action {
        log.info("!!!INDENT")
        val s = this.script
        s.indent()
    })

    @SuppressSubnodes
    open fun Dedent(): Rule = Sequence(Spacing(), DEDENT, action {
        log.info("!!!DEDENT") // todo
        val s = this.script
        s.dedent()
        val node = peek()
        if (node is AstIndentBlock && node.indent == s.indent) {
            pop()
            val parent = peek();
            if (parent is AstBlock) {
                parent.addChildren(node)
            } else {
                parent as AstNode
                error("The '${parent.name}' is not a block")
            }
        }
    })

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