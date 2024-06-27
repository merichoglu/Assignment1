@echo off
REM Compile the Java files
echo Compiling Java files...
javac -cp lib\postgresql-42.7.3.jar -d out\production\SRDC src\main\java\com\srdc\messageapp\*.java
if %ERRORLEVEL% neq 0 (
    echo Compilation failed.
    pause
    exit /b %ERRORLEVEL%
)
echo Compilation successful.

REM List the compiled class files
echo Listing compiled class files...
dir out\production\SRDC\com\srdc\messageapp

REM Start the server
echo Starting the server...
java -cp "lib\postgresql-42.7.3.jar;out\production\SRDC" com.srdc.messageapp.Server

echo Server started.
pause
