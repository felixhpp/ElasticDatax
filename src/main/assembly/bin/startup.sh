#!/bin/bash
#======================================================================
# linux startup project shell script
# boot path: spring boot jar package
# config path: config file dir
# logs path: project log
# logs/startup.log: project startup log
# nohup run
#
# author: felix
# date: 2019-10-17
#======================================================================

JAR_NAME=demo-2.3.4.RELEASE
Suffix=jar
APP_NAME=$JAR_NAME.$Suffix
CUR_SHELL_DIR=`pwd`
echo $CUR_SHELL_DIR
JAR_PATH=$CUR_SHELL_DIR/$APP_NAME
echo ">>> JAR_PATH: ${JAR_PATH} .<<<"
# LOG_PATH=./logs/start.log
LOG_PATH=$CUR_SHELL_DIR/logs/start.log
echo ">>> LOG_PATH: ${LOG_PATH} .<<<"
# SPRING_PROFILES_ACTIV="-Dspring.profiles.active=eureka2"
SPRING_PROFILES_ACTIV=""
JAVA_MEM_OPTS=" -server -Xms1024m -Xmx1024m -XX:PermSize=128m"
# JAVA_MEM_OPTS=""
PROFILES_ACTIVE=" --spring.profiles.active=prod"
# help command
usage() {
 echo "Usage: sh startup.sh [start|stop|restart|status]"
 exit 1
}

is_exist(){
 pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
 # if not found return 1 else return 0
 if [ -z "${pid}" ]; then
    return 1
 else
    return 0
 fi
}

start(){
 is_exist
 if [ $? -eq "0" ]; then
    echo ">>> ${APP_NAME} is already running. pid=${pid} .<<<"
 else
    nohup java $JAVA_MEM_OPTS -jar $JAR_PATH $PROFILES_ACTIVE >> $LOG_PATH 2>&1 &
    echo "${APP_NAME} start success"
 fi
}

stop(){
 is_exist
 if [ $? -eq "0" ]; then
    echo ">>> api 2 PID = $pid begin kill -9 $pid  <<<"
    kill -9 $pid
    sleep 2
    echo ">>> $JAR_NAME process stopped <<<"
 else
    echo ">>> ${APP_NAME} is not running >>>"
 fi
}

status(){
 is_exist
 if [ $? -eq "0" ]; then
    echo ">>> ${APP_NAME} is running. Pid is ${pid} <<<"
 else
    echo ">>> ${APP_NAME} is NOT running. <<<"
 fi
}

restart(){
 stop
 start
}

case "$1" in
 "start")
 start
 ;;
 "stop")
 stop
 ;;
 "status")
 status
 ;;
 "restart")
 restart
 ;;
 *)
 usage
 ;;
esac
