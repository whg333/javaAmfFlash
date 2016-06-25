package com.why.amf.util;

/**
 *	令牌生成器
 */
public class TokenProducer {
    private static final String produceTokenAttachStr = "             sango@whg333.com          "; 
    
    private static final String worldBattleProduceTokenAttachStr = "           worldbattle.sango@whg333.com          "; 
    
    private static final String worldBattleProduceTokenAttachStrV2 = "           worldbattleV2.sango@whg333.com          "; 
    
    private static final String weixinProduceTokenAttachStr = "           weixin.sango@whg333.com          "; 
    
    public static String produceToken(String userIdStr) {
        return StringUtil.encryptToMd5(userIdStr + produceTokenAttachStr);
    }
    
    public static String produceWorldBattleToken(String userIdStr) {
        return StringUtil.encryptToMd5(userIdStr + worldBattleProduceTokenAttachStr);
    }
    
    public static String produceWorldBattleTokenV2(String userIdStr) {
        return StringUtil.encryptToMd5(userIdStr + worldBattleProduceTokenAttachStrV2);
    }
    
    public static String produceGetUserInfoToken(String openid) {
        return StringUtil.encryptToMd5(openid + produceTokenAttachStr);
    }
    
    public static String produceLoginIdentifyingCodeToken(String userIdStr) {
        return StringUtil.encryptToMd5(userIdStr + produceTokenAttachStr + "whg333&*^*huai~@12"+TimeUtil.currentTimeMillis());
    }
    
    public static String produceWeiXinToken(){
    	return StringUtil.encryptToMd5(weixinProduceTokenAttachStr);
    }
    
    public static void main(String[] args) {
		System.out.println(produceWeiXinToken());
	}
}
