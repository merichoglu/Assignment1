@echo off
REM Start the client
echo Starting the client...
java -cp "lib\postgresql-42.7.3.jar;out\production\SRDC" com.srdc.messageapp.Client

echo Client started.
pause
