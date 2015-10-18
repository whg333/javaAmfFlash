package com.why.amf.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component("dateUtil")
public class DateUtil implements Timer {
	
	private static final String DATE_FORMAT_DAY="yyyy-MM-dd";
	
	private static final String DATE_FORMAT_SECONDS= "yyyy-MM-dd HH:mm:ss";

    private static long time_difference = TimeUnit.HOURS.toMillis(5);
    
    private static long actual_time_disfference = TimeUnit.HOURS.toMillis(8);

    private static Date open_yellow_date;
    
    private static Date open_signIn_date;
    
    private static Date open_yuanZheng_date;

    /**
     *获取黄钻开放的时间
     */
    public static Date getOpenYellowDate() {
        if (open_yellow_date == null) {
            try {
                open_yellow_date = DateUtils.parse("2011-03-22",DATE_FORMAT_DAY);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return open_yellow_date;
    }
    
    /**
     *获取签到系统开放时间
     */
    public static Date getOpenSignInDate() {
        if (open_signIn_date == null) {
            try {
                open_signIn_date = DateUtils.parse("2012-04-17",DATE_FORMAT_DAY);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return open_signIn_date;
    }
    
    /**
     *获取远征之路开放的时间
     */
    public static Date getOpenYuanZhengDate() {
        if (open_yuanZheng_date == null) {
            try {
            	open_yuanZheng_date = DateUtils.parse("2011-11-11",DATE_FORMAT_DAY);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return open_yuanZheng_date;
    }
    
    public static int findActualMaxMonthDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(TimeUtil.currentTimeMillis());
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    
    /**
     * 判断时间是否合法
     * @param time
     * @return
     */
    public static boolean isValid(long time){
    	return time>0?true:false;
    }
    
    /**
     * 按照MM-dd HH:mm格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate(long time) {
        return DateUtils.format(time, "MM-dd HH:mm");
    }

    /**
     * 按照yyyy-MM-dd HH:mm:ss格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate2(long time) {
        return DateUtils.format(time, DATE_FORMAT_SECONDS);
    }
    
    public static long covertToDate3(String time){
        try {
            return DateUtils.parse(time, DATE_FORMAT_SECONDS).getTime();
        } catch (ParseException e) {
            throw new RuntimeException("covertToDate3 exception ex:"+e.getMessage());
        }
    }

    /**
     * 按照yyyyMMdd HH:mm:ss格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate3(long time) {
        return DateUtils.format(time, "yyyyMMdd HH:mm:ss");
    }

    /**
     * 按照yyyyMMdd格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate4(long time) {
        return DateUtils.format(time, "yyyyMMdd");
    }
    
    /**
     * 按照yyyy-MM-dd格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate6(long time) {
        return DateUtils.format(time, "yyyy-MM-dd");
    }

    /**
     * 按照yyyyMM格式化long类型时间
     * 
     * @param time
     * @return
     */
    public static String convertToDate5(long time) {
        return DateUtils.format(time, "yyyyMM");
    }

    /**
     * 返回当前距1970年1月1日的天数(3点为界)
     * 
     * @return
     */
    public static int getTodayIntValue() {
        return (int) TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijing());
    }
    
    /**
     * 返回当前距1970年1月1日的天数(0点为界)
     * 
     * @return
     */
    public static int getActualTodayIntValue() {
        return (int) TimeUnit.MILLISECONDS.toDays(TimeUtil.currentTimeMillis() + actual_time_disfference);
    }
    
    /**
     * 返回nDaysAtferToday距1970年1月1日的天数(0点为界)
     * 
     * @return
     */
    public static int getBeiJingDayIntValue(int nDaysAfterToday) {
        return (int) (TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijingActual()) - nDaysAfterToday);
    }
    
    /**
     * 返回昨天距1970年1月1日的天数
     * 
     * @return
     */
    public static int getYestodayIntValue() {
        return (int) (TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijing()) - 1);
    }
    
    public static int getTomorrowIntValue() {
        return (int) (TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijing()) + 1);
    }

    /**
     * 获取当前时间距1970年1月1日0时的小时数
     * 
     * @return
     */
    public static int getHourIntValue() {
        return (int) TimeUnit.MILLISECONDS.toHours(currentTimeMillisAtBeijing());
    }

    /**
     * 当前时间加上5小时的时差（3点为界）
     * 
     * @return
     */
    private static long currentTimeMillisAtBeijing() {
        return TimeUtil.currentTimeMillis() + time_difference;
    }
    
    /**
     * 当前时间加上8小时的时差（0点为界）
     * 
     * @return
     */
    private static long currentTimeMillisAtBeijingActual() {
        return TimeUtil.currentTimeMillis() + actual_time_disfference;
    }

    public static int timeMillisToDays(long millis) {
        return (int) TimeUnit.MILLISECONDS.toDays(millis + time_difference);
    }
    
    /**
     * 返回nDaysAtferToday距1970年1月1日的天数，默认时差为5小时，故会以凌晨3点为界
     * <br/>可用getBeiJingDayIntValue以0点为界代替
     * 例子：getDayIntValue(-1)代表明天
     * @return
     */
    public static int getDayIntValue(int nDaysAfterToday) {
        return (int) (TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijing()) - nDaysAfterToday);
    }

    /**
     * 返回当前距1970年1月4日的周数
     * 
     * @return
     */
    public static int getWeekIntValue() {
        return (int) (TimeUnit.MILLISECONDS.toDays(currentTimeMillisAtBeijing())+4)/7;
    }
    @Override
    public long currentTimeMillis() {
        return TimeUtil.currentTimeMillis();
    }
    
    /**
     * 获取当前天是一周的第几天
     * @return
     */
    public static int getWeekDayIntValue(){
    	long now=TimeUtil.currentTimeMillis();
    	Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(now);
		int weekDay=cal.get(Calendar.DAY_OF_WEEK);
		return weekDay;
    }
    
    public static long nowBetweenNextDayMills(){
    	long now=TimeUtil.currentTimeMillis();
    	Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(now); 
		calEnd.set(Calendar.HOUR_OF_DAY, 23) ; 
		calEnd.set(Calendar.MINUTE, 59); 
		calEnd.set(Calendar.SECOND, 59);
		calEnd.set(Calendar.MILLISECOND, 999); 
		return calEnd.getTimeInMillis()-now;
    }
    
    /** 下周一的时间，凌晨3点为界 */
    public static long nextMondayMillis(){
		long now = TimeUtil.currentTimeMillis();
		int currentHour = hour(now);
		
		int dayOfWeek = DateUtil.getWeekDayIntValue()-1;
		boolean isMonday = dayOfWeek == 1;
		int nextMondayInterval = (isMonday && currentHour >= 3) ? -7 : (dayOfWeek-7-1)%7;
		//int dayIntValue = nextMondayInterval == 0?DateUtil.getActualTodayIntValue():DateUtil.getDayIntValue(nextMondayInterval);
		long nextMonday = TimeUnit.DAYS.toMillis(getBeiJingDayIntValue(nextMondayInterval));
//		System.out.println(DateUtil.convertToDate2(nextMonday));
		
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(nextMonday); 
		calEnd.set(Calendar.HOUR_OF_DAY, 3);
		return calEnd.getTimeInMillis();
	}
	
	public static int hour(long timeMillis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeMillis);
		int nowHour = cal.get(Calendar.HOUR_OF_DAY);
		return nowHour;
	}
    
    public static void main(String[] args) throws Exception{
//		System.out.println(getWeekIntValue());
		System.out.println(getWeekDayIntValue()-1);
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss");
//    	long threshold = sdf.parse("2012-04-05:00-00-00").getTime();
//    	System.out.println(threshold);
//    	System.out.println(DateUtil.covertToDate3("2012-04-05 00:00:00"));
//    	
//    	System.out.println(TimeUtil.currentTimeMillis());
//    	System.out.println(nowBetweenNextDayMills());
	}

}
