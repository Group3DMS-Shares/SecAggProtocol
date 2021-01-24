#!/bin/bash
set -e
for i in {2..500}
do
    echo $i
    java -cp aggprotocol/target/aggprotocol-1.0-SNAPSHOT-jar-with-dependencies.jar edu.bjut.aggprotocol.app.AggApp -u $i
    java -cp psecagg/target/psecagg-1.0-SNAPSHOT-jar-with-dependencies.jar edu.bjut.psecagg.app.AggApp -u $i
    java -cp verifynet/target/verifynet-1.0-SNAPSHOT-jar-with-dependencies.jar edu.bjut.verifynet.app.App -u $i
done
