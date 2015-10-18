package com.why.amf.bo.auth;

import java.util.concurrent.TimeUnit;

import com.why.amf.util.StringUtil;
import com.why.amf.util.TimeUtil;

/**
 * 
 * @author luzj
 *
 */
public class AuthValidation {

    public static String AUTH_KEY = "authSecret";
    public static String IDENTIFYING_CODE_KEY = "idf_c_key";
    
    private static final String _ = "_";

    private long userId;

    private long currentTime;

    private String auth;

    private long genearatedTime;

    public AuthValidation(long userId) {
        this.userId = userId;
        currentTime = (int) TimeUnit.MILLISECONDS.toHours(TimeUtil.currentTimeMillis());
    }

    public AuthValidation(long userId, String auth) {
        this(userId);

        int index = auth.lastIndexOf(_);
        genearatedTime = Long.parseLong(auth.substring(index + 1));

        this.auth = auth.substring(0, index);
    }

    private static final String prefix = "hoolai!@#$%^";

    private static final String endfix = "!@$%^huaibeijing";

    private String generateAuth(long time) {
    	String generateAuth = StringUtil.encryptToMd5(prefix + userId + _ + time + endfix).substring(8, 24);
    	//System.out.println("generateAuth:"+generateAuth);
        return generateAuth;
    }

    public String currentAuth() {
    	String currentAuth = generateAuth(currentTime) + _ + currentTime;
    	//System.out.println("currentAuth:"+currentAuth);
        return currentAuth;
    }

    public boolean isAuthValid() {
        if (this.genearatedTime < currentTime - 12)
            return false;

        //System.out.println("auth:"+auth);
        return generateAuth(genearatedTime).equals(this.auth);
    }

    public void checkAuth() {
        if (!isAuthValid()) {
            throw new RuntimeException("secret validate fail");
        }
    }

}
