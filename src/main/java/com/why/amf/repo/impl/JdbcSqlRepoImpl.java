package com.why.amf.repo.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.why.amf.bo.pay.PayOrder;
import com.why.amf.repo.JdbcSqlRepo;
import com.why.amf.util.DateUtil;
import com.why.amf.util.TimeUtil;

@Repository("jdbcSqlRepo")
public class JdbcSqlRepoImpl implements JdbcSqlRepo {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Override
    public PayOrder findOrderByBillno(String billno) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());

        try {
            PayOrder order = (PayOrder) jdbcTemplate.queryForObject("select * from " + date
                    + "_pay_orders where billno=?", new Object[] { billno }, new PayOrderRowMapper(date));
            return order;

        } catch (Exception e) {
            //System.out.println();
            return null;
        }
    }

    @Override
    public PayOrder findOrderById(int id) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());

        try {
            PayOrder order = (PayOrder) jdbcTemplate.queryForObject("select * from " + date + "_pay_orders where id=?",
                    new Object[] { id }, new PayOrderRowMapper(date));
            return order;

        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PayOrder> findOrdersByDate(int datetime) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());

        try {
            List<PayOrder> orders = (List<PayOrder>) jdbcTemplate.query("select * from " + date
                    + "_pay_orders where date=?", new Object[] { datetime }, new PayOrderRowMapper(date));
            return orders;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PayOrder> findOrdersByOpenId(String openId) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());

        try {
            List<PayOrder> orders = (List<PayOrder>) jdbcTemplate.query("select * from " + date
                    + "_pay_orders where openid=?", new Object[] { openId }, new PayOrderRowMapper(date));
            return orders;

        } catch (Exception e) {
            return null;
        }
    }
    
    private static class PayOrderRowMapper implements RowMapper{
    	private final String date;
    	public PayOrderRowMapper(String date) {
			this.date = date;
		}
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            PayOrder order = new PayOrder();
            order.setId(rs.getInt("id"));
            order.setDate(rs.getInt("date"));
            order.setTime(rs.getTimestamp("time"));
            order.setOpenid(rs.getString("openid"));
            order.setStatus(rs.getInt("status"));
            order.setExchangeAmount(rs.getInt("exchange_amount"));
            order.setVip(rs.getBoolean("is_vip"));
            order.setItemId(rs.getString("item_id"));
            order.setItemNum(rs.getInt("item_num"));
            order.setBillno(rs.getString("billno"));
            order.setRet(rs.getInt("ret"));
            order.setQzone(rs.getBoolean("is_qzone"));
            order.setServerId(rs.getInt("server_id"));
            order.setPubacct_payamt_coins(rs.getString("pubacct_payamt_coins"));
            if(PayOrder.isNeedSrcAmount(date)){
            	order.setSrcAmount(rs.getInt("src_amount"));
            }
            // add some other attribute here
            return order;
        }
    }

    @Override
    public void insertOrder(final PayOrder payOrder) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());
        PayOrderPreparedStatementSetter setter = new PayOrderPreparedStatementSetter(payOrder, date);
        jdbcTemplate.update(setter.sql(), setter);
    }
    
    private static class PayOrderPreparedStatementSetter implements PreparedStatementSetter{
    	private final PayOrder payOrder;
    	private final String date;
    	public PayOrderPreparedStatementSetter(PayOrder payOrder, String date) {
			super();
			this.payOrder = payOrder;
			this.date = date;
		}
    	public String sql(){
    		if(PayOrder.isNeedSrcAmount(date)){
    			return "insert into " + date + "_pay_orders(date,time, openid, status, exchange_amount, is_vip, item_id,"
            		 + "item_num, billno, ret, is_qzone, item_xml_id, server_id, pubacct_payamt_coins, src_amount) " 
            		 + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    		}else{
    			return "insert into " + date + "_pay_orders(date,time, openid, status, exchange_amount, is_vip, item_id,"
                	 + "item_num, billno, ret, is_qzone, item_xml_id, server_id, pubacct_payamt_coins) " 
                	 + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    		}
    	}
		public void setValues(PreparedStatement ps) throws SQLException {
            ps.setInt(1, payOrder.getDate());
            ps.setTimestamp(2, payOrder.getTime());
            ps.setString(3, payOrder.getOpenid());
            ps.setInt(4, payOrder.getStatus());
            ps.setInt(5, payOrder.getExchangeAmount());
            ps.setBoolean(6, payOrder.isVip());
            ps.setString(7, payOrder.getItemId());
            ps.setInt(8, payOrder.getItemNum());
            ps.setString(9, payOrder.getBillno());
            ps.setInt(10, payOrder.getRet());
            ps.setBoolean(11, payOrder.isQzone());
            ps.setInt(12, payOrder.getItemXmlId());
            ps.setInt(13, payOrder.getServerId());
            ps.setString(14, payOrder.getPubacct_payamt_coins());
            if(PayOrder.isNeedSrcAmount(date)){
            	ps.setInt(15, payOrder.getSrcAmount());
            }
        }
    }

    @Deprecated
    @Override
    public void updateOrder(final PayOrder payOrder) {
    	String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());

        jdbcTemplate.update("update " + date + "_pay_orders set date =?,time=?,openid=?,status=?,exchange_amount=?,"
                + "is_vip=?,item_id=?,item_num=?,billno=?,ret=?,is_qzone=?,item_xml_id=? ,pubacct_payamt_coins=? where id = ?",
                new PreparedStatementSetter() {
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, payOrder.getDate());
                        ps.setTimestamp(2, payOrder.getTime());
                        ps.setString(3, payOrder.getOpenid());
                        ps.setInt(4, payOrder.getStatus());
                        ps.setInt(5, payOrder.getExchangeAmount());
                        ps.setBoolean(6, payOrder.isVip());
                        ps.setString(7, payOrder.getItemId());
                        ps.setInt(8, payOrder.getItemNum());
                        ps.setString(9, payOrder.getBillno());
                        ps.setInt(10, payOrder.getRet());
                        ps.setBoolean(11, payOrder.isQzone());
                        ps.setInt(12, payOrder.getItemXmlId());
                        ps.setInt(13, payOrder.getId());
                        ps.setString(14, payOrder.getPubacct_payamt_coins());
                    }
                });
    }

    @Override
    public void initOrderTables() {

        for (int i = 0; i < 36; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, +i);
            String date = DateUtil.convertToDate5(TimeUtil.currentTimeMillis());
            try {
                jdbcTemplate.execute("CREATE TABLE `" + date + "_pay_orders` "
                        + "(`id` int(11) NOT NULL auto_increment," 
                		+ "`date` int(11) NOT NULL,"
                        + "`time` datetime NOT NULL," 
                		+ "`openid` varchar(50) NOT NULL," 
                        + "`status` int(11) NOT NULL,"
                        + "`exchange_amount` int(11) NOT NULL," 
                        + "`is_vip` tinyint(1) NOT NULL,"
                        + "`item_id` varchar(50) NOT NULL," 
                        + "`item_num` int(11) NOT NULL,"
                        + "`billno` varchar(100) NOT NULL," 
                        + "`ret` int(11) NOT NULL,"
                        + "`server_id` int(11) NOT NULL DEFAULT 0,"
                        + "`pubacct_payamt_coins` varchar(20),"
                        + "`is_qzone` tinyint(1) NOT NULL," 
                        + "`item_xml_id` int(11)," 
                        + "PRIMARY KEY  (`id`),"
                        + "KEY `date` (`date`)," 
                        + "KEY `openid and status` (`openid`,`status`),"
                        + "KEY `date and status` (`date`,`status`)"
                        + ") ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;");
            } catch (BadSqlGrammarException e) {
                System.out.println("Table '" + date + "_pay_orders' already exists");
            }
        }
    }

}
