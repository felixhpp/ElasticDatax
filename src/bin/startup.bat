@echo off
rem ======================================================================
rem windows startup ElasticDatax script
rem
rem author: felix
rem date: 2019-07-11
rem ======================================================================

rem Open in a browser
rem start "" "http://localhost:8161/main"

rem startup jar
set DIR=%CD%
cd %0\..\..
set BASEDIR=%CD%
java -jar -Xmx1024m -Dfile.encoding=utf-8 boot/demo-2.3.4.RELEASE.jar --spring.config.location=config/ --spring.profiles.active=prod

pause