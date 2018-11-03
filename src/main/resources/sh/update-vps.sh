#!/bin/bash

pid=$(ps -A |grep "java"| awk '{print $1}')
echo "找到PID:"$pid
kill -9 $pid
echo "杀死进程:"$pid
echo "移动JAR包"
mv /root/jars/xinghuo-0.0.1-SNAPSHOT.jar /root/
echo "执行脚本:/root/system-init.sh"
/root/system-init.sh