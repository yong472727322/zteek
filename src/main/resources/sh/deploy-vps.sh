#!/bin/bash

#部署新VPS 使用方法 /root/deploy/deploy-vps.sh 104.223.145.90 20016 root 09fb88dd
if [ $# != 4 ];then
echo "必须4个参数：host,port,username,password"
exit 1;
fi

starttime=`date +'%Y-%m-%d %H:%M:%S'`

host=$1
port=$2
username=$3
password=$4

#echo "本机拨号，防止网络问题。"
#pppoe-stop
#sleep 2
#pppoe-start
#sleep 15

#1、创建VPS上的system-init.sh文件

echo "创建system-init.sh"
touch /root/system-init.sh
echo "添加执行权限"
chmod u+x /root/system-init.sh

echo "开始写入内容"
echo "#!/bin/bash" > /root/system-init.sh
echo "#系统初始化" >> /root/system-init.sh
echo "echo '启动shadowsocks'" >> /root/system-init.sh
echo "ssserver -c /root/shadowsocks.json -d start" >> /root/system-init.sh
echo "echo '启动jar包'" >> /root/system-init.sh
echo "nohup  /java/jdk/jdk1.8.0_181/bin/java -jar /root/xinghuo-0.0.1-SNAPSHOT.jar  --vps.ip=$host-$port   > /root/log.file 2>&1 &" >> /root/system-init.sh
echo "完成写入内容"


#2、创建VPS上的 init-vps.sh

echo "创建[init-vps.sh]"
touch /root/init-vps.sh
echo "[init-vps.sh]添加执行权限"
chmod u+x /root/init-vps.sh

echo "开始写入内容到[/root/init-vps.sh]"
echo "#!/bin/bash" > /root/init-vps.sh
echo "#VPS初始化" >> /root/init-vps.sh
echo "echo '开始拨号'" >> /root/init-vps.sh
echo "pppoe-stop" >> /root/init-vps.sh
echo "sleep 2" >> /root/init-vps.sh
echo "pppoe-start" >> /root/init-vps.sh
echo "sleep 15" >> /root/init-vps.sh
echo "echo '设置时区'" >> /root/init-vps.sh
echo "date" >> /root/init-vps.sh
echo "ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime" >> /root/init-vps.sh
echo "date" >> /root/init-vps.sh
echo "echo '安装 ss'" >> /root/init-vps.sh
echo "yum -y install epel-release" >> /root/init-vps.sh
echo "sleep 2" >> /root/init-vps.sh
echo "yum -y install python-pip" >> /root/init-vps.sh
echo "sleep 2" >> /root/init-vps.sh
echo "pip install shadowsocks" >> /root/init-vps.sh
echo "sleep 2" >> /root/init-vps.sh
echo "echo '修改[/etc/rc.d/rc.local]文件'" >> /root/init-vps.sh
echo "echo '/root/system-init.sh' >> /etc/rc.d/rc.local" >> /root/init-vps.sh
echo "echo '修改[/etc/rc.d/rc.local]文件的权限'" >> /root/init-vps.sh
echo "chmod u+x /etc/rc.d/rc.local" >> /root/init-vps.sh
echo "完成写入内容到[init-vps.sh]"





echo "---->开始初始化VPS"$host"-"$port



expect << EOF


set timeout 3600

spawn echo "拷贝文件1"
spawn /usr/bin/scp -P $port /etc/yum.repos.d/CentOS-Base.repo $username@$host:/etc/yum.repos.d/
expect {
	"*yes/no" { send "yes\r"; exp_continue }
	"*password:" { send "$password\r" }
}
sleep 1
expect "*#"

spawn echo "拷贝文件到VPS"
spawn /usr/bin/scp -P $port -r /root $username@$host:/
expect {
	"*yes/no" { send "yes\r"; exp_continue }
	"*password:" { send "$password\r" }
}
sleep 1
expect "*#"

spawn echo "拷贝完成，执行SSH登录"
spawn ssh -p $port root@$host
expect {
	"*yes/no" { send "yes\r"; exp_continue }
	"*password:" { send "$password\r" }
}

expect "*]#*"

send "echo '解压JDK'\r"
expect "*]#*"
send "cd /\r"
expect "*]#*"
send "mkdir java\r"
expect "*]#*"
send "cd java\r"
expect "*]#*"
send "mkdir jdk\r"
expect "*]#*"
send "cd\r"
expect "*]#*"
send "tar -zxvf /root/jdk-8u181-linux-x64.tar.gz -C /java/jdk\r"
expect "*]#*"
send "echo '开始初始化VPS'\r"
expect "*]#*"
send "/root/init-vps.sh \r"
expect "*]#*"
send "echo '完成初始化VPS'\r"
expect "*]#*"
send "echo '开始启动VPS'\r"
expect "*]#*"
send "/root/system-init.sh \r"
expect "*]#*"
send "echo '完成启动VPS'\r"
expect "*]#*"

send "echo '开放端口1819、4431'\r"
expect "*]#*"
send "/root/add-open-port.sh 1819 \r"
expect "*]#*"
send "/root/add-open-port.sh 4431 \r"
expect "*]#*"
send "logout\r"

EOF

echo "部署VPS$host-$port完成"

endtime=`date +'%Y-%m-%d %H:%M:%S'`
start_seconds=$(date --date="$starttime" +%s);
end_seconds=$(date --date="$endtime" +%s);
echo "本次运行时间： "$((end_seconds-start_seconds))"s"