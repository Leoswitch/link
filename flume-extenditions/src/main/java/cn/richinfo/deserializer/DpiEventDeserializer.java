package cn.richinfo.deserializer;

import cn.richinfo.Constants;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.serialization.EventDeserializer;
import org.apache.flume.serialization.ResettableInputStream;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by root on 7/26/16.
 */
public class DpiEventDeserializer implements EventDeserializer{
    private static final Logger logger = LoggerFactory.getLogger
            (DpiEventDeserializer.class);

    private SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final ResettableInputStream in;
    private final Charset outputCharset;
    private final int maxLineLength;
    private volatile boolean isOpen;

    public static final String OUT_CHARSET_KEY = "outputCharset";
    public static final String CHARSET_DFLT = "UTF-8";

    public static final String MAXLINE_KEY = "maxLineLength";
    public static final int MAXLINE_DFLT = 2048;

    public static final int NO_RANDOMSEED = -1;
    private int timestampIndex = 0;
    private int randomSeed = -1;
    private Random random = new Random();
    private int recordSize = 34;
    private String RECORDSIZE = "recordsize";

    private String baseNameKey = "";
    private final  String BASENAME = "baseNamekey";


    DpiEventDeserializer(Context context, ResettableInputStream in) {
        this.in = in;
        this.outputCharset = Charset.forName(
                context.getString(OUT_CHARSET_KEY, CHARSET_DFLT));
        this.maxLineLength = context.getInteger(MAXLINE_KEY, MAXLINE_DFLT);
        this.timestampIndex = context.getInteger("timestampIndex",0);
        this.randomSeed = context.getInteger("randomSeed",NO_RANDOMSEED);
        this.recordSize = context.getInteger(RECORDSIZE,34);
        this.baseNameKey = context.getString(BASENAME);
        this.isOpen = true;
    }

    /**
     * Reads a line from a file and returns an event
     * @return Event containing parsed line
     * @throws IOException
     */
    public Event readEvent() throws IOException {
        ensureOpen();
        Event event = null;
        String line = readLine();
        if (line == null) {
            return null;
        } else {
            while(event==null&&line!=null){
                event =  EventBuilder.withBody(line, outputCharset);
                Iterable<String> split = Splitter.on(",").split(line);
                ArrayList<String> strings = Lists.newArrayList(split);
                if(strings.size()!=recordSize){
                    logger.debug("(==================the record does not satisfy the num Fields===>"+line+" "+strings.size());
                    event = null;
                }else{

                    try {
                            String baseName = event.getHeaders().get(baseNameKey);
                            if(StringUtils.isNotEmpty(baseName)){
                                if(baseName.startsWith(Constants.netPrefix)){
                                    baseName = Constants.netPrefix;
                                }else if(baseName.startsWith(Constants.videoPrefix)){
                                    baseName = Constants.videoPrefix;
                                }else if(baseName.startsWith(Constants.dnsPrefix)){
                                    baseName = Constants.dnsPrefix;
                                }
                            }

                            event.setHeaders(new HashMap<String, String>());
                            event.getHeaders().put(baseNameKey,baseName);
                            if(randomSeed != NO_RANDOMSEED){
                                event.getHeaders().put("dpi","dpi"+random.nextInt(randomSeed));
                            }




                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.warn("()()()()()()()())()()()()()ignore data()()()"+line);
                        event = null;
                    }
                }

                if(event==null){
                    line = readLine();
                }
            }

            if(line==null){
                return null;
            }
            logger.debug("hhhhhhhhhhhhh>>>>>>>>>>>>>"+JSONValue.toJSONString(event.getHeaders()));
            return event;
        }
    }

    /**
     * Batch line read
     * @param numEvents Maximum number of events to return.
     * @return List of events containing read lines
     * @throws IOException
     */
    public List<Event> readEvents(int numEvents) throws IOException {
        ensureOpen();
        List<Event> events = Lists.newLinkedList();
        for (int i = 0; i < numEvents; i++) {
            Event event = readEvent();
            if (event != null) {
                events.add(event);
            } else {
                break;
            }
        }
        return events;
    }

    public void mark() throws IOException {
        ensureOpen();
        in.mark();
    }

    public void reset() throws IOException {
        ensureOpen();
        in.reset();
    }

    public void close() throws IOException {
        if (isOpen) {
            reset();
            in.close();
            isOpen = false;
        }
    }

    private void ensureOpen() {
        if (!isOpen) {
            throw new IllegalStateException("Serializer has been closed");
        }
    }

    // TODO: consider not returning a final character that is a high surrogate
    // when truncating
    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        int readChars = 0;
        while ((c = in.readChar()) != -1) {
            readChars++;
            if (c == '\n') {
                if (readChars>2 && sb.charAt(readChars-2)== '\r') {
                    sb.setLength(readChars-2);
                }
                break;
            }

            sb.append((char)c);
            if (readChars >= maxLineLength) {
                logger.warn("Line length exceeds max ({}), truncating line!",
                        maxLineLength);
                break;
            }
        }

        if (readChars > 0) {
            return sb.toString();
        } else {
            return null;
        }
    }

    public static class Builder implements EventDeserializer.Builder {

        public EventDeserializer build(Context context, ResettableInputStream in) {
            return new DpiEventDeserializer(context, in);
        }

    }
}
