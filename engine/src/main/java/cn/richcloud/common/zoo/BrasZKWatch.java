package cn.richcloud.common.zoo;

import cn.richcloud.engine.realtime.busi.BrasanlaysInfoSpec;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import cn.richcloud.engine.realtime.busi.SycKeyService;

import java.nio.charset.Charset;

/**
 * Created by Administrator on 2016/10/25.
 */
public class BrasZKWatch implements JedisZkWatch {
    private CuratorFramework zkTools;
    private BrasanlaysInfoSpec brasanlaysInfoSpec;
    private String path;
    private static Charset charset = Charset.forName("utf-8");

    public BrasZKWatch(CuratorFramework zkTools, BrasanlaysInfoSpec brasanlaysInfoSpec) {
        this.brasanlaysInfoSpec = brasanlaysInfoSpec;
        this.zkTools = zkTools;
        this.path = this.brasanlaysInfoSpec.getZnode();
    }

    public String getPath(){
        return path;
    }

    public  synchronized  void  setSpec(BrasanlaysInfoSpec brasanlaysInfoSpec){
        this.brasanlaysInfoSpec = brasanlaysInfoSpec;
    }
    //        @Override
    public void process(WatchedEvent event) throws Exception {
            if(event.getType() == Watcher.Event.EventType.NodeDataChanged){
                byte[] data = zkTools.
                        getData().
                        usingWatcher(this).forPath(path);
                String key = new String(data,charset);
                synchronized (this){
                    if(this.brasanlaysInfoSpec!=null){
                        SycKeyService.syc(this.brasanlaysInfoSpec.getSycKey());
                        System.out.println(path+":"+key);
                        brasanlaysInfoSpec.setKey(key);
                    }else{
                        System.out.println("BrasZKWatch---->suspended");
                    }
                }

            }
    }

}
