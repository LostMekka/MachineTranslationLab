@echo off

::-------------------- defaults
set defCorp=4096
set defSrc=en
set defTarg=de
set defAct=decode

::-------------------- ask
set /p corp="corpus? (default = %defCorp%) "
set /p src="source? (default = %defSrc%) "
set /p targ="target? (default = %defTarg%) "
set /p act="action? (default = %defAct%) "

::-------------------- set to defauts
if xx%corp% EQU xx set corp=%defCorp%
if xx%src% EQU xx set src=%defSrc%
if xx%targ% EQU xx set targ=%defTarg%
if xx%act% EQU xx set act=%defAct%

::-------------------- run
cd dist
java -jar MachineTransationLab.jar ../../%corp%.train %src% %targ% %act%
cd ..
pause
