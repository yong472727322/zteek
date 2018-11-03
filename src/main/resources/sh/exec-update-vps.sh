#!/bin/bash
#更新VPS

#157.52.202.18 20198 root  SRw8f3ZwM

starttime=`date +'%Y-%m-%d %H:%M:%S'`

cat /root/update_vps/vps.txt | while read myline
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

spawn echo "拷贝文件xinghuo-0.0.1-SNAPSHOT.jar"
spawn /usr/bin/scp -P $port /root/apk/xinghuo-0.0.1-SNAPSHOT.jar $username@$host:/root/jars/
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "$password\r" }
}
expect "*]#*"

spawn echo "拷贝文件update-vps"
spawn /usr/bin/scp -P $port /root/update_vps/update-vps.sh $username@$host:/root/
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "$password\r" }
}
expect "*]#*"

spawn ssh -p $port $username@$host
expect {
"*yes/no" { send "yes\r"; exp_continue }
"*password:" { send "$password\r" }
}
expect "*]#*"

send "/root/update-vps.sh \r"

expect "*]#*"

send "logout\r"
EOF


done

endtime=`date +'%Y-%m-%d %H:%M:%S'`
start_seconds=$(date --date="$starttime" +%s);
end_seconds=$(date --date="$endtime" +%s);
echo "本次运行时间： "$((end_seconds-start_seconds))"s"


