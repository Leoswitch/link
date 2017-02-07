package cn.richcloud.engine.realtime.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RealTimeException extends RuntimeException {

	public RealTimeException() {
		super( );
		
	
	}

	public RealTimeException(String message) {
		super(message);
	
	}
	
	 public RealTimeException(String message, Throwable cause) {
	        super(message, cause);
	    }
}
