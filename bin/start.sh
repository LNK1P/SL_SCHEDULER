#!/bin/sh

HOME_DIR=/home/syslink/SL_SCHEDULER/bin

if [ "x$HOME_DIR" != "x" ]; then
    cd $HOME_DIR
    echo "Change HOME_DIR =$HOME_DIR"
fi


# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi


# Default JVM Options
JVM_OPTS="-server \
  -Dfile.encoding=UTF-8 \
  -Xss1m -Xms128m -Xmx256m \
  -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true"


# Program JVM Options
## CONF      : config directory , nlb.json, db.json .... 
## PROC      : 프로그램 명칭 1번,2번은 각각 ABC_IP 등으로 구분한다.
## REDIS     : Redis 에 대한 주소 및 데이터 베이스 SYSDBM:6379/0 
## NLB       : Network LoadBalancer 에 대한 파일 위치 , 기본값은 conf/nlb.json
## DBSET     : DB Config 위치 기본값은 conf/db.json 
## LOGGER    : false,true,true -> consoel,file,remote 에 대한 설정   
## LOG_DIR   : LOG Directory 기본은 root/logs 
## LOG_MAXDAY: 파일로그의 MAX 보관기간 일단위, 기본값은 60일
## LOG_LEVEL : LOG 레벨 설정 , DEBUG, INFO, ALL, TRACE, WARN,ERROR 등 기본값은 DEBUG , 운영환경은 INFO 로 설정하도록.
## LOG_REDIS : 로그관리 Redis 에 대한 주소 및 데이터베이스 , 설정 없으면 상위 REDIS  를 따라 간다.


PROC_NAME="SL_SCHEDULER"


PRG_OPTS="-DPROC=$PROC_NAME \
  -DCONF=../conf \
  -DREDIS=SYSDBM:6379/0 \
  -DNLB=../conf/nlb.json \
  -DDBSET=../conf/db.json \
  -DLOGGER=false,true,true \
  -DLOG_DIR=../logs \
  -DLOG_MAXDAY=60 \
  -DLOG_LEVEL=DEBUG \
  -DLOG_REDIS=SYSDBM:6379/0"

# Setup Classpath  
JVM_CP=../lib/*
for d in $JVM_CP/; do
    JVM_CP=$JVM_CP:$d*
done
JVM_CP=`echo $JVM_CP | cut -c1-`


# Program Main Class
MAIN_CLASS="syslink.message.main.Daemon"


PID=$PROC_NAME.pid

if [ -f "$PID" ]; then
  NEW_PID=`cat < $PID`
  if [ ! -z "$NEW_PID" ]; then
    if [ $NEW_PID -gt 0 ]; then
      if ps -p $NEW_PID > /dev/null; then
        echo "$CY_PROC is still be running with $NEW_PID"
        exit 1
      fi
    fi
  fi
fi


NEW_PID=`ps -ef | grep java | grep $PROC_NAME`
if [ ! -z "$NEW_PID" ]; then
  echo "#Process is already running"
  echo "#$NEW_PID"
  echo "#Check your process!"
  exit 1;
fi



# Run JVM
#echo $JAVA $PRG_OPTS $JVM_OPTS -cp $JVM_CP $MAIN_CLASS
$JAVA $PRG_OPTS $JVM_OPTS -cp $JVM_CP $MAIN_CLASS &
echo $!>$PROC_NAME.pid

if [ "$1" = "log" ] ; then
  tail -f ../logs/root.txt
fi
