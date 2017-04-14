package com.softmotions.es

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test
class BasicBlocks : BaseTest() {

    @BeforeClass
    override fun setup() {
        super.setup()
    }

    @Test
    fun testSet1() {
        parse(true) { "  " }
        parse(true) { " abr acad abra " }
        parse { "set VAR0 test" }
        parse { "set VAR01 \"test\"" }
        parse { "set VAR01 'test'" }
        parse { "set env VAR01 `test`" }
        parse {
            """

    set env VAR01 `test`
    set VAR [one, \"two\",99]

"""
        }
        parse {
            """
# Simple comment
 # This is second comment
set VAR0 test
    set BAR1 'bar'      ## Set my var
        set DAR2 `dar`

set env ZAR1 "zbar"
    set env ZAR2 "zbar2"
"""
        }
        parse {
            """

set VAR [one, two, `three`,"four",1111]

"""
        }
        parse {
            """

set VAR0 test
    set VAR0 test
        set env VAR0 test
        set VAR0 test
set VAR0 test
"""
        }
        parse {
            """
if file exists 'myfile.txt'
    echo 'Hello!'
"""
        }
        parse {
            """
if file exists ['myfile.txt', "bar.txt"]
    echo 'Hello!'
"""
        }

    }
}