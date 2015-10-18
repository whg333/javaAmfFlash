package com.why.amf.bo.pay;

import java.sql.Timestamp;

import com.why.amf.util.TimeUtil;

public class PayOrder {

    public static final int ORDER_PREPAY = 6; // 预付支付

    public static final int ORDER_CONFIRM = 7; // 确认支付

    public static final int ORDER_CANCEL = 8; // 冲正(取消)支付
    
    private int id;

    private int date;

    private Timestamp time;

    private String openid;

    private int status;

    /** Q点*10 （打折以后的价钱）*/
    private int exchangeAmount;
    
    /** Q点（打折前的价钱）*/
    private int srcAmount;

    private boolean isVip;

    /**
     * 格式： 物品id*物品个数|物品id*物品个数|物品id*物品个数。 如果物品个数为1，就不用*个数了，直接写物品id
     */
    private String itemId;

    private int itemNum;

    /** 交易流水账号 */
    private String billno;

    private int ret;

    private boolean isQzone;

    private int itemXmlId;
    
    private int serverId;
    
    private int provideType;
    
    private String pubacct_payamt_coins;
    
    public int getItemXmlId() {
        return itemXmlId;
    }

    public void setItemXmlId(int itemXmlId) {
        this.itemXmlId = itemXmlId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getProvideType() {
        return provideType;
    }

    public void setProvideType(int provideType) {
        this.provideType = provideType;
    }

    public String getPubacct_payamt_coins() {
        return pubacct_payamt_coins;
    }
    
    public boolean isHasPubacct_payamt_coins(){
    	return pubacct_payamt_coins != null && pubacct_payamt_coins.trim().length() > 0;
    }

    public void setPubacct_payamt_coins(String pubacctPayamtCoins) {
        pubacct_payamt_coins = pubacctPayamtCoins;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getExchangeAmount() {
        return exchangeAmount;
    }

    public void setExchangeAmount(int exchangeAmount) {
        this.exchangeAmount = exchangeAmount;
    }
    
    public static boolean isNeedSrcAmount(String dateStr){
    	int date = Integer.parseInt(dateStr);
    	return date > 201312; //从2014年01月开始添加的src_amount字段
    }
    
    public static boolean isNeedSecCurrTable(String dateStr){
    	int date = Integer.parseInt(dateStr);
    	return date > 201410; //从2014年11月开始查询紫晶钻表计算消费
    }
    
    public int getSrcAmount() {
		return srcAmount;
	}

	public void setSrcAmount(int srcAmount) {
		this.srcAmount = srcAmount;
	}

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean isVip) {
        this.isVip = isVip;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public String getBillno() {
        return billno;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    public boolean isQzone() {
        return isQzone;
    }

    public void setQzone(boolean isQzone) {
        this.isQzone = isQzone;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public void setIsVip(boolean isVip) {
        this.isVip = isVip;
    }
    
    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    /**
     * 生成一个新的订单
     * 
     * @param billno
     * @param exchangeAmount
     * @param itemXmlId
     * @param num
     * @param payItemStr
     * @param platformId
     * @param isVip
     */
    public PayOrder(String billno, int exchangeAmount, int itemXmlId, int num, String payItemStr, 
            String platformId, boolean isVip, int provideType,String pubacct_payamt_coins, int srcAmount) {
        this();
        this.date = 20141027;
        this.billno = billno;
        this.exchangeAmount = exchangeAmount;
        this.itemXmlId = itemXmlId;
        this.itemId = payItemStr;
        this.itemNum = num;
        this.openid = platformId;
        this.setQzone(true);
        this.isVip = isVip;
        this.status = PayOrder.ORDER_PREPAY;
        this.ret = 0; // 有订单号 ret一定是0
        this.time = new Timestamp(TimeUtil.currentTimeMillis());
        this.provideType = provideType;
        this.pubacct_payamt_coins = pubacct_payamt_coins;
        this.srcAmount = srcAmount;
    }
    
	public PayOrder() {
        this.serverId = 0;
    }

    /**
     * 更新ret
     * 
     * @param payRet
     */
    public void updateRet(int payRet) {
        this.time = new Timestamp(TimeUtil.currentTimeMillis());
        this.ret = payRet;
    }

    /**
     * 更改订单状态
     * 
     * @param status
     */
    public void updateStatus(int status) {
        this.status = status;
    }

    public boolean isFinished() {
        return this.status == ORDER_CONFIRM;
    }

	public boolean needRecordHistory() {
		return this.provideType == 0 || this.provideType == 4;//支付(0)记录, 赠送(1)开通黄钻不记录, (4)代表商城
	}
    
}
