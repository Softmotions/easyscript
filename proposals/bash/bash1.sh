#!/bin/bash

## time -p echo 'Test' |& wc -c

# tilde expansion only without quotes

# { echo -n -e "Hi!!! my name\nis ${loo}"; echo ' 222'; }

foo="zzzz"
export FOO=("Test${foo}test" `pwd` "d\n${foo}dd" $'sq\n{loo}sq')
echo "${FOO[@]}"