#!/bin/bash
dir="log_dropouts"
if [ ! -d $dir ]; then
    mkdir $dir
fi

set -e
set -x

vm_option="-Xms6086m -Xmx8480m"
jar1="aaia/build/libs/aaia.jar"
jar2="psecagg/build/libs/psecagg.jar"
jar3="verifynet/build/libs/verifynet.jar"

i=100
g=1000

for m in 0.0 0.1 0.2 0.3
do
  fail=$(echo "${m}*${i}/1"|bc)
  java -cp ${jar1} ${vm_option} edu.bjut.aaia.app.AAIAApp -u $i -f $fail -g $g >> $dir/agg_${m}_${i}_${g}.log
  java -cp ${jar2} ${vm_option} edu.bjut.psecagg.app.AggApp -u $i -f $fail -g $g >> $dir/psec_${m}_${i}_${g}.log
  java -cp ${jar3} ${vm_option} edu.bjut.verifynet.app.VerifyNetApp -u $i -f $fail -g $g >> $dir/verifynet_${m}_${i}_${g}.log
done
