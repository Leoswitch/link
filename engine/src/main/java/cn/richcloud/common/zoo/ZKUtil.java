package cn.richcloud.common.zoo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Administrator on 2016/10/25.
 */
public class ZKUtil {

    public CuratorFramework zkTools;
    private ConcurrentSkipListSet watchers = new ConcurrentSkipListSet();
    private static Charset charset = Charset.forName("utf-8");

    public ZKUtil(String quorum) {
        zkTools = CuratorFrameworkFactory
                .builder()
                .connectString(quorum)
                .retryPolicy(new RetryNTimes(2000,20000))
                .build();
        zkTools.start();

    }

    public void addReconnectionWatcher(final String path,final ZookeeperWatcherType watcherType,final CuratorWatcher watcher){
        synchronized (this) {
            if(!watchers.contains(watcher.toString()))//不要添加重复的监听事件
            {
                watchers.add(watcher.toString());
                System.out.println("add new watcher " + watcher);
                zkTools.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                    //                @Override
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        System.out.println(newState);
                        if(newState == ConnectionState.RECONNECTED){//处理session过期
                            try{
                                if(watcherType == ZookeeperWatcherType.EXITS){
                                    zkTools.checkExists().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.GET_CHILDREN){
                                    zkTools.getChildren().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.GET_DATA){
                                    zkTools.getData().usingWatcher(watcher).forPath(path);
                                }else if(watcherType == ZookeeperWatcherType.CREATE_ON_NO_EXITS){
                                    //ephemeral类型的节点session过期了，需要重新创建节点，并且注册监听事件，之后监听事件中，
                                    //会处理create事件，将路径值恢复到先前状态
                                    Stat stat = zkTools.checkExists().usingWatcher(watcher).forPath(path);
                                    if(stat == null){
                                        System.err.println("to create");
                                        zkTools.create()
                                                .creatingParentsIfNeeded()
                                                .withMode(CreateMode.EPHEMERAL)
                                                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                                                .forPath(path);
                                    }
                                }
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    public void create(String path) throws Exception{

        zkTools.create()//创建一个路径
                .creatingParentsIfNeeded()//如果指定的节点的父节点不存在，递归创建父节点
                .withMode(CreateMode.PERSISTENT)//存储类型（临时的还是持久的）
                .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)//访问权限
                .forPath(path);//创建的路径
    }

    public boolean isExist(String path){
        boolean ret = false;
        Stat stat = null;
        try {
            stat = zkTools.checkExists().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(stat!=null){
            ret = true;
        }
        return ret;
    }

    public void put(String path,String value) throws Exception{
        zkTools.//对路径节点赋值
                setData().
                forPath(path,value.getBytes(Charset.forName("utf-8")));
    }

    public String get(String path){
        byte[] buffer  = new byte[0];
        try {
            buffer = zkTools.
                    getData().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String data = new String(buffer,charset);
        System.out.println(data);
        return data;
    }

    public  String get(JedisZkWatch watch) throws Exception{
        byte[] buffer  = zkTools.
                    getData().
                    usingWatcher(watch).forPath(watch.getPath());
        addReconnectionWatcher(watch.getPath(), ZookeeperWatcherType.GET_DATA, watch);
        String data = new String(buffer,charset);
        System.out.println(data);
        //添加session过期的监控

        return data;
    }

    public enum ZookeeperWatcherType{
        GET_DATA,GET_CHILDREN,EXITS,CREATE_ON_NO_EXITS
    }



    public static void main(String[] args) throws Exception {
        ZKUtil zkUtil = new ZKUtil("192.168.28.86,192.168.28.87,192.168.28.88:2181");
        zkUtil.put("/onu","helloleo");

        System.out.println(zkUtil.isExist("/onu"));
        zkUtil.get("/onu");
        /*BrasanlaysInfoSpec braspec = new BrasanlaysInfoSpec();
        BrasZKWatch brasZKWatch = new BrasZKWatch(zkUtil.zkTools,braspec);

        OnuDelinfoSpec onuSpec = new OnuDelinfoSpec();
        OnuZKWatch onuZKWatch = new OnuZKWatch(zkUtil.zkTools,onuSpec);
        String brasInitKey = zkUtil.get(brasZKWatch);
        String OnuInitKey = zkUtil.get(onuZKWatch);
        onuSpec.setKey(OnuInitKey);
        braspec.setKey(brasInitKey);
        System.out.println("initKey:"+brasInitKey+" "+OnuInitKey);
        Scanner s = new Scanner(System.in);
        while(true){
            String cm = s.nextLine();
            if(cm.equals("exit")){
                break;
            }else if(cm.equals("pk")){
                System.out.println(braspec.getKey()+"  "+onuSpec.getKey());
            }else if(cm.equals("rgo")){
                try{
                    onuSpec.cleanup();
                }catch (Exception e){
                    e.printStackTrace();

                }

                String preKey =  onuSpec.getKey();
                onuSpec =new OnuDelinfoSpec();
                onuSpec.setKey(preKey);
                onuZKWatch.setSpec(onuSpec);
            }else if(cm.equals("rgb")){
                try{
                    braspec.cleanup();
                }catch (Exception e){
                    e.printStackTrace();

                }
                String preKey =  braspec.getKey();
                braspec =new BrasanlaysInfoSpec();
                braspec.setKey(preKey);
                brasZKWatch.setSpec(braspec);
            }

        }

//        test.register();
        Thread.sleep(10000000000L);*/

    }
}
