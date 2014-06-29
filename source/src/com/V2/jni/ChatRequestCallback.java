package com.V2.jni;

import com.v2tech.util.V2Log;


public interface ChatRequestCallback {

	
	/**
	 * <ul>Use to receive message from server side.</ul>
	 * 
	 * @param nGroupID
	 * @param nBusinessType
	 * @param nFromUserID
	 * @param nTime
	 * @param szXmlText
	 */
	public void OnRecvChatTextCallback(long nGroupID, int nBusinessType,
			long nFromUserID, long nTime, String szXmlText);

	
	/**
	 * <ul>Receive image data from server side.</ul>
	 * @param nGroupID
	 * @param nBusinessType
	 * @param nFromUserID
	 * @param nTime
	 * @param pPicData
	 */
	public void OnRecvChatPictureCallback(long nGroupID, int nBusinessType,
			long nFromUserID, long nTime, String szSeqID, byte[] pPicData);
	
	
	/**
	 * <ul>Receive audio data from server side.</ul>
	 * @param gid  belong group id
	 * @param businessType   1: IM  2: conference
	 * @param fromUserId  
	 * @param timeStamp
	 * @param messageId
	 * @param audioPath
	 */
	public void OnRecvChatAudio(long gid, int businessType, long fromUserId, long timeStamp, String messageId,
			String audioPath);
	

}
