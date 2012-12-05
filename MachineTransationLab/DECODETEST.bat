@echo off

::-------------------- defaults
set defCorp=2048
set defSrc=en
set defTarg=de

::-------------------- ask
set /p corp="corpus? (default = %defCorp%) "
set /p src="source? (default = %defSrc%) "
set /p targ="target? (default = %defTarg%) "

::-------------------- set to defauts
if xx%corp% EQU xx set corp=%defCorp%
if xx%src% EQU xx set src=%defSrc%
if xx%targ% EQU xx set targ=%defTarg%

::-------------------- run
cd dist
java -jar MachineTransationLab.jar ../../%corp%.train %src% %targ% decode < ..\DECODETEST.txt >..\DECODEOUT.txt
cd ..
pause
