package com.softmotions.es

import com.softmotions.es.ast.AstScript
import com.softmotions.es.bash.BashScriptGenerator
import org.apache.commons.io.output.StringBuilderWriter
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
        val D = "\$"

        @JvmStatic
        val TQ = "\"\"\""

        @JvmStatic fun main(vararg args: String) {
            EasyScriptMain().run()
        }
    }

    fun run() {
        val data0 = """

shell ${TQ}
    ps -Af;
    echo 'foo'
    echo "BAAAR!" ${TQ}
        fail "oopss" exit 22

"""

        val data = data0;
        val parser = Parboiled.createParser(ESParser::class.java)
        val script = AstScript(false)

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

            val gen = BashScriptGenerator()
            val w = StringBuilderWriter()
            gen.generate(script, w)
            println("\n\nBASH:")
            println(w)

        }
    }
}