@echo off
rem ======================================================================
rem beach install local maven repostory script
rem
rem author: felix
rem date: 2019-07-11
rem ======================================================================

cd %~dp0
call mvn install:install-file -DgroupId=com.intersys -DartifactId=cachedb -Dversion=1.0.0 -Dpackaging=jar -Dfile=CacheDB.jar
call mvn install:install-file -DgroupId=org.postgresql -DartifactId=gsjdbc4 -Dversion=1.0.0 -Dpackaging=jar -Dfile=gsjdbc4.jar
call mvn install:install-file -DgroupId=com.microsoft.sqlserver -DartifactId=sqljdbc4 -Dversion=1.0.0 -Dpackaging=jar -Dfile=sqljdbc4.jar
pause