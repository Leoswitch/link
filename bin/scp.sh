#scp /opt/engine-0.0.1.jar zery38:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/engine-0.0.1.jar zery39:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/engine-0.0.1.jar zery40:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/engine-0.0.1.jar zery41:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery38:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery39:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery40:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
#scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery41:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
f34=0
f38=0
killproc=0
startproc=0
copyfile=0
COMMAND=""
for i in $@; do
COMMAND=$i
case $COMMAND in
# usage flags
  --help|-help|-h)

     exit
    ;;
   kp)
    killproc=1
   ;;
   sp)
    startproc=1
   ;;
   cf)
     copyfile=1
   ;;
   f34)
     f34=1
   ;;
   f38)
     f38=1
   ;;
esac
done
if [ $f34 -eq 1 ];then
echo "====>ssh zery34-37...."
if [ $copyfile -eq 1 ];then
echo "copyfile zery34-37.."
scp /opt/engine-0.0.1.jar zery34:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery35:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery36:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery37:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery34:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery35:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery36:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery37:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
fi

if [ $killproc -eq 1 ];then
echo "killproc zery34-37.."
ssh zery34 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery34 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery35 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery35 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery36 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery36 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery37 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery37 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
fi

if [ $startproc -eq 1 ];then
echo "startproc zery34-37.."
ssh zery34 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery34 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery35 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery35 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery36 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery36 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery37 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery37 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"
fi
fi

if [ $f38 -eq 1 ];then
echo "ssh zery38-41..."
if [ $copyfile -eq 1 ];then
echo "copyfile zery38-41.."
scp /opt/engine-0.0.1.jar zery38:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery39:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery40:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/engine-0.0.1.jar zery41:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery38:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery39:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery40:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
scp /opt/flumextendtion-1.0-SNAPSHOT.jar zery41:/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/
fi

if [ $killproc -eq 1 ];then
echo "killproc zery38-41.."
ssh zery38 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery38 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery39 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery39 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery40 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery40 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery41 "ps -ef|grep kafkAgent2|grep -v grep |awk '{print \$2}'| xargs kill -9"
ssh zery41 "ps -ef|grep kafkAgent1|grep -v grep |awk '{print \$2}'| xargs kill -9"
fi

if [ $startproc -eq 1 ];then
echo "startproc zery38-41.."
ssh zery38 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery38 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery39 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery39 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery40 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery40 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"

ssh zery41 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent1 -n kafkAgent1 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume.properties -Dflume.root.logger=warn,console >/data1/flume1.log 2>&1 &"
ssh zery41 "nohup /opt/apache-flume-1.6.0-bin/bin/flume-ng agent -Xmx4048m -Xms2048m -Dquorum_port=2181 -Dquorum=zery34,zery35,zery36 -Dagent=kafkAgent2 -n kafkAgent2 -c /opt/apache-flume-1.6
.0-bin/conf -f /opt/apache-flume-1.6.0-bin/conf/flume2.properties -Dflume.root.logger=warn,console >/data1/flume2.log 2>&1 &"
fi
fi
