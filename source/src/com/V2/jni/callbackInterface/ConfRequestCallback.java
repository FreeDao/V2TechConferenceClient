package com.V2.jni.callbackInterface;

import com.V2.jni.ConfRequest;
import com.V2.jni.ind.BoUserInfoShort;
import com.V2.jni.ind.BoUserInfoBase;
import com.V2.jni.ind.V2Conference;

public interface ConfRequestCallback {

	/**
	 * <ul>
	 * Indicate result what current request to enter conference. <br/>
	 * This call back is called after user request to enter conference.
	 * </ul>
	 * 
	 * @param nConfID
	 *            conference ID
	 * @param nTime
	 *            entered time
	 * @param szConfData
	 * @param nJoinResult
	 *            0 means successfully
	 * 
	 * @see ConfRequest#enterConf(long)
	 */
	public void OnEnterConfCallback(long nConfID, long nTime,
			String szConfData, int nJoinResult);

	/**
	 * <ul>
	 * Indicate new attendee entered current conference which user in.<br>
	 * This callback is called many times and same with current conference's
	 * attendee count except self.
	 * </ul>
	 * 
	 * @param nConfID
	 *            conference ID what user entered
	 * @param nTime
	 * @param user
	 */
	public void OnConfMemberEnterCallback(long nConfID, long nTime,
			BoUserInfoShort boUserInfoShort);

	/**
	 * <ul>
	 * <Indicate attendee exited current conference.
	 * </ul>
	 * 
	 * @param nConfID
	 *            conference ID what user entered
	 * @param nTime
	 * @param nUserID
	 *            exited user's ID
	 */
	public void OnConfMemberExitCallback(long nConfID, long nTime, long nUserID);

	/**
	 * <ul>
	 * Indicate user has been requested to get out of conference.
	 * </ul>
	 * <ul>
	 * Two reason will affect this function will be called :<br>
	 * 1. Chair man of conference requested.<br>
	 * 2. Chair man removed conference.
	 * </ul>
	 * 
	 * @param nReason
	 *            TODO add comments of reason
	 */
	public void OnKickConfCallback(int nReason);

	/**
	 * FIXME add code
	 * 
	 * @param userid
	 * @param type
	 * @param status
	 */
	public void OnGrantPermissionCallback(long userid, int type, int status);

	/**
	 * User invite current user to join further conference.
	 * 
	 * @param v2conf
	 * @param user
	 */
	public void OnConfNotify(V2Conference v2conf, BoUserInfoBase user);

	/**
	 * Notify user request host event
	 * 
	 * @param user
	 *            request user
	 * @param permission
	 */
	public void OnConfHostRequest(BoUserInfoBase user, int permission);

	public void OnConfSyncOpenVideo(String str);

	public void OnConfSyncCloseVideo(long gid, String str);

	public void OnConfSyncCloseVideoToMobile(long nDstUserID, String sDstMediaID);

	public void OnConfSyncOpenVideoToMobile(String sSyncVideoMsgXML);

}
