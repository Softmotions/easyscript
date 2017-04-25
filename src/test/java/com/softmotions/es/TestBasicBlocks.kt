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
set MYVAR1 read 'myfile' as lines
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
if file exists 'myfile.txt'
    echo 'One'
    if file exists 'myfile2.txt'
        echo 'Three'
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

        parse {
            """

  echo $TQ
            Hello
    I'm a multiline
                    string

$TQ
"""
        }

        parse {
            """

  set VAR1 $TQ
    one
        two
            three
$TQ
"""
        }

        parse {
            """
  set VAR1 ['first', $TQ second
multi line $TQ  ]
"""
        }

        parse {
            """
set MYVAR1 `cat /etc/passwd` as lines
"""
        }


        parse {
            """

shell 'echo "test"'

"""
        }

        parse {
            """
shell $TQ
   ls -al './{FILE}';
   cat /etc/passwd > /tmp/passwd
$TQ
"""
        }

        parse {
            """
`ps -Af`
"""
        }

        parse {
            """
send `ps -Af` >> ['myfile.txt', 'myfile2.txt']
"""
        }

        parse {
            """
send $TQ My multi
line text
    $TQ > 'file.txt'
"""
        }

        parse {
            """
send shell $TQ My multi
line text
    $TQ > 'file.txt'
"""
        }

        parse {
            """
if VAR in ['one', 'two', "three"]
    echo "Hello!!!"
"""
        }

        parse {
            """
if VAR not in ['one', 'two', "three"]
    echo "Hello!!!"
"""
        }

        parse {
            """
if VAR in ['one', 'two', "three"] and ZZ = 'test'
    or BV in ["bvq"]
    echo "Hello!!!"
"""
        }

        parse {
            """
if 'test' in read 'myfile' as lines
    echo "Yes"
"""
        }

        parse {
            """
if 'test' in read ['myfile', 'myfile2']
    echo "Yes"
"""
        }

        parse {
            """
each myline in ['foo', "bar", 1213]
    echo myline
"""
        }

        parse {
            """
each myline in read '/etc/passwd'
    echo "line={myline}"
"""
        }

        parse {
            """
each myvar in `cat ./myfile.txt` as lines
    echo myvar
"""
        }

    }

}