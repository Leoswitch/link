#folder=$1
#filetop=$2

filename=$(ssh 117.187.14.126 "cd /;find /home/ftp01/Radius_Log/Radius_Uat/ -name *txt -cmin -5|head -1")

#filename=$(find /zcbuff/$folder -name $filetop*dat -cmin -2 | head -1)
j=1
init=1;
argus="";
ackNum=16
while [ $j -eq 1 ]
   do
     echo $filename
     #cp $filename /zcbuff/data_test/$filename.tmp
     #mv /zcbuff/data_test/$filename.tmp /zcbuff/data_test/$filename
     old_filename=$filename
     scp 117.187.14.126:$filename /home/redius_uat/data/
     #.....
     basename=${filename##*/}
#echo $basename
     if [ $init -eq 1 ];then
        argus="quorumServers:zery34,zery35,zery36:2181 interval:60000 zNode:/bras zValue:hello zookeeperTimeOut:10000 filePath:/home/redius_uat/data/${basename} ackNum:${ackNum} resetSycAck:t
rue"
     else
        argus="quorumServers:zery34,zery35,zery36:2181 interval:60000 zNode:/bras zValue:hello zookeeperTimeOut:10000 filePath:/home/redius_uat/data/${basename} ackNum:${ackNum}"
     fi

     echo ${init}${argus}
     init=$(($init+1))

    java -classpath \
/opt/apache-flume-1.6.0-bin/conf:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/engine-0.0.1.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/commons-lang-2.6.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/commons-logging-1.1.1.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/commons-logging-api-1.1.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/jedis-2.8.1.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/json-20090211.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/mysql-connector-java-5.1.26.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/json-simple-1.1.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/guava-18.0.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/commons-pool2-2.4.2.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/log4j-1.2.16.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/slf4j-api-1.7.10.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/slf4j-log4j12-1.7.10.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/curator-client-2.9.0.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/curator-framework-2.9.0.jar:\
/opt/apache-flume-1.6.0-bin/plugins.d/kafkAgent/lib/zookeeper-3.4.6.2.3.4.0-3485.jar \
richcloud.engine.AnalysisExecutor tool_cmd:LoadTool load_cmd:brasAnlaysInfoFileLoader ${argus}
     i=1
     while [ $i -eq 1 ]
          do
             filename=$(ssh 117.187.14.126 "cd /;find /home/ftp01/Radius_Log/Radius_Uat/ -name *txt -newer $old_filename| head -1")
             if [ ! -n "$filename" ];then
                i=1
                sleep 5
                echo "sleep"
             else
                i=0
             fi

            # echo $filename

          done
   done
