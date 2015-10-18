package com.why.amf.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.why.amf.repo.JdbcSqlRepo;
import com.why.amf.service.UserService;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private JdbcSqlRepo jdbcSqlRepo;
	
	@Override
	public Map<String, Object> findPayOrder(String userIdStr) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("test", "test");
		return result;
	}

}
