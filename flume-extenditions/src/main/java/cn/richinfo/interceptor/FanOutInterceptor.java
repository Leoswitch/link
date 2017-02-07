/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.richinfo.interceptor;

import cn.richinfo.Constants;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.apache.mina.core.RuntimeIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class FanOutInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger
            (FanOutInterceptor.class);


    private int randomSeed = Constants.NO_RANDOMSEED;
    private Random random = new Random();

    public Map<String,int[]> fileProp = new HashedMap();

    private String fileTypeKey = "";

    private FanOutInterceptor(Map<String,int[]> fileProp,
                              int randomSeed,
                              String fileTypeKey) {
        this.fileTypeKey = fileTypeKey;
        this.randomSeed = randomSeed;
        this.fileProp = fileProp;
    }

    public void initialize() {
        // no-op
    }

    private boolean validateRecordSize(ArrayList<String> fields,String fileType){

        boolean result = false;
        int[] ints = fileProp.get(fileType);
        int limitSize = ints[Constants.RECORD_SIZE_INDE];
        if(fileType.equals(cn.richinfo.Constants.videoPrefix)){
            result = (fields.size() == limitSize);

        }else if(fileType.equals(cn.richinfo.Constants.netPrefix)){
            result = (fields.size() == limitSize);

        }else if(fileType.equals(cn.richinfo.Constants.dnsPrefix)){
            result = (fields.size() == limitSize);
        }else{
            logger.error("=-=-=--=-=-=-!!unknown fileType:"+fileType);
        }
        if(!result){
            logger.debug(fileType+"(==================ignore data for the record does not satisfy the num "+limitSize+" Fields===>" + Joiner.on(",").join(fields) + " " + fields.size());
        }
        return result;
    }


    public Event intercept(Event event) {
        String body = new String(event.getBody());
        Iterable<String> split = Splitter.on(Constants.COMMA).split(body);
        ArrayList<String> fields = Lists.newArrayList(split);
        String fileType = event.getHeaders().get(fileTypeKey);

        if(StringUtils.isEmpty(fileType)){
          throw new RuntimeIoException("get fileType from event header is empty~~~~~");
        }

        try {
            fileType = fileType.substring(0,4);//fileType:3006* 3080* 3005*
            if (!validateRecordSize(fields,fileType)) {
                return null;
            } else {
                event.getHeaders().put(fileTypeKey,fileType);
                if (randomSeed != Constants.NO_RANDOMSEED) {
                    event.getHeaders().put("dpi", "dpi" + random.nextInt(randomSeed));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.debug("============>ignore data for Exception:" + body);
            return null;
        }

        return event;
    }

    /**
     * Delegates to {@link #intercept(Event)} in a loop.
     * @param events
     * @return
     */
    public List<Event> intercept(List<Event> events) {
        List<Event> ret = Lists.newArrayList();
        for (Event event : events) {
            Event intercept = intercept(event);
            if (intercept!= null) {
                ret.add(intercept);
            }
        }
        return ret;
    }

    public void close() {
        // no-op
    }

    /**
     * Builder which builds new instances of the TimestampInterceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private Map<String,int[]> fileProp = new HashedMap();
        private int randomSeed = -1;
        private String fileTypeKey = "";

        public Interceptor build() {
            return new FanOutInterceptor(fileProp, randomSeed, fileTypeKey);
        }

        public void configure(Context context) {
            int[] videoProp = new int[]{context.getInteger(Constants.VIDEO_RECORDSIZE, 33),
                    context.getInteger(Constants.VIDEO_IP_POSITION, 1),
                    context.getInteger(Constants.VIDEO_TIMESTAMP_INDEX, 0)};
            int[] netProp = new int[]{context.getInteger(Constants.NET_RECORDSIZE, 33),
                    context.getInteger(Constants.NET_IP_POSITION, 1),
                    context.getInteger(Constants.NET_TIMESTAMP_INDEX, 0)};
            int[] dnsProp = new int[]{context.getInteger(Constants.DNS_RECORDSIZE, 33),
                    context.getInteger(Constants.DNS_IP_POSITION, 1),
                    context.getInteger(Constants.DNS_TIMESTAMP_INDEX, 0)};
            fileProp = new HashedMap();
            fileProp.put(cn.richinfo.Constants.videoPrefix,videoProp);
            fileProp.put(cn.richinfo.Constants.netPrefix,netProp);
            fileProp.put(cn.richinfo.Constants.dnsPrefix,dnsProp);

            this.randomSeed = context.getInteger(Constants.RANDOM_SEED, Constants.NO_RANDOMSEED);
            this.fileTypeKey = context.getString(Constants.FILETYPE_KEY,Constants.DEFAULT_FILETYPEKEY);

        }

    }

}


