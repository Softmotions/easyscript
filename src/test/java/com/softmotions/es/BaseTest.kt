package com.softmotions.es

import com.softmotions.es.ast.AstScript
import org.parboiled.Parboiled
import org.parboiled.buffers.IndentDedentInputBuffer
import org.parboiled.common.StringBuilderSink
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.TracingParseRunner
import org.slf4j.LoggerFactory
import org.testng.Assert.*

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
open class BaseTest {

    @JvmField
    protected val log = LoggerFactory.getLogger(javaClass)

    companion object {
        @JvmStatic
        protected val TQ = "\"\"\""
    }

    val parser: ESPTreeParser by lazy(LazyThreadSafetyMode.NONE, {
        Parboiled.createParser(ESPTreeParser::class.java)
    })

    open fun setup() {
    }

    fun parse(fail: Boolean = false,
              text: () -> String): AstScript {
        val script = AstScript()
        val runner = TracingParseRunner<Any>(parser.Script(script)).withLog(StringBuilderSink())
        val result = runner
                .run(IndentDedentInputBuffer(text().toCharArray(),
                        4, "#", false, true))
        assertTrue(if (result.parseErrors.isEmpty()) !fail else {
            if (!fail) {
                println(ErrorUtils.printParseError(result.parseErrors[0]))
                System.err.println(runner.log);
            }
            fail
        })
        return script
    }

}