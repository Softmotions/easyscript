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
    <set|env> <varname> <literal|array|run>
    <read|lines|find|run> as <varname>
    <varname> <write>

### if

    if <file|dir|link> [not]<exists|executable|readable|writable>
        <body>    
        
    if <varname|literal|file> [>|<|=|!=] <var2> [and|or] (group)
        <body>
    
### Run

run command `true` if exit code is not zero

    [lines] `<command>` [write]
        <body|fail handler>

### Fail handler

    fail [string|call] [exit <code>] 
    <echo> 
    
### Read
    
    read [lines] <file> [as <varname>]
    
### Write
    
    > [append|insert|replace] <file>
    
### Find

find evaluated to `true` if found.

    find [file|dir|link|grep] /regexp/[rxflags] in <filename|dirname|varname> [as <varname>]
    
### Function 
    
    call <fname> [arg1, arg2]
    <fname>(arg1, arg2)
        <fbody>
    
### Each 
    
    
    
    
### Shell
  
    shell
        <shell body>
        
        
# Examples        
        
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

read file.txt as VAR > append file.txt   # Read file into variable VAR and copy its data to file.txt
read lines file.txt as MyVar             # Read file into variable MyVar
lines `cmd` as MyVar                     # Executed comand cmd and parse output as lines


append """ Some multiline text 
            with {VAR} """ > file.txt
           
"Simple string" > file.txt              #  Replace file data with "Simple string"
append "Simple string" > file.txt       # Append to end of file
    
   
find /regexp/ in {VAR} as FOUND

find /regexp/ in file*name.txt 

replace /regexp/ with <var|literal> < <file>

shell 
    Here is free shell command
    cat <<  END
    
    ;l;l
    END
    
copy dir/**/f?le* <target>
 
permit group read write > <file>
permit 677 > <file>

if arg1 = "test"
    echo Hello 
    
if arg3 in ["one", "two", "free"]
    echo Hello
    
each myvar in ["foo", "bar"]
    append myvar > file.txt 
    
    
set printhelp(foo, bar)
    echo "This is a help of {foo} {bar}"
    

printhelp [read file.txt, lines]


```