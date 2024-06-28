@echo off
REM Compile the Java files
echo Compiling Java files...
javac -cp lib\postgresql-42.7.3.jar -d out\production\SRDC src\main\java\com\srdc\messageapp\client\Client.java src\main\java\com\srdc\messageapp\server\ClientHandler.java src\main\java\com\srdc\messageapp\server\Server.java src\main\java\com\srdc\messageapp\models\Message.java src\main\java\com\srdc\messageapp\models\User.java src\main\java\com\srdc\messageapp\database\DatabaseHandler.java

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
java -cp "lib\postgresql-42.7.3.jar;out\production\SRDC" com.srdc.messageapp.server.Server


echo Server started.
pause
