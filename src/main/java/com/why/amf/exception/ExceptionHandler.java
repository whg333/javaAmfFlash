package com.why.amf.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.why.amf.util.NetworkUtils;

public class ExceptionHandler implements MethodInterceptor {

	public static final int STATUS_ERROR = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int SYSTEM_ERROR = 4000;
	
    private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

    public static boolean isStatistical = false;

    public static boolean detectMaliciousUsers = false;

    public static ConcurrentMap<String, AtomicLong> perInterfaceUsedTime = new ConcurrentHashMap<String, AtomicLong>();// 执行时间
    public static ConcurrentMap<String, AtomicLong> perInterfaceUsedMTime = new ConcurrentHashMap<String, AtomicLong>();// 执行时间
    public static ConcurrentMap<String, AtomicLong> perInterfaceReqCount = new ConcurrentHashMap<String, AtomicLong>();// 执行次数
    
    public static List<StatisRecord> currentStatisRecords(){
    	List<StatisRecord> statisRecords = new ArrayList<StatisRecord>();
    	if(perInterfaceReqCount.isEmpty()){
    		return Collections.emptyList();
    	}
    	for(String serviceMethodName:perInterfaceReqCount.keySet()){
    		String[] name = serviceMethodName.split("\\.");
    		long reqCount = perInterfaceReqCount.get(serviceMethodName).longValue();
    		double usedTime = perInterfaceUsedMTime.get(serviceMethodName).longValue();
    		double avgUsedTime = reqCount <= 0.0 ? 0 : usedTime/reqCount; //avoid divided by 0.0 generate NaN
    		String ip = NetworkUtils.parseServerIp();
    		StatisRecord statisRecord = new StatisRecord(name[0], name[1], reqCount, usedTime, avgUsedTime, ip);
    		statisRecords.add(statisRecord);
    	}
    	clearStatis();
    	return statisRecords;
    }
    
    public static class StatisRecord {
		private final String serviceName;
		private final String methodName;
		private final long reqCount;
		private final double usedTime; 		//单位是毫秒
		private final double avgUsedTime;	//单位是毫秒
		private final String serverIp;
		public StatisRecord(String serviceName, String methodName, long reqCount, double usedTime, double avgUsedTime, String serverIp) {
			this.serviceName = serviceName;
			this.methodName = methodName;
			this.reqCount = reqCount;
			this.usedTime = usedTime;
			this.avgUsedTime = avgUsedTime;
			this.serverIp = serverIp;
		}
		public String getServiceName() {
			return serviceName;
		}
		public String getMethodName() {
			return methodName;
		}
		public long getReqCount() {
			return reqCount;
		}
		public double getUsedTime() {
			return usedTime;
		}
		public double getAvgUsedTime() {
			return avgUsedTime;
		}
		public String getServerIp() {
			return serverIp;
		}
		public String sqlValues(){
			return "('"+serviceName+"','"+methodName+"',"+reqCount+","+usedTime+","+avgUsedTime+",'"+serverIp+"',now())";
		}
	}
    
    private static void clearStatis(){
    	perInterfaceReqCount.clear();
    	perInterfaceUsedTime.clear();
    	perInterfaceUsedMTime.clear();
    }

    private void addStatis(String serviceMethodName, long ntime, long mtime) {
        //if(mtime > 1000000) return;
        
        AtomicLong count = perInterfaceReqCount.get(serviceMethodName);
        AtomicLong nUsedTime = perInterfaceUsedTime.get(serviceMethodName);
        AtomicLong mUsedTime = perInterfaceUsedMTime.get(serviceMethodName);

        if (count == null) {
            count = new AtomicLong(0);
            perInterfaceReqCount.putIfAbsent(serviceMethodName, count);
            nUsedTime = new AtomicLong(0);
            perInterfaceUsedTime.putIfAbsent(serviceMethodName, nUsedTime);
            mUsedTime = new AtomicLong(0);
            perInterfaceUsedMTime.putIfAbsent(serviceMethodName, mUsedTime);
        }

        count.incrementAndGet();
        nUsedTime.addAndGet(ntime);
        mUsedTime.addAndGet(mtime);
    }

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String serviceMethodName = null;
        long start = 0;
        long ms = 0;
        if (isStatistical) {
            ms = System.currentTimeMillis();
            start = System.nanoTime();
        }
        try {
            serviceMethodName = serviceMethodName(invocation);
            Map<String, Object> result = (Map<String, Object>) invocation.proceed();
            result.put("status", STATUS_SUCCESS);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("errorcode", 4000);
            result.put("status", STATUS_ERROR);
            return result;
        } 
    }
    
    private String serviceMethodName(MethodInvocation invocation){
    	String methodName = invocation.getMethod().getName();
        Object service = invocation.getThis();
        String serviceName = service.toString();
        serviceName = serviceName.substring(serviceName.lastIndexOf(".")+1, serviceName.indexOf("@"));
        return serviceName+"."+methodName;
    }

}
