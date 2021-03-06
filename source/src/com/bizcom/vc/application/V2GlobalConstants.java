package com.bizcom.vc.application;

public class V2GlobalConstants {
	
	/**
	 * Global request type for conference
	 */
	public static final int REQUEST_TYPE_CONF = 1;
	
	/**
	 * Global request type for IM
	 */
	public static final int REQUEST_TYPE_IM = 2;
	
	
	/**
	 * User state for on line
	 */
	public static final int USER_STATUS_ONLINE = 1;

	/**
	 * User state for leaved
	 */
	public static final int USER_STATUS_LEAVING = 2;

	/**
	 * User state for busy
	 */
	public static final int USER_STATUS_BUSY = 3;

	/**
	 * User state for do not disturb
	 */
	public static final int USER_STATUS_DO_NOT_DISTURB = 4;
	
	/**
	 * User state for hidden
	 */
	public static final int USER_STATUS_HIDDEN = 5;

	/**
	 * User state for off line
	 */
	public static final int USER_STATUS_OFFLINE = 0;
	
	
	/**
	 * error conference code for user deleted conference  
	 */
	public static final int CONF_CODE_DELETED = 204;
	
	
	
	/**
	 * Indicate send on line file
	 */
	public static final int FILE_TYPE_ONLINE = 1;
	
	/**
	 * Indicate send off line file
	 */
	public static final int FILE_TYPE_OFFLINE = 2;
	
	public static final int RECORD_TYPE_START = 0x0001;
	public static final int RECORD_TYPE_STOP = 0x0002;

	/**
	 * groupType 的分类
	 */
	public static final int GROUP_TYPE_USER = 0;
	public static final int GROUP_TYPE_DEPARTMENT = 1;
	public static final int GROUP_TYPE_CONTACT = 2;
	public static final int GROUP_TYPE_CROWD = 3;
	public static final int GROUP_TYPE_CONFERENCE = 4;
	public static final int GROUP_TYPE_DISCUSSION = 5;

	/**
	 * 文件传输的flag
	 */
	public static final int FILE_TRANS_SENDING = 10;
	public static final int FILE_TRANS_DOWNLOADING = 11;
	public static final int FILE_TRANS_ERROR = 13;
	
	public static final int REQUEST_CONVERSATION_TEXT_RETURN = 14;
}
