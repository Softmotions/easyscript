#!/bin/bash

(echo 'eee' | grep ";oew;lewl")
 if [[ $? != '0' ]]; then
    echo "Fail ${HOME}";
    exit 1
 fi