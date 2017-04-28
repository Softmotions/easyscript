#!/bin/bash

(ps -Af;
    echo 'foo'
    echo "BAAAR!")
 if [[ $? != '0' ]]; then
    echo "Fail ${HOME}";
  (pwd)
   if [[ $? != '0' ]]; then
        echo $'Pwd \'\nfail';
   fi
 fi

