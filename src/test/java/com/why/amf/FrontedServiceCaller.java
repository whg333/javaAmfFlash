package com.why.amf;

import com.why.amf.bo.auth.AuthValidation;

import flex.messaging.io.amf.client.AMFConnection;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;

/**
 * 此类的目的是为了在缺少前端或者前端没写完的情况下来模拟前端调用
 */
public class FrontedServiceCaller {
	
	/** 请求接口URL，即配置的tomcat请求地址+/messagebroker/amf */
	private static final String URL = "http://localhost:7070/amf/messagebroker/amf/";
	
	/** 请求玩家的userId */
	private static final long userId = 10042;
	
	public static void main(String[] args) throws ClientStatusException, ServerStatusException {
		AMFConnection connection = skipValidateConnection();
		Object result = null;
		try {
			result = connection.call("UserService.findPayOrder", "10042");
//			result = connection.call("OffLineUnionBattleService.dismissUnionCountry", userId+"", 3, "");
		} finally {
			connection.close();
		}
		if(result != null){
			System.out.println(result);
		}
	}
	
	/**
	 * 跳过验证的AMF协议连接
	 * <br/>验证包括：游戏Auth验证和操作ID验证，但目前操作ID验证不起作用
	 * @return
	 * @throws ClientStatusException 
	 * @throws ClientStatusException
	 */
	private static final AMFConnection skipValidateConnection() throws ClientStatusException {
		AMFConnection connection = new AMFConnection();
		connection.connect(URL);
		connection.addAmfHeader(AuthValidation.AUTH_KEY, true, new AuthValidation(userId).currentAuth());
		connection.addAmfHeader(AuthValidation.IDENTIFYING_CODE_KEY, true, "");
		return connection;
	}
	
}
