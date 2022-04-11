#!/bin/bash
dir="log_single"
if [ ! -d $dir ]; then
    mkdir $dir
fi

set -e

for m in 0.0 0.1 0.2 0.3
do
    for i in {100..501..50}
    do
        fail=$(echo "${m}*${i}/1"|bc)
        java -cp aggprotocol/target/aggprotocol-1.0-SNAPSHOT-jar-with-dependencies.jar  -Xms8086m -Xmx20480m  edu.bjut.aggprotocol.app.AggApp -u $i -f $fail -g 1000 >> $dir/${m}_${i}_1000.log
    done
done
