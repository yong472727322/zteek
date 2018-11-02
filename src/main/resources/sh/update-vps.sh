#!/bin/bash

echo "---->开始一键更新VPS"


set timeout 3600


set host "172.247.116.251"
set port 20171
set password "wufeng"

spawn echo ---->开始部署VPS[$host-$port]
spawn echo "拷贝jar包到VPS["$host"-$port]"
spawn /usr/bin/scp -P $port /root/apk/xinghuo-0.0.1-SNAPSHOT.jar root@$host:/root/jars/

expect "*password:*"
send $password"\r"

expect "100%"
sleep 1
expect "*#"

spawn echo "拷贝完成，执行SSH登录"
spawn ssh -p $port root@$host
expect "*password:*"
send $password"\r"
expect "*]#*"

spawn echo "开始执行VPS[$host-$port]中的脚本[/root/deploy-vps.sh]"
send "/root/deploy-vps.sh \r"
expect "*]#*"

send "logout\r"
spawn echo date"---->完成部署VPS[$host-$port]"
spawn echo ""
sleep 2


echo "---->完成一键更新VPS"


