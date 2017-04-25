package com.softmotions.es

import com.softmotions.es.ast.*
import org.parboiled.BaseParser
import org.parboiled.Rule
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
        val LF_CHARS = "\r\n"
        val SPACE_CHARS = " \t"
        val SPACE_LF_CHARS = "${SPACE_CHARS}${LF_CHARS}"
    }

    val script: AstScript by lazy(LazyThreadSafetyMode.NONE, {
        peek(context.valueStack.size() - 1) as AstScript
    })

    open fun Script(s: AstScript): Rule {
        return Sequence(
                push(s),
                Spacing(),
                FirstBlock(),
                Spacing(),
                action {
                    while (context.valueStack.size() > 1 && peek() is AstIndentBlock) pop()
                    if (context.valueStack.size() > 1) {
                        throw IllegalStateException("Invalid parser state. Stack: " + context.valueStack)
                    }
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
                                    val pn = peek(1)
                                    if (pn is AstIndentBlock) {
                                        val lc = pn.lastChild()
                                        if (lc is AstNestedBodyAware) {
                                            throw ActionException("'${lc.name}' statement must contain a nested body")
                                        }
                                    }
                                    asAstBlock(pn).addChild(asAstNode(pop()))
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
                RunBlock(),
                If(),
                Else(),
                Fail(),
                Shell(),
                Send(),
                Each()
        )
    }

    open fun Each(): Rule {
        val vnode = Var<AstEach>()
        return Sequence(Action("each"),
                vnode.set(AstEach()),
                Identifier(),
                action {
                    vnode.get().identifier = (pop() as TypedValue).value
                },
                Blank(),
                "in",
                Blank(),
                Data(),
                action {
                    vnode.get().data = pop() as AstData
                },
                Optional(Blank(), As(), action {
                    vnode.get().readAs = pop() as ReadAs
                }),
                action {
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun RunBlock(): Rule {
        return Sequence(
                Run(),
                action {
                    push(AstRunBlock(pop() as TypedValue))
                })
    }

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
                Data(),
                action {
                    push(AstShell(pop() as AstData))
                }
        )
    }

    open fun Send(): Rule {
        val vnode = Var<AstSend>()
        return Sequence(
                Action("send"),
                vnode.set(AstSend()),
                FirstOf(
                        Sequence(Shell(), action {
                            vnode.get().srcShell = pop() as AstShell
                        }),
                        Sequence(AtomicData(), action {
                            vnode.get().srcData = pop() as AstData
                        })
                ),
                SpacingNoLF(),
                FirstOf(
                        Sequence(">>", action {
                            vnode.get().sendOp = SendOp.APPEND
                        }),
                        Sequence('>', action {
                            vnode.get().sendOp = SendOp.REPLACE
                        })
                ),
                SpacingNoLF(),
                Data(),
                action {
                    vnode.get().target = pop() as AstData
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun Fail(): Rule {
        val vnode = Var<AstFail>()
        return Sequence(
                Action("fail"),
                action {
                    vnode.set(AstFail())
                },
                Optional(
                        FirstOf(StringMultiQuoted(),
                                StringDoubleQuoted(),
                                StringSingleQuoted() /* todo call */),
                        action {
                            vnode.get().msg = pop() as TypedValue
                        }
                ),
                Optional(
                        SpacingNoLF(),
                        Action("exit"),
                        Number(),
                        action {
                            vnode.get().exitCode = pop() as TypedValue
                        }
                ),
                action {
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun Set(): Rule {
        val vnode = Var<AstSet>();
        return Sequence(
                Action("set"),
                vnode.set(AstSet()),
                Optional(Sequence("env", Blank(), action {
                    vnode.get().isEnv = true
                })),
                Identifier(),
                action {
                    vnode.get().identifier = (pop() as TypedValue).value
                },
                Blank(),
                Data(),
                action {
                    vnode.get().data = pop() as AstData
                },
                Optional(
                        Blank(),
                        As(),
                        action {
                            vnode.get().readAs = pop() as ReadAs
                        }
                ),
                action {
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun Unset(): Rule {
        val vnode = Var<AstUnset>()
        return Sequence(
                Action("unset"),
                action {
                    vnode.set(AstUnset())
                },
                Optional(
                        "env",
                        Blank(),
                        action {
                            vnode.get().isEnv = true
                        }
                ),
                Identifier(),
                action {
                    vnode.get().identifier = (pop() as TypedValue).value
                    push(vnode.getAndSet(null))
                })
    }

    open fun As(): Rule {
        val vnode = Var<ReadAs>()
        return Sequence(
                "as",
                Blank(),
                FirstOf(
                        Sequence("lines", vnode.set(ReadAs.LINES)),
                        Sequence("words", vnode.set(ReadAs.WORDS)),
                        Sequence("chars", vnode.set(ReadAs.CHARS))
                ),
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
        val veif = Var<Boolean>()
        return Sequence(
                String("else"),
                veif.set(false),
                Optional(
                        Blank(),
                        If(),
                        action {
                            veif.set(true)
                            push(AstElse(asAstIf(pop())))
                        }
                ),
                action {
                    if (!veif.get()) {
                        push(AstElse())
                    }
                }
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
                            asAstBooleanBlock(peek()).next = nexp
                        }
                )
        )
    }

    open fun IfIn(): Rule {
        val vnode = Var<AstCompareBooleanBlock>()
        return Sequence(
                AtomicData(),
                action {
                    vnode.set(AstCompareBooleanBlock())
                    vnode.get().left = asAstNode(pop())
                },
                Blank(),
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
                    vnode.get().right = asAstNode(pop())
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
                action {
                    vnode.set(AstCompareBooleanBlock())
                    vnode.get().left = asAstNode(pop())
                },
                SpacingNoLF(),
                CompareOp(),
                action {
                    vnode.get().op = pop() as AstCompareOp
                },
                SpacingNoLF(),
                AtomicData(),
                action {
                    vnode.get().right = asAstNode(pop())
                    push(vnode.getAndSet(null))
                }
        )
    }

    open fun CompareOp(): Rule {
        return Sequence(FirstOf(
                "==",
                '=',
                "<=",
                ">=",
                '>',
                '<'),
                action {
                    push(when (match()) {
                        "=" -> AstCompareOp.EQ
                        "==" -> AstCompareOp.EQ
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

    @SuppressSubnodes
    open fun Action(): Rule {
        return Sequence(
                FirstOf(
                        "if", "else",
                        "set", "unset",
                        "echo", "fail", "send",
                        "each", "call", "shell",
                        "read", "as", "lines", "words", "chars"),
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
                Sequence(Letter(), ZeroOrMore(LetterOrDigit())),
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
                action {
                    push(TypedValue.run(match().let {
                        it.substring(1, it.length - 1)
                    }))
                }
        )
    }

    @SuppressSubnodes
    open fun StringMultiQuoted(): Rule {
        val quoteChar = "\"\"\""
        return Sequence(
                Sequence(
                        quoteChar,
                        ZeroOrMore(
                                TestNot(quoteChar),
                                ANY
                        ),
                        quoteChar),
                action {
                    push(TypedValue.mquoted(match().let {
                        it.substring(quoteChar.length, it.length - quoteChar.length)
                    }))
                }
        ).suppressSubnodes();
    }

    @SuppressSubnodes
    open fun StringDoubleQuoted(): Rule {
        return Sequence(
                Quoted("StringDoubleQuoted", '"'),
                action {
                    push(TypedValue.dquoted(match().let {
                        it.substring(1, it.length - 1)
                    }))
                }
        )
    }

    @SuppressSubnodes
    open fun StringSingleQuoted(): Rule {
        return Sequence(
                Quoted("StringSingleQuoted", '\''),
                action {
                    push(TypedValue.squoted(match().let {
                        it.substring(1, it.length - 1)
                    }))
                }
        )
    }

    @SuppressSubnodes
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

    @SuppressSubnodes
    open fun Escape(): Rule {
        return Sequence('\\', AnyOf("btnfr\"\'`\\"))
    }

    ////////////////////////////////////s///////////////////////////////////////
    //                            Helpers                                    //
    ///////////////////////////////////////////////////////////////////////////

    fun actionError(msg: String) {
        throw ActionException(msg)
    }

    fun asAstIf(n: Any): AstIf {
        if (n is AstIf) {
            return n
        } else throw ActionException(
                "Expected 'if/else' statement but found '${(n as? AstNode)?.name ?: n}'. " +
                        "Incorrect balancing of 'if else' statements")

    }

    fun asAstIndentBlock(n: Any): AstIndentBlock {
        if (n is AstIndentBlock) {
            return n
        } else throw ActionException(
                "Expected an indented block but found '${(n as? AstNode)?.name ?: n}'. " +
                        "Incorrect indentation")
    }

    fun asAstBlock(n: Any): AstBlock {
        if (n is AstBlock) {
            return n
        } else throw ActionException(
                "Expected a block but found '${(n as? AstNode)?.name ?: n}'. " +
                        "Incorrect indentation")
    }

    fun asAstBooleanBlock(n: Any): AstBooleanBlock {
        if (n is AstBooleanBlock) {
            return n
        } else throw ActionException(
                "Expected a boolean expression but found '${(n as? AstNode)?.name ?: n}'.")
    }

    fun asAstNode(n: Any): AstNode {
        if (n is AstNode) {
            return n
        } else {
            throw ActionException("Expected ast node bu found '${n}'")
        }
    }

    override fun push(value: Any?): Boolean {
        super.push(value)
        if (script.verbose) {
            log.info("PUSH \t{}", value)
        }
        return true
    }

    override fun pop(): Any {
        return super.pop().also {
            if (script.verbose) {
                log.info("POP  \t{}", it)
            }
        }
    }

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