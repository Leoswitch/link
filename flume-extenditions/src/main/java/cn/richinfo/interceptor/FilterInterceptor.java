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

import cn.richcloud.common.zoo.BrasZKWatch;
import cn.richcloud.common.zoo.OnuZKWatch;
import cn.richcloud.common.zoo.ZKUtil;
import cn.richcloud.engine.realtime.busi.BrasanlaysInfoSpec;
import cn.richcloud.engine.realtime.busi.OnuDelinfoSpec;
import cn.richinfo.Constants;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.interceptor.Interceptor;
import org.apache.mina.core.RuntimeIoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FilterInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(FilterInterceptor.class);

    private int randomSeed = Constants.NO_RANDOMSEED;
    private Random random = new Random();
    public Map<String, int[]> fileProp = new HashedMap();
    private static final AtomicLong count = new AtomicLong();
    private static final AtomicLong intercptedCount = new AtomicLong();
    private static final AtomicLong userIntercptedCount = new AtomicLong();
    private static final Long upperLimit = Long.MAX_VALUE - 10000;
    private BrasanlaysInfoSpec brasanlaysInfoSpec = new BrasanlaysInfoSpec();
    private OnuDelinfoSpec onuDelinfoSpec = new OnuDelinfoSpec();
    private int onuFieldsNum = 8;
    private int brasanlaysFieldNum = 4;
    private String fileTypeKey = "";
    private ZKUtil zkUtil;
    private BrasZKWatch brasZKWatch;
    private OnuZKWatch onuZKWatch;

    private FilterInterceptor(Map<String, int[]> fileProp,int randomSeed,
                              String fileTypeKey,String quorumServers,
                              int zookeeperTimeOut) {
        this.fileTypeKey = fileTypeKey;
        this.randomSeed = randomSeed;
        this.fileProp = fileProp;
        try {
            zkUtil = new ZKUtil(quorumServers);

            brasZKWatch = new BrasZKWatch(zkUtil.zkTools, brasanlaysInfoSpec);
            onuZKWatch = new OnuZKWatch(zkUtil.zkTools, onuDelinfoSpec);

            String brasanlaysInfoZNodeValue = zkUtil.get(brasZKWatch);
            brasanlaysInfoSpec.setKey(brasanlaysInfoZNodeValue);
            logger.warn("init brasanlaysInfoSpec key:" + brasanlaysInfoZNodeValue);
            String onuDelinfoZNodeValue = zkUtil.get(onuZKWatch);
            onuDelinfoSpec.setKey(onuDelinfoZNodeValue);
            logger.warn("init onuDelinfoSpec key:" + onuDelinfoZNodeValue);
        } catch (Exception e) {
            throw new RuntimeException("get znode value faile~");
        }


    }

    public void initialize() {
        // no-op
    }

    private boolean validateRecordSize(ArrayList<String> fields, String fileType) {

        boolean result = false;
        int[] ints = fileProp.get(fileType);
        int limitSize = ints[Constants.RECORD_SIZE_INDE];
        if (fileType.equals(cn.richinfo.Constants.videoPrefix)) {
            result = (fields.size() == limitSize);

        } else if (fileType.equals(cn.richinfo.Constants.netPrefix)) {
            result = (fields.size() == limitSize);

        } else if (fileType.equals(cn.richinfo.Constants.dnsPrefix)) {
            result = (fields.size() == limitSize);
        } else {
            logger.error("=-=-=--=-=-=-!!unknown fileType:" + fileType);
        }
        if (!result) {
            logger.debug(fileType + "(==================ignore data for the record does not satisfy the num " + limitSize + " Fields===>" + Joiner.on(",").join(fields) + " " + fields.size());
        }
        return result;
    }


    public Event intercept(Event event) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String body = new String(event.getBody());
        Iterable<String> split = Splitter.on(Constants.COMMA).split(body);
        Long start = System.currentTimeMillis();
        ArrayList<String> fields = Lists.newArrayList(split);
        String fileType = event.getHeaders().get(fileTypeKey);//fileType:3006 3080 3005

        if (StringUtils.isEmpty(fileType)) {
            throw new RuntimeIoException("get fileType from event header is nulll~~~~~");
        }

        try {
            if (!validateRecordSize(fields, fileType)) {
                return null;
            } else {
                int[] thisFileProps = fileProp.get(fileType);

                //// TODO: extract the TimeStampFieldVal with the TIME_INDEX for thisFileProps
                String timestampStr = fields.get(thisFileProps[Constants.TIME_INDEX]);
                Date timestamp = dateFormat.parse(timestampStr);

                if (timestamp != null) {
                    event.getHeaders().put(fileTypeKey, fileType);
                    if (randomSeed != Constants.NO_RANDOMSEED) {
                        event.getHeaders().put("dpi", "dpi" + random.nextInt(randomSeed));
                    }
                    long timestamplong = timestamp.getTime();
                    if (timestamplong <= 0) {
                        logger.error("!!!Timestamp must be positive--" + timestamplong + "  " + timestampStr + "-->" + fileType + ":" + thisFileProps[Constants.TIME_INDEX]);
                        return null;
                    }
                    event.getHeaders().put(Constants.TIMESTAMP, timestamplong + "");
                } else {
                    logger.debug("============>ignore data for timestamp is null:" + body);
                    return null;
                }

                //// TODO: extract the IpFieldVal with the IP_INDEX for thisFileProps
                String ip = fields.get(thisFileProps[Constants.IP_INDEX]);

                Map<String, String> brasanlaysInfo = fetchBrasanlaysInfo(ip);

                if (brasanlaysInfo == null || brasanlaysInfo.isEmpty() || brasanlaysInfo.values() == null || brasanlaysInfo.values().isEmpty()) {
                    logger.debug(">1111111>>ignore data for no brasanlaysInfo to the ip[" + ip + "] bodyMsg:" + body);
                    return null;
                } else {

                    mrakCount(userIntercptedCount, 1, USER_RULEFILTERCOUNT_DEBUG, 1000);
                    String useraccount = brasanlaysInfo.get("useracount");
                    Map<String, String> onuDelinfo = fetchOnuInfo(useraccount);
                    if (onuDelinfo == null || onuDelinfo.isEmpty()
                            || onuDelinfo.values() == null || onuDelinfo.values().isEmpty()) {
                        logger.debug("<2222222<ignore data for no onuDelinfo for the useracount[" + useraccount + "] bodyMsg:" + body);
                        return null;
                    } else {
                        StringBuilder brasanlaysInfoValuesJoin = new StringBuilder();//Joiner.on(COMMA).join(brasanlaysInfo.values());
                        StringBuilder onuDelinfoValuesJoin = new StringBuilder();//Joiner.on(COMMA).join(onuDelinfo.values());
                        for (Object s : brasanlaysInfo.values()) {
                            brasanlaysInfoValuesJoin.append(s).append(Constants.COMMA);
                        }
                        Set<Map.Entry<String, String>> entries = onuDelinfo.entrySet();
                        for (Map.Entry<String, String> en : entries) {
                            String key = en.getKey();
                            if (key.equalsIgnoreCase("useraccount")) {//TODO:exclude useraccount
                                continue;
                            }
                            onuDelinfoValuesJoin.append(en.getValue()).append(Constants.COMMA);
                        }
                        if (onuDelinfo.values().size() != onuFieldsNum) {
                            logger.debug("<<<ignore data for onuDelinfo illegal stored in memory[" + onuDelinfoValuesJoin + "] bodyMsg:" + body);
                            return null;
                        }
                        if (brasanlaysInfo.values().size() != brasanlaysFieldNum) {
                            logger.debug("<<<ignore data for brasanlaysInfo illegal stored in memory[" + onuDelinfoValuesJoin + "] bodyMsg:" + body);
                            return null;
                        }

                        String newbody = body.replaceAll("\\s", " ").trim() + Constants.COMMA + brasanlaysInfoValuesJoin + onuDelinfoValuesJoin.deleteCharAt(onuDelinfoValuesJoin.length() - 1);
                        event = EventBuilder.withBody(newbody.getBytes(), event.getHeaders());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("============>ignore data for parseException:" + body);
            return null;
        }

        return event;
    }


    private Long timestamp = System.currentTimeMillis();
    private AtomicLong printNum = new AtomicLong(0);

    public Map<String, String> fetchOnuInfo(String useraccount) {
        long start = System.currentTimeMillis();
        Map<String, String> onuDelinfo = null;
        try {
            onuDelinfo = (Map<String, String>) onuDelinfoSpec.hgetObject(useraccount);
        } catch (Exception e) {
            UUID id = UUID.randomUUID();
            logger.error(id + ":(~_~)!!" + e.getMessage());
            try {
                onuDelinfoSpec.cleanup();
            } catch (Exception e0) {
                logger.warn(id + ":===========>onuDelinfoSpec returnResource error ...." + e0.getMessage());
            }
            logger.warn(id + ":onuDelinfoSpec returnResource ");
        }

        if (System.currentTimeMillis() - timestamp > 60000) {
            if (printNum.incrementAndGet() < 500) {
                logger.info("===>(" + (System.currentTimeMillis() - start) + ") " + useraccount + ":" + (onuDelinfo != null ? onuDelinfo.size() : "0"));
            } else {
                printNum.set(0);
                timestamp = System.currentTimeMillis();
            }
        }
        logger.debug((System.currentTimeMillis() - start) + "fetchOnuInfo in " + onuDelinfoSpec + " useraccount:" + useraccount + " " + onuDelinfo);
        return onuDelinfo;
    }

    public Map<String, String> fetchBrasanlaysInfo(String ip) {
        long start = System.currentTimeMillis();
        Map<String, String> brasanlaysInfo = null;
        try {
            brasanlaysInfo = (Map<String, String>) (brasanlaysInfoSpec.hgetObject(ip));
        } catch (Exception e) {
            UUID id = UUID.randomUUID();
            logger.error(id + ":(~_~)!!" + e.getMessage());
            try {
                brasanlaysInfoSpec.cleanup();
            } catch (Exception e0) {
                logger.warn(id + ":==========>brasanlaysInfoSpec returnResource error ...." + e0.getMessage());
            }
            logger.warn(id + ":brasanlaysInfoSpec returnResource ");
        }
        logger.debug((System.currentTimeMillis() - start) + "fetchBrasanlaysInfo for ip in " + brasanlaysInfoSpec + ip + " " + brasanlaysInfo);
        return brasanlaysInfo;
    }


    /**
     * Delegates to {@link #intercept(Event)} in a loop.
     *
     * @param events
     * @return
     */
    public List<Event> intercept(List<Event> events) {
        List<Event> ret = Lists.newArrayList();
        Long start = System.currentTimeMillis();
        for (Event event : events) {
            Long start2 = System.currentTimeMillis();
            Event intercept = intercept(event);
            Long end2 = System.currentTimeMillis();
            logger.debug((end2 - start2) + "event<event>event-");
            if (intercept != null) {
                mrakCount(intercptedCount, 1, ALL_RULEFILTERCOUNT_DEBUG, 1000);
                ret.add(intercept);
            }
        }

        Long end = System.currentTimeMillis();
        long l = count.addAndGet(events.size());
        if (l >= upperLimit) {
            count.set(0);
        } else {
            logger.info(String.format(INTERCEPTCOUNT_DEBUG, end - start, l));
        }

        return ret;
    }

    public void close() {
        // no-op
    }


    private static final String USER_RULEFILTERCOUNT_DEBUG = "user rule filterCount:%d";
    private static final String ALL_RULEFILTERCOUNT_DEBUG = "~~all rules filterCount~~:%d";
    private static final String INTERCEPTCOUNT_DEBUG = "%d interceptCount===%d";

    private void mrakCount(AtomicLong count, long size, String debugMsg, int intervalPrintSize) {
        long l = count.addAndGet(size);
        if (l >= upperLimit) {
            count.set(0);
        } else {
            if (l % intervalPrintSize == 0) {
                logger.info(String.format(debugMsg, l));
            }
        }
    }

    /**
     * Builder which builds new instances of the TimestampInterceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private Map<String, int[]> fileProp = new HashedMap();
        private int randomSeed = -1;
        private String fileTypeKey = "";
        private String quorumServers = "";
        private int zookeeperTimeOut = 0;

        public Interceptor build() {
            return new FilterInterceptor(fileProp, randomSeed, fileTypeKey, quorumServers, zookeeperTimeOut);
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
            fileProp.put(cn.richinfo.Constants.dnsPrefix, dnsProp);
            fileProp.put(cn.richinfo.Constants.videoPrefix, videoProp);
            fileProp.put(cn.richinfo.Constants.netPrefix, netProp);

            this.randomSeed = context.getInteger(Constants.RANDOM_SEED, Constants.NO_RANDOMSEED);
            this.fileTypeKey = context.getString(Constants.FILETYPE_KEY, Constants.DEFAULT_FILETYPEKEY);
            this.quorumServers = context.getString(Constants.QUORUMSERVERS);
            this.zookeeperTimeOut = context.getInteger(Constants.ZOOKEEPER_SESSION_TIMEOUT, 0);

        }
    }
}


