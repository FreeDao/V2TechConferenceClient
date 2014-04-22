package com.V2.jni;


public interface WBRequestCallback {
	
	/**
	 * FIXME add comment
	 * @param nGroupID
	 * @param nBusinessType
	 * @param nFromUserID
	 * @param szWBoardID
	 * @param nWhiteIndex
	 * @param szFileName
	 * @param type
	 */
	public void OnWBoardChatInviteCallback(long nGroupID, int nBusinessType, long  nFromUserID, String szWBoardID, 
			int nWhiteIndex,String szFileName, int type);

	public void OnWBoardPageListCallback(String szWBoardID, String szPageData,
			int nPageID);

	public void OnWBoardActivePageCallback(long nUserID, String szWBoardID, int nPageID);

	public void OnWBoardDocDisplayCallback(String szWBoardID, int nPageID,
			String szFileName, int result);
	
	
	public void OnWBoardClosedCallback(long nGroupID, int nBusinessType, long nUserID,
			String szWBoardID);
	
	public void OnWBoardAddPageCallback(String szWBoardID, int nPageID);

}
