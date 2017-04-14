package com.softmotions.es

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
@Test
class TestBasicBlocks : BaseTest() {

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
        parse(true) {
            """
echo 'Hello1'
    ech1o bar
"""
        }

        parse {
            """

    set env VAR01 `test`
    set VAR [one, "two",99]

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

        parse {
            """
if file exists 'path.txt'
    and file exists `pwd`
    or file readable "/path/to/my/file.txt"
    echo 'Hello!'
"""
        }

        parse {
            """
if file exists 'path.txt'
    and file exists `pwd`
    or file readable "/path/to/my/file.txt" and file writable 'path2.txt'
    echo 'Hello!'
else if file exists 'path2.txt'
    echo 'ElseIf'
    if file exists 'path3.txt'
        echo 'ElseIf2'
    else
        echo 'ElseIf2Else'
else
    echo "Else"
"""
        }

        parse {
            """
if file not exists 'myfile.txt'
    fail "Fail1"

if file exists 'myfile2.txt'
    fail 'Fail 2' exit 22

if file exists 'myfile2.txt'
    fail exit 33

"""
        }

        parse {
            """

if A >= 'val'
    echo 'One'
    if A <= 'val2'
        echo 'One2'
        if `A` <= 'val3'
            echo 'One3'
        set V "foo"
set env E `ls`
"""
        }

    }

}