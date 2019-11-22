@echo off
rem ======================================================================
rem beach install local maven repostory script
rem
rem author: felix
rem date: 2019-07-11
rem ======================================================================

cd %~dp0
call mvn install:install-file -DgroupId=com.intersys -DartifactId=cachedb -Dversion=1.0.0 -Dpackaging=jar -Dfile=CacheDB.jar
call mvn install:install-file -DgroupId=org.apache.commons -DartifactId=commons-collections -Dversion=3.2.2 -Dpackaging=jar -Dfile=commons-collections-3.2.2.jar
call mvn install:install-file -DgroupId=org.apache.hadoop -DartifactId=hadoop-auth -Dversion=2.7.2 -Dpackaging=jar -Dfile=hadoop-auth-2.7.2.jar
call mvn install:install-file -DgroupId=org.apache.hadoop -DartifactId=hadoop-hdfs -Dversion=2.7.2 -Dpackaging=jar -Dfile=hadoop-hdfs-2.7.2.jar
call mvn install:install-file -DgroupId=org.apache.hadoop -DartifactId=hadoop-common -Dversion=2.7.2 -Dpackaging=jar -Dfile=hadoop-common-2.7.2.jar
call mvn install:install-file -DgroupId=org.apache.hadoop -DartifactId=hadoop-hdfs-client -Dversion=2.7.2 -Dpackaging=jar -Dfile=hadoop-hdfs-client-2.7.2.jar
call mvn install:install-file -DgroupId=org.apache.hadoop -DartifactId=hadoop-mapreduce-client-core -Dversion=2.7.2 -Dpackaging=jar -Dfile=hadoop-mapreduce-client-core-2.7.2.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbase-client -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-client-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbase-common -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-common-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbase-fi-secondaryindex -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-fi-secondaryindex-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbas-hindex -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-hindex-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbase-protocol -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-protocol-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbase-server -Dversion=1.3.1 -Dpackaging=jar -Dfile=hbase-server-1.3.1.jar
call mvn install:install-file -DgroupId=org.apache.hbase -DartifactId=hbaseFileStream -Dversion=1.0 -Dpackaging=jar -Dfile=hbaseFileStream-1.0.jar
call mvn install:install-file -DgroupId=com.huawei.hadoop -DartifactId=dynalogger -Dversion=1.0.0 -Dpackaging=jar -Dfile=dynalogger-V100R002C30.jar
call mvn install:install-file -DgroupId=com.huawei.zookeeper -DartifactId=zookeeper -Dversion=3.5.1 -Dpackaging=jar -Dfile=zookeeper-3.5.1.jar
call mvn install:install-file -DgroupId=org.apache.commons -DartifactId=commons-configuration -Dversion=1.6 -Dpackaging=jar -Dfile=commons-configuration-1.6.jar
call mvn install:install-file -DgroupId=org.postgresql -DartifactId=gsjdbc4 -Dversion=1.0.0 -Dpackaging=jar -Dfile=gsjdbc4.jar
call mvn install:install-file -DgroupId=com.dhcc.csmsearch -DartifactId=huawei-elasticsearch -Dversion=2.0.0 -Dpackaging=jar -Dfile=huawei-elasticsearch-2.0.0.jar
pause