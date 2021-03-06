package com.V2.jni.callbackInterface;

import java.util.List;

import com.V2.jni.ImRequest;
import com.V2.jni.ind.BoUserInfoBase;


/**
 * 
 * @author 28851274
 *
 */
public interface ImRequestCallback {

	
	/**
	 * Login call back function. <br>
	 * This function is called after you call {@link ImRequest#login(String, String, int, int)}.
	 * @param nUserID if succeed means user ID, otherwise 0
	 * @param nStatus
	 * @param nResult  0: succeed,  1: failed
	 * @param serverTime 
	 * @param sDBID : Server id
	 * @see com.bizcom.request.jni.JNIResponse.Result
	 */
	public void OnLoginCallback(long nUserID, int nStatus, int nResult , long serverTime , String sDBID);
	
	
	/**
	 * <ul>When Same user log in with other device, then this function will be called</ul>
	 * @param nType device type of logged
	 */
	public void OnLogoutCallback(int nType);
	
	/**
	 * When network connection state changed, this function will be called.<br>
	 * <p>
	 *    When call {@link ImRequest#login(String, String, int, int)},  this call back will before than {@link #OnLoginCallback(long, int, int)}
	 * </p>
	 * @param nResult 301 can't not connect server;  0: succeed
	 */
	public void OnConnectResponseCallback(int nResult);
	
	
	/**
	 * Update user information call back. <br>
	 * <p> When you log in successfully, this function will be called.<br>
	 *    another case is when you call {@link ImRequest#getUserBaseInfo(long)}, this function will be called by JNI.
	 * </p>
	 * @param nUserID  user id
	 * @param user
	 */
	public void OnUpdateBaseInfoCallback(BoUserInfoBase user);
	
	
	
	/**
	 * <ul>User status update API.</ul>
	 * 
	 * @param nUserID
	 * @param nType 1 PC 2 cell phone
	 * @param nStatus  1 is online, 0 is offline
	 * @param szStatusDesc
	 * 
	 * @see com.bizcom.vo.User.Status
	 */
	public void OnUserStatusUpdatedCallback(long nUserID, int nType, int nStatus, String szStatusDesc);
	
	
	
	/**
	 *  <ul>Indicate user avatar changed.</ul>
	 * @param nAvatarType
	 * @param nUserID  User ID which user's changed avatar
	 * @param AvatarName  patch of avatar
	 */
	public void OnChangeAvatarCallback(int nAvatarType, long nUserID, String AvatarName);
	
	
	/**
	 * 
	 * @param nUserId
	 * @param sCommmentName
	 */
	public void OnModifyCommentNameCallback(long nUserId, String sCommmentName);
	
	
	
	/**
	 * 
	 * @param list
	 */
	public void OnSearchUserCallback(List<BoUserInfoBase> list);
	
	/**
	 * <ul>Indicate the all group info or group's users info  has already received! over! </ul>
	 */
	public void OnGroupsLoaded();
	
	/**
	 * <ul>Indicate the all messsages that offline type start received!</ul>
	 */
	public void OnOfflineStart();
	
	/**
	 * <ul>Indicate the all messsages that offline type has already received! over! </ul>
	 */
	public void OnOfflineEnd();
}
