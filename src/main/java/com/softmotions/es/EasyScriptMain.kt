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

        val data0 =             """

if A >= 'val'
    echo 'One'
    if A <= 'val2'
        echo 'One2'
        if `A` <= 'val3'
            echo 'One3'
        set V "foo"
set env E `ls`
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