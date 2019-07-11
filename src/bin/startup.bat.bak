@echo off & title elasticsaerch-java-service

REM 后续命令使用的是：UTF-8编码
chcp 65001
set DIR=%CD%

cd %0\..\..
set BASEDIR=%CD%

java -jar -Xmx4096m -Dfile.encoding=utf-8 %BASEDIR%\demo-2.3.4.RELEASE.jar --spring.profiles.active=prod
pause

