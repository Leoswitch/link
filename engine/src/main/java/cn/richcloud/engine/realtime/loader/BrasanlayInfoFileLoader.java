package cn.richcloud.engine.realtime.loader;

import cn.richcloud.common.tool.AnalysisOptions;
import cn.richcloud.engine.realtime.timer.BrasanlayInfoFileImportTimer;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONValue;
import redis.clients.jedis.Pipeline;
import cn.richcloud.common.DAOUtils;
import cn.richcloud.common.zoo.ZKUtil;
import cn.richcloud.engine.realtime.busi.BrasanlaysInfoSpec;
import cn.richcloud.engine.realtime.common.utils.RealResult;
import cn.richcloud.engine.realtime.timer.FileImportTimer;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


public class BrasanlayInfoFileLoader extends Loader{
	private static AtomicLong count = new AtomicLong();
	private static final String spliterRegex = "\\|";
	private int syncUpper = 1000;
	private long consumCount = 0;
	protected Pipeline pipeLine  = null;

	int flag = 1;
	String zNode;
	String zVal ;
	String thisRoundValue;
	String preRoundValue;
	private ZKUtil zkUtil ;
	String filePath;

	@Override
	public RealResult load(AnalysisOptions options) {
		System.out.println("------=======load=======------");
		String ackNum =options.getProperties().get("ackNum");
		String resetSycAck = options.getProperties().get("resetSycAck");
		String quorumServers = options.getProperties().get("quorumServers");
		String zookeeperTimeOut = options.getProperties().get("zookeeperTimeOut");
		int timeout = 0;
		if(StringUtils.isNotEmpty(zookeeperTimeOut)){
			timeout = Integer.parseInt(zookeeperTimeOut);
		}
		int interval = Integer.parseInt(options.getProperties().get("interval"));
		zVal = options.getProperties().get("zValue");
		zNode = options.getProperties().get("zNode");
		zkUtil = new ZKUtil(quorumServers);

		try {
			if(zkUtil.isExist(zNode)){
				//TODO:get zNode value for the nexRoundValue
				String zvaluenow = zkUtil.get(zNode);
				flag = Integer.parseInt(zvaluenow.substring(zVal.length(),zvaluenow.length()));
				preRoundValue = zVal+flag;
				thisRoundValue = rollValue();
				System.out.println("step1:zNodeValue=="+zvaluenow+" and thisRoundValue  "+thisRoundValue);

			}else{
				thisRoundValue = zVal+flag;
				zkUtil.create(zNode);
				zkUtil.put(zNode,thisRoundValue);
				System.out.println("step1:init --- thisRoundValue  "+thisRoundValue);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}



		ArrayList<String> fieldNames = Lists.newArrayList("useracount", "id", "ipaddress", "brasdeviceid");

		filePath = options.getProperties().get("filePath");

		final BrasanlaysInfoSpec spec = new BrasanlaysInfoSpec();
		if(spec.isLoading()){
			System.out.println("   preLoadTask is loading......");
			return new RealResult(RealResult.SUCESS, "");
		}else{
			String sycAck = spec.getJedis().get(spec.getSycKey());
			if(Integer.valueOf(ackNum).compareTo(Integer.valueOf(sycAck))>0
					&&!"true".equalsIgnoreCase(resetSycAck)){
				int tryNum = 3;
				boolean failover = false;
				while(tryNum>0&&!failover){
					tryNum--;
					System.out.println(tryNum+"   sycAck is not completed......"+sycAck+" "+ackNum);
					try {
						spec.getJedis().decrBy(spec.getSycKey(),Long.parseLong(sycAck));//
						try {
							System.out.println("  ===>znode value rechange:"+preRoundValue);
							zkUtil.put(zNode,preRoundValue);
						} catch (Exception e) {
							System.out.println("  ===>znode value reset error:"+e.getMessage());
							throw new RuntimeException(e);
						}
						Thread.currentThread().sleep(200L);

						sycAck = spec.getJedis().get(spec.getSycKey());
						failover = Integer.valueOf(ackNum).compareTo(Integer.valueOf(sycAck))<=0;

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(failover){
					System.out.println("   ===>failover true, "+sycAck+" and now reset");
					spec.getJedis().decrBy(spec.getSycKey(),Long.parseLong(sycAck));
				}else{
					return new RealResult(RealResult.SUCESS, "failover fail");
				}
			}else{
				System.out.println("  sycAck is "+sycAck+" and now reset");
				spec.getJedis().decrBy(spec.getSycKey(),Long.parseLong(sycAck));
			}

		}

		final BrasanlayInfoFileImportTimer fileImportTimer = new BrasanlayInfoFileImportTimer(filePath,fieldNames,spliterRegex,new Date(),interval);
		fileImportTimer.setDbOptor(new JedisImportLifeCycle<BrasanlaysInfoSpec>( ) {

			Long start  = System.currentTimeMillis();
			public void importBefore() {
				try{
					start  = System.currentTimeMillis();
					this.setJedisSpec(spec);
					this.getJedisSpec().setKey(thisRoundValue);
					this.getJedisSpec().hdelAll();
					this.getJedisSpec().setLoading();
					pipeLine = this.getJedisSpec().getJedis().pipelined();
					System.out.println("step2-1:("+this.getJedisSpec()+")  setKey hdelAll markLoading..beforeImport");
				}catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}

			}

			public void importCompete() {
				if(this.getJedisSpec()==null){
					System.out.println("nulnulnulnul");
				}else{
					pipeLine.sync();
					this.getJedisSpec().setUnLoading();
					this.getJedisSpec().cleanup();
					this.setJedisSpec(null);
				}

				File file = new File(filePath);
				File filecomplete = new File(filePath+".complete");
				file.renameTo(filecomplete);

				//TODO:update zNode data;
				try {
					zkUtil.put(zNode,thisRoundValue);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e.getMessage());
				}

				System.out.println("step2-2:completeImport("+(System.currentTimeMillis()-start)+"~"+consumCount+ ") "+ DAOUtils.getCurrentDateTime()+" setUnloading setzNode("+zNode+")="+thisRoundValue);
				consumCount=0;
			}

			public void imports(Map<String, Object> h) {
				if (this.getJedisSpec() == null) {
					throw new IllegalArgumentException("the jedisSpec must be get~");
				}
				BrasanlaysInfoSpec spec = this.getJedisSpec();
				String key = spec.getObjectKey();
				String ipaddress = h.get("ipaddress").toString();
				pipeLine.hset(key,ipaddress, JSONValue.toJSONString(h));
				consumCount++;
				if(consumCount%syncUpper==0){
					pipeLine.sync();
				}
			}
		});
		fileImportTimer.singleLoad();

		return new RealResult(RealResult.SUCESS, "");
	}

	private String rollValue(){
		flag = flag^1;
		return zVal+flag;
	}
}
