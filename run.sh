#!/bin/bash

# prepare path
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")

cd 'C:\robocode'

for i in {1..99999}
do
  ./robocode.bat -nodisplay -battle $SCRIPTPATH/configuration.battle -results $SCRIPTPATH/tmp_result.txt
  cat $SCRIPTPATH/tmp_result.txt >> $SCRIPTPATH/result.txt
done

exit