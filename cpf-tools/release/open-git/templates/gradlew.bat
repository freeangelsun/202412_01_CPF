@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Keep Gradle cache/build diagnostics and JVM crash/OOM artifacts out of the repository root.
set "CPF_GENERATED_EVIDENCE=%APP_HOME%\build\cpf-gradle"
set "CPF_GRADLE_PROJECT_CACHE=%CPF_GENERATED_EVIDENCE%\gradle\project-cache"
set "CPF_MANAGED_GRADLE_ROOT=%CPF_GENERATED_EVIDENCE%\gradle\managed-builds"
set "CPF_JVM_CRASH_DIR=%CPF_GENERATED_EVIDENCE%\jvm\crash"
set "CPF_JVM_HEAP_DUMP_DIR=%CPF_GENERATED_EVIDENCE%\jvm\heap-dump"
if not exist "%CPF_JVM_CRASH_DIR%" mkdir "%CPF_JVM_CRASH_DIR%" || goto fail
if not exist "%CPF_JVM_HEAP_DUMP_DIR%" mkdir "%CPF_JVM_HEAP_DUMP_DIR%" || goto fail
set "JAVA_TOOL_OPTIONS=%JAVA_TOOL_OPTIONS% -XX:+HeapDumpOnOutOfMemoryError "-XX:ErrorFile=%CPF_JVM_CRASH_DIR%\java-hs_err_pid%%p.log" "-XX:HeapDumpPath=%CPF_JVM_HEAP_DUMP_DIR%""

@rem CPF project-local resource policy. This affects this repository wrapper only.
if "%CPF_RESOURCE_PROFILE%"=="" set "CPF_RESOURCE_PROFILE=local"
call :cpfParseResourceProfile %*
goto cpfProfileParsed

:cpfParseResourceProfile
if "%~1"=="" goto :eof
for /f "tokens=1,* delims==" %%B in ("%~1") do (
  if /i "%%B"=="-PcpfResourceProfile" if not "%%C"=="" set "CPF_RESOURCE_PROFILE=%%C"
)
shift
goto cpfParseResourceProfile

:cpfProfileParsed
set "CPF_RESOURCE_COMMON=%APP_HOME%\gradle\cpf-runtime\common.properties"
set "CPF_RESOURCE_ENV=%APP_HOME%\gradle\cpf-runtime\%CPF_RESOURCE_PROFILE%.properties"
if not exist "%CPF_RESOURCE_COMMON%" (
  echo ERROR: CPF resource policy missing: %CPF_RESOURCE_COMMON% 1>&2
  goto fail
)
if not exist "%CPF_RESOURCE_ENV%" (
  echo ERROR: CPF resource profile missing: %CPF_RESOURCE_ENV% 1>&2
  goto fail
)
set "CPF_GRADLE_XMS="
set "CPF_GRADLE_XMX="
set "CPF_GRADLE_METASPACE="
set "CPF_GRADLE_WORKERS="
set "CPF_GRADLE_PARALLEL="
for %%F in ("%CPF_RESOURCE_COMMON%" "%CPF_RESOURCE_ENV%") do (
  for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%%~F") do (
    if /i "%%A"=="gradle.jvm.xms" set "CPF_GRADLE_XMS=%%B"
    if /i "%%A"=="gradle.jvm.xmx" set "CPF_GRADLE_XMX=%%B"
    if /i "%%A"=="gradle.jvm.maxMetaspace" set "CPF_GRADLE_METASPACE=%%B"
    if /i "%%A"=="gradle.maxWorkers" set "CPF_GRADLE_WORKERS=%%B"
    if /i "%%A"=="gradle.parallel" set "CPF_GRADLE_PARALLEL=%%B"
  )
)
if "%CPF_GRADLE_XMS%"=="" set CPF_GRADLE_XMS=250m
if "%CPF_GRADLE_XMX%"=="" set CPF_GRADLE_XMX=1000m
if "%CPF_GRADLE_METASPACE%"=="" set CPF_GRADLE_METASPACE=256m
if "%CPF_GRADLE_WORKERS%"=="" set CPF_GRADLE_WORKERS=2
if "%CPF_GRADLE_PARALLEL%"=="" set CPF_GRADLE_PARALLEL=false
set "GRADLE_OPTS=-Xms%CPF_GRADLE_XMS% -Xmx%CPF_GRADLE_XMX% -XX:MaxMetaspaceSize=%CPF_GRADLE_METASPACE% -Dorg.gradle.workers.max=%CPF_GRADLE_WORKERS% -Dorg.gradle.parallel=%CPF_GRADLE_PARALLEL% %GRADLE_OPTS%"

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line



@rem Execute Gradle
@rem Each included Build owns its default .gradle project cache.
@rem A shared --project-cache-dir makes included Builds share stale-output state.
@rem Keep only the CPF-managed Gradle root shared by this launcher.
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" "-PcpfManagedGradleRoot=%CPF_MANAGED_GRADLE_ROOT%" %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
