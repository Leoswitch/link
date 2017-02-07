package cn.richcloud.common.zoo;

import org.apache.curator.framework.api.CuratorWatcher;

/**
 * Created by Administrator on 2016/10/25.
 */
public interface JedisZkWatch extends CuratorWatcher {

    public String getPath();

}
