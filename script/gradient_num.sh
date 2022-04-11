#!/bin/bash

dir="log_gradient"
if [ ! -d $dir ]; then
    mkdir $dir
fi

set -e
set -x

vm_option="-Xms8086m -Xmx20480m"
jar1="aggprotocol/target/aggprotocol-1.0-SNAPSHOT-jar-with-dependencies.jar"
jar2="psecagg/target/psecagg-1.0-SNAPSHOT-jar-with-dependencies.jar"
jar3="verifynet/target/verifynet-1.0-SNAPSHOT-jar-with-dependencies.jar"

i=100

for m in 0.0 0.1 0.2 0.3
do
    for g in {1000..5001..1000}
    do
        fail=$(echo "${m}*${i}/1"|bc)
        java -cp ${jar1} ${vm_option} edu.bjut.aggprotocol.app.AggApp -u $i -f $fail -g $g >> $dir/agg_${m}_${i}_${g}.log
        java -cp ${jar2} ${vm_option} edu.bjut.psecagg.app.AggApp -u $i -f $fail -g $g >> $dir/psec_${m}_${i}_${g}.log
        java -cp ${jar3} ${vm_option} edu.bjut.verifynet.app.App -u $i -f $fail -g $g >> $dir/verifynet_${m}_${i}_${g}.log
    done
done
