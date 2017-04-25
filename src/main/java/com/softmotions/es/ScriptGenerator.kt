package com.softmotions.es

import com.softmotions.es.ast.AstScript
import java.io.Writer

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
interface ScriptGenerator {

    fun generate(ast: AstScript, out: Writer)
}