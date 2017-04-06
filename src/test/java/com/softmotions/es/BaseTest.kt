package com.softmotions.es

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

    val parser: ESPTreeParser by lazy(LazyThreadSafetyMode.NONE, {
        Parboiled.createParser(ESPTreeParser::class.java)
    })

    open fun setup() {
    }

    fun parse(fail: Boolean = false,
              text: () -> String) {
        val runner = TracingParseRunner<Any>(parser.Script()).withLog(StringBuilderSink())
        val result = runner
                .run(IndentDedentInputBuffer(text().toCharArray(),
                        4, "#", true, true))
        assertTrue(if (result.parseErrors.isEmpty()) !fail else {
            if (!fail) {
                println(ErrorUtils.printParseError(result.parseErrors[0]))
                System.err.println(runner.log);
            }
            fail
        })
    }

}