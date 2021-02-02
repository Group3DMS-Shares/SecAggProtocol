#!/bin/bash

if [ ! -d "log" ]; then
    mkdir log
fi

set -e

for m in 0.0 0.1 0.2 0.3
do
    for i in {100..501..50}
    do
        fail=$(echo "${m}*${i}/1"|bc)
        java -cp aggprotocol/target/aggprotocol-1.0-SNAPSHOT-jar-with-dependencies.jar edu.bjut.aggprotocol.app.AggApp -u $i -f $fail -g 10000 >> log/${m}_${i}_10000.log
    done
done
