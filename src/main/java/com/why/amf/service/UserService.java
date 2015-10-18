package com.why.amf.service;

import java.util.Map;

public interface UserService {

	Map<String, Object> findPayOrder(String userIdStr);
	
}
