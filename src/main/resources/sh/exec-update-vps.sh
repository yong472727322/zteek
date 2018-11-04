#!/bin/bash
#更新VPS

#157.52.202.18 20198 root  SRw8f3ZwM

starttime=`date +'%Y-%m-%d %H:%M:%S'`

cat /root/deploy/vps.txt | while read myline
do
if [ -z "$myline" ]
then
continue
fi
OLD_IFS="$IFS"
IFS=" "
arr=($myline)
IFS="$OLD_IFS"

host=${arr[0]}
port=${arr[1]}
username=${arr[2]}
password=${arr[3]}

echo "连接VPS"$host"-"$port

expect << EOF

set timeout 3600

spawn echo "执行SSH登录"
spawn ssh -p $port root@$host
expect {
	"*yes/no" { send "yes\r"; exp_continue }
	"*password:" { send "$password\r" }
}
expect "*]#*"
send "mkdir jars\r"
expect "*]#*"
send "logout\r"
expect "*]#*"
sleep 3

spawn echo "拷贝文件xinghuo-0.0.1-SNAPSHOT.jar"
spawn /usr/bin/scp -P $port /root/deploy/xinghuo-0.0.1-SNAPSHOT.jar $username@$host:/root/jars/
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "$password\r" }
}
expect "*]#*"

spawn echo "执行SSH登录2"
spawn ssh -p $port root@$host
expect {
	"*yes/no" { send "yes\r"; exp_continue }
	"*password:" { send "$password\r" }
}
expect "*]#*"


send "pid=$(ps -A |grep "java"| awk '{print $1}')\r"
expect "*]#*"

spawn echo "找到PID:"$pid
expect "*]#*"
spawn echo "杀死进程:"$pid
expect "*]#*"
send "kill -9 $pid \r"
expect "*]#*"
spawn echo "移动JAR包"
expect "*]#*"
send "mv /root/jars/xinghuo-0.0.1-SNAPSHOT.jar /root/ \r"
expect "*]#*"
spawn echo "执行脚本:/root/system-init.sh"
send "/root/system-init.sh \r"


#send "/root/update-vps.sh \r"

expect "*]#*"

send "logout\r"
EOF


done

endtime=`date +'%Y-%m-%d %H:%M:%S'`
start_seconds=$(date --date="$starttime" +%s);
end_seconds=$(date --date="$endtime" +%s);
echo "本次运行时间： "$((end_seconds-start_seconds))"s"


