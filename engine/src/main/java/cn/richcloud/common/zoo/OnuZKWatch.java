package cn.richcloud.common.zoo;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import cn.richcloud.engine.realtime.busi.OnuDelinfoSpec;
import cn.richcloud.engine.realtime.busi.SycKeyService;

import java.nio.charset.Charset;

/**
 * Created by Administrator on 2016/10/25.
 */
public class OnuZKWatch implements JedisZkWatch {
//    private final String path;

    private CuratorFramework zkTools;
    private OnuDelinfoSpec onuDelinfoSpec;
    private String path;
    private static Charset charset = Charset.forName("utf-8");

    public OnuZKWatch(CuratorFramework zkTools, OnuDelinfoSpec onuDelinfoSpec) {
        this.onuDelinfoSpec = onuDelinfoSpec;
        this.zkTools = zkTools;
        this.path = this.onuDelinfoSpec.getZnode();
    }

    public String getPath(){
        return path;
    }

    public synchronized void setSpec(OnuDelinfoSpec onuDelinfoSpec){
        this.onuDelinfoSpec = onuDelinfoSpec;
    }
    //        @Override
    public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                byte[] data = zkTools.
                        getData().
                        usingWatcher(this).forPath(path);
                String key = new String(data, charset);
                synchronized(this){
                    if(this.onuDelinfoSpec!=null) {
                        SycKeyService.syc(this.onuDelinfoSpec.getSycKey());
                        System.out.println(path + ":" + key);
                        onuDelinfoSpec.setKey(key);
                    }else{
                        System.out.println("OnuZKWatch---->suspended");
                    }
                }

            }

    }

}
