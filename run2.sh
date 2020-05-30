#!/bin/bash

# prepare path
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

cd 'C:\robocode'

for i in {1..99999}
do
  java -Xmx2048M -cp libs/robocode.jar -XX:+IgnoreUnrecognizedVMOptions "--add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED" "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" "--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED" "--add-opens=java.desktop/sun.awt=ALL-UNNAMED" robocode.Robocode -nodisplay -battle $SCRIPTPATH/configuration2.battle -results $SCRIPTPATH/tmp_result2.txt
  cat $SCRIPTPATH/tmp_result2.txt >> $SCRIPTPATH/result2.txt
done

exit