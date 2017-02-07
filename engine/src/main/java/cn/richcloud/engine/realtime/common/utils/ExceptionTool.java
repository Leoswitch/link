package cn.richcloud.engine.realtime.common.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ExceptionTool {

	public static void throwException(){
		String ip = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		}catch (UnknownHostException e) {
		}
		throw new RealTimeException(ip);
	}
	
	public static void throwException(String message){
		String ip = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		}catch (UnknownHostException e) {
		}
		throw new RealTimeException("ip:" + ip + message);
	}
	
	public static void throwException(Throwable cause){
		String ip = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		}catch (UnknownHostException e) {
		}
		throw new RealTimeException("ip:" + ip  , cause );
	}
	
	public static void throwException(String message, Throwable cause){
		String ip = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
		}catch (UnknownHostException e) {
		}
		throw new RealTimeException("ip:" + ip + message,cause);
	}
}
