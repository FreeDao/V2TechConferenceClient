package com.bizcom.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.v2tech.R;

import android.text.format.DateUtils;

import com.v2tech.R;

public class DateUtil {

	/**
	 * get specific format String date <br>
	 * <ul>
	 * today HH:mm:ss (时:分:秒)<br>
	 * yesterday : yesterday (昨天) <br>
	 * more before : yyyy-MM-dd HH:mm:ss <br>
	 * </ul>
	 * 
	 * @param longDate
	 * @return
	 */
	public static String getStringDate(long longDate) {
		SimpleDateFormat format = null;
		if (DateUtils.isToday(longDate))
			return getShortDate(longDate);

		Date dates = new Date(longDate);
		Calendar cale = Calendar.getInstance();
		cale.setTime(dates);
		Calendar currentCale = Calendar.getInstance();
		int days = cale.get(Calendar.DAY_OF_MONTH);
		int currentCaleDays = currentCale.get(Calendar.DAY_OF_MONTH);
		if (currentCaleDays - 1 == days) {
			// need get context
			return FileUitls.context.getResources().getString(
					R.string.common_date_yesterday)
					+ getShortDate(longDate);
		}

		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(longDate);
	}

	/**
	 * get the time format , like HH:mm:ss
	 * 
	 * @param mTimeLine
	 * @return
	 */
	public static String getShortDate(long mTimeLine) {

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date(mTimeLine);
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * get standard date time , like 2014-09-01 14:20:22
	 * 
	 * @return
	 */
	public static String getStandardDate(Date date) {

		if (date == null)
			throw new RuntimeException("Given date object is null...");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date.getTime());
	}

	/**
	 * get the time format , like HH:mm:ss , VoiceMessageDetailActivity use
	 * 
	 * @param times
	 * @return
	 */
	public static String calculateTime(long times) {

		times = times / 1000;
		int hour = (int) times / 3600;
		int minute = (int) (times - (hour * 3600)) / 60;
		int second = (int) times - (hour * 3600 + minute * 60);
		if (minute <= 0 && hour <= 0) {
			return (second < 10 ? "0" + second : second) + "秒";
		} else if (hour <= 0) {
			return (minute < 10 ? "0" + minute : minute) + "分"
					+ (second < 10 ? "0" + second : second) + "秒";
		} else {
			if(minute <= 0 && second > 0){
				return (hour < 10 ? "0" + hour : hour) + "时"
						+ (second < 10 ? "0" + second : second) + "秒";
			} else if(second <= 0 && minute > 0){
				return (hour < 10 ? "0" + hour : hour) + "时"
						+ (minute < 10 ? "0" + minute : minute) + "分";
			} else if(second <= 0 && minute <= 0){
				return (hour < 10 ? "0" + hour : hour) + "时";
			} else {
				return (hour < 10 ? "0" + hour : hour) + "时"
						+ (minute < 10 ? "0" + minute : minute) + "分"
						+ (second < 10 ? "0" + second : second) + "秒";
			}
		}
	}
}
