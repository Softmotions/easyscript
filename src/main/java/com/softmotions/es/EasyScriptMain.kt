package com.softmotions.es

import com.softmotions.es.ast.AstScript
import org.parboiled.Parboiled
import org.parboiled.buffers.IndentDedentInputBuffer
import org.parboiled.common.StringBuilderSink
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.TracingParseRunner

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class EasyScriptMain {

    companion object {

        @JvmStatic
        val TQ = "\"\"\""

        @JvmStatic fun main(vararg args: String) {
            EasyScriptMain().run()
        }
    }

    fun run() {
        val data0 = """

`cat {FILE}`
    fail "Ooops" exit 1
"""

//        val data1 = """
//set VAR0 110
//env VAR1 "test"
//set VAR2 `pwd`
//    echo "Failed to run"
//
//echo VAR2
//echo "VAR2={VAR2}"
//
//set FILE "~/.profile"

//`cat {FILE}`
//    fail "Oops.." exit 1
//"""
        val data = data0;
        val parser = Parboiled.createParser(ESParser::class.java)
        val script = AstScript(false)

//        val result = ReportingParseRunner<Any>(parser.Sript())
//                .run(IndentDedentInputBuffer(data.toCharArray(), 4, "#", true, true))
        val runner = TracingParseRunner<Any>(parser.Script(script));
        runner.withLog(StringBuilderSink())
        val result = runner.run(IndentDedentInputBuffer(data.toCharArray(), 4, "#", false, true))
        if (!result.parseErrors.isEmpty()) {
            println(ErrorUtils.printParseError(result.parseErrors[0]))
            //System.out.println(runner.log);
        } else {
            //println("NodeTree: ${printNodeTree(result)}\n")
            //val value = result.parseTreeRoot?.value
            //println(value ?: "")
            //System.out.println(runner.log);
            println()
            println(script)
        }
    }
}