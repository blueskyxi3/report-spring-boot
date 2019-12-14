#!/bin/sh
proccess_string=`ps -ef | grep report-spring-boot | grep -v "grep" | awk '{print $2}'`
echo $proccess_string
if [ ! -n "$proccess_string" ]; then
  echo "IS NULL"
  nohup java -jar  report-spring-boot-0.0.1-SNAPSHOT.jar >out.log 2>&1 &
else
  echo "NOT NULL"
  ps -ef|grep report-spring-boot|grep -v "grep"|awk '{print $2}'|xargs kill -9
  sleep 5s
  nohup java -jar  report-spring-boot-0.0.1-SNAPSHOT.jar >out.log 2>&1 &
fi
id
ps -ef|grep report-spring-boot|grep -v "grep"|awk '{print $2}'