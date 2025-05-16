@echo off
setlocal enabledelayedexpansion

:: Get the current directory (where the script is located)
set SCRIPT_DIR=%~dp0
:: Set the project directory relative to where the script is located
set PROJECT_DIR=%SCRIPT_DIR%\namenode
:: Set the path to the target folder in the project directory
set TARGET_DIR=%PROJECT_DIR%\target

set JAR_FILE="%PROJECT_DIR%\target\namenode-0.0.1-SNAPSHOT.jar"
set "PROPERTIES_FILE=%PROJECT_DIR%\src\main\resources\application.properties"

:: List of IPs and ports to use
set "IP_PORT_LIST=127.0.0.20:8081:Warre 127.0.0.30:8082:Daan 127.0.0.40:8083:Arvo"

echo Deleting existing target directory...
rmdir /s /q "%TARGET_DIR%"

:: Step 1: Build the Maven project
echo Building Maven project...
cd /d %PROJECT_DIR%
call mvn install:install-file -Dfile=jade/lib/jade.jar -DgroupId=com.tilab.jade -DartifactId=jade -Dversion=4.6.0 -Dpackaging=jar
call mvn clean package -DskipTests

:: Check if build was successful
if errorlevel 1 (
    echo Maven build failed! Exiting...
    exit /b 1
)

:: Step 2: Run instances with different IPs and ports
echo Starting servers and agents

for %%A in (%IP_PORT_LIST%) do (
    :: Split the pair into IP and Port using the colon (":") delimiter
    for /f "tokens=1,2,3 delims=:" %%B in ("%%A") do (
        echo IP: %%B, Port: %%C, Name: %%D
        set SERVER_PORT=%%C
        set SERVER_IP=%%B
        set SERVER_NAME=%%D
        set NS_IP=143.169.218.121
        set NS_PORT=8090
        set /a AGENT_PORT=!SERVER_PORT!+1

        if not exist "local_files_%%D" mkdir "local_files_%%D"
        if not exist "logs_%%D" mkdir "logs_%%D"
        if not exist "replicated_files_%%D" mkdir "replicated_files_%%D"
        start "%%D - %%B:%%C" cmd /k "java -jar %JAR_FILE%"
        echo agent port: !AGENT_PORT!
        start "%%D - Sync agent" cmd /k "java -cp target/classes;jade/lib/jade.jar jade.Boot -gui -agents %%D:kaasenwijn.namenode.agents.SyncAgent  -host localhost -port !AGENT_PORT!"

    )
)


echo All processes started.
endlocal
