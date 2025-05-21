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
set "IP_PORT_LIST=127.0.0.20:8081:Warre 127.0.0.30:8085:Daan 127.0.0.40:8091:Arvo"

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
set NS_IP=127.0.0.1
set NS_PORT=8090
for %%A in (%IP_PORT_LIST%) do (
    :: Split the pair into IP and Port using the colon (":") delimiter
    for /f "tokens=1,2,3 delims=:" %%B in ("%%A") do (
        echo IP: %%B, Port: %%C, Name: %%D
        set /a AGENT_PORT=%%C + 2
        set /a SELF_HTTP_PORT=%%C + 1

        if not exist "local_files_%%D" mkdir "local_files_%%D"
        if not exist "logs_%%D" mkdir "logs_%%D"
        if not exist "replicated_files_%%D" mkdir "replicated_files_%%D"

        start "%%D - %%B:%%C" cmd /k "java -DSERVER_PORT=%%C -DSERVER_IP=%%B -DSERVER_NAME=%%D -DNS_IP=%NS_IP% -DNS_PORT=%NS_PORT% -DAGENT_PORT=!AGENT_PORT! -DREMOTE=false -jar %JAR_FILE% --server.port=!SELF_HTTP_PORT!"
@REM         echo agent port: !AGENT_PORT!
@REM         start "%%D - Sync agent" cmd /k "java -DSERVER_PORT=%%C -DSERVER_IP=%%B -DSERVER_NAME=%%D -DNS_IP=%NS_IP% -DNS_PORT=%NS_PORT% -DAGENT_PORT=!AGENT_PORT! -DREMOTE=false  -cp target/classes;jade/lib/jade.jar jade.Boot -gui -agents %%D:kaasenwijn.namenode.agents.SyncAgent  -host localhost -port !AGENT_PORT! "

    )
)


echo All processes started.
endlocal
