package com.why.amf.repo;

import java.util.List;

import com.why.amf.bo.pay.PayOrder;

public interface JdbcSqlRepo {

    /**
     * 通过id查询具体的订单
     * 
     * @param id
     * @return
     */
    PayOrder findOrderById(int id);

    /**
     * 通过billno查询具体的订单
     * 
     * @param billno
     * @return
     */
    PayOrder findOrderByBillno(String billno);

    /**
     * 通过openid查询特定用户的订单
     * 
     * @param openid
     * @return
     */
    List<PayOrder> findOrdersByOpenId(String openId);

    /**
     * 通过date查询某一天的订单
     * 
     * @param date
     * @return
     */
    List<PayOrder> findOrdersByDate(int date);

    /**
     * 插入订单
     * 
     * @param payOrder
     * @return
     */
    void insertOrder(PayOrder payOrder);

    /**
     * 更新订单 根据订单id或者billno 唯一性索引
     * 
     * @param payOrder
     * @return
     */
    void updateOrder(PayOrder payOrder);

    /**
     * 初始化两年的order tables按月分表
     * 
     * @return
     */
    void initOrderTables();

}
