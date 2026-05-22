@if "%DEBUG%" == "" @echo off
@rem Gradle startup script for Windows

setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

set GRADLE_HOME=C:\Users\chent\.gradle\wrapper\dists\gradle-8.2-bin\bbg7u40eoinfdyxsxr3z4i7ta\gradle-8.2
set JAVA_HOME=C:\Users\chent\.jdks\jbr-17.0.14
set ANDROID_SDK_ROOT=C:\Users\chent\AppData\Local\Android\Sdk

"%GRADLE_HOME%\bin\gradle" %*
