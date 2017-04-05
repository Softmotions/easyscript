package com.softmotions.es

import org.parboiled.Parboiled
import org.parboiled.buffers.IndentDedentInputBuffer
import org.parboiled.common.StringBuilderSink
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.TracingParseRunner
import org.parboiled.support.ParseTreeUtils.printNodeTree

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
class EasyScriptMain {

    companion object {
        @JvmStatic fun main(vararg args: String) {
            EasyScriptMain().run()
        }
    }

    fun run(vararg args: String) {

        val data0 = """

set VAR0 test
    set VAR0 test
        set env VAR0 test
        set VAR0 test
set VAR0 test

"""

        val data7 = """

set VAR [one, two, `three`,"four",1111]

"""

        val data6 = """
# Simple comment
 # This is second comment
set VAR0 test
    set BAR1 'bar'      ## Set my var
        set DAR2 `dar`

set env ZAR1 "zbar"
    set env ZAR2 "zbar2"
"""
        val data5 = """

    set env VAR01 `test`
    set VAR [one, \"two\",99]

"""
        val data4 = "set env VAR01 `test`"
        val data3 = "set VAR01 'test'"
        val data2 = "set VAR01 \"test\""
        val data1 = "set VAR0 test"

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
//if file exists "{FILE}"
//    echo "File {FILE} exists"
//else
//    echo "File {FILE} is not exists"
//
//`cat {FILE}`
//    fail "Oops.." exit 1
//"""
        val data = data0;
        val parser = Parboiled.createParser(ESPTreeParser::class.java);
        
//        val result = ReportingParseRunner<Any>(parser.Sript())
//                .run(IndentDedentInputBuffer(data.toCharArray(), 4, "#", true, true))
        val runner = TracingParseRunner<Any>(parser.Script());
        runner.withLog(StringBuilderSink())
        val result = runner
                .run(IndentDedentInputBuffer(data.toCharArray(), 4, "#", true, true))
        if (!result.parseErrors.isEmpty()) {
            println(ErrorUtils.printParseError(result.parseErrors[0]))
            System.out.println(runner.log);
        } else {
            println("NodeTree: ${printNodeTree(result)}\n")
            val value = result.parseTreeRoot?.value
            println(value ?: "")
        }
    }
}