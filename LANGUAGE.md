# Language features

Words:

### Comments

    # Comment string

### Literals

    0-9 Number
    "Interpolated string"
    'Not interpolated string'
    """Muli 
        line string"""  
        
### Array
    <arraystart>: [
    <arrayend>:   ]
    <arraycomma>: ,
    
    <arraystart> [ <literal|call><arraycomma>* ]    
        
### Echo

    echo <literal|run|varname>

### Variables

    <varname>: [a-zA-Z][a-zA-Z0-9]*
    <set|env> <varname> <literal|array|run> [as lines]
    <varname> <write>

### if

    if <file|dir|link> [not]<exists|executable|readable|writable>
        <body>    
        
    if <varname|literal|file> [>|<|=|!=] <var2> [and|or] (group)
        <body>
    
### Run

run command `true` if exit code is not zero

    `<command>`
        <body|fail handler>

### Fail handler

    fail [string|call] [exit <code>] 
    <echo> 
        
### Write
    
    send "simple string" >> myfile

    send """ This is my text
        Next line
    """ > 'myfile.txt'
    
    send `ls -al` >> 'myfile'
    
    send shell """
        some text 
    """ > file
    
    
    
### Find

find evaluated to `true` if found.

    find [file|dir|link|grep] /regexp/[rxflags] in <filename|dirname|varname> [as <varname>]
    
### Function 
    
    call <fname> [arg1, arg2]
    <fname>(arg1, arg2)
        <fbody>
    
### Each 
    
    
    
    
### Shell
  
    shell """
        <shell body>
    """
    
    shell `cat ./myscript.sh`
    
    shell 'ls'
        
        
# Examples       
 
```text
    
        
    `mktemp -d` as TD
    copy ./**/*.f > TD
    
    if file exists 'myfile'
       echo "Copying {it} to {TD}"
       copy it > TD
    
    
    if file exists 'myfile' as FF
       echo "Copying {FF} to {TD}"
       copy FF > TD
       
    
    `tar -cvf {TD:filename}.tgz {TD}`
    `rm -rf {TD}`

    
``` 
 
        
```text

if file exists <file|dir>
    body.. 

if dir not exists <file> 
    body..
	

if file executable <file|dir>
    body..


if file readable <file|dir>
    body..


if file writable <file|dir>
	<do something>
		<do something>
		<do something>
	<do something>
		

if file exists <dir> and (file exists or file exec) <file>
	body..

`cmd`              # Run command 
bg `cmd`           # Run command in background 
 
each myline in `ps -Af`
	echo myline
	myline > file
	append line > file
	send line > adamansky@gmail.com 	   #send line as email
	send line > adamansky@gmail.com file   #send line to file (file as specifier) 

set env MYENV "Var"     # Set environment variable MYENV
set MYVAR 1             # Set local variable 
set MYVAR 22

set VA1 `cat ./myfile.txt` as lines


send """ Some multiline text 
            with {VAR} """ >> 'file.txt'  # Append
           
send "Simple string" > file.txt           # Replace file data with "Simple string"
    
   
find /regexp/ in {VAR} as FOUND

find /regexp/ in file*name.txt 

replace /regexp/ with <var|literal> < <file>

shell """
    Here is free shell command
    cat <<  END
    
    ;l;l
    END
"""
     
permit group read write > <file>
permit 677 > <file>

if arg1 = "test"
    echo Hello 
    
if arg3 in ["one", "two", "free"]
    echo Hello
    
each myvar in ["foo", "bar"]
    send myvar > file.txt 
    
    
fun printhelp(foo, bar)
    echo "This is a help of {foo} {bar}"
    

printhelp(read file.txt, lines)


```