package com.V2.jni;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.util.Log;

import com.V2.jni.ind.V2Group;
import com.V2.jni.ind.V2User;
import com.V2.jni.util.V2Log;
import com.V2.jni.util.XmlAttributeExtractor;

public class GroupRequest {

	public boolean loginResult;
	private static GroupRequest mGroupRequest;

	private List<WeakReference<GroupRequestCallback>> mCallbacks;

	private GroupRequest(Context context) {
		mCallbacks = new CopyOnWriteArrayList<WeakReference<GroupRequestCallback>>();
	};

	public static synchronized GroupRequest getInstance(Context context) {
		if (mGroupRequest == null) {
			mGroupRequest = new GroupRequest(context);
			if (!mGroupRequest.initialize(mGroupRequest)) {
				throw new RuntimeException(
						" can't not inintialize group request");
			}
		}
		return mGroupRequest;
	}

	public static synchronized GroupRequest getInstance() {
		if (mGroupRequest == null) {
			mGroupRequest = new GroupRequest(null);
			if (!mGroupRequest.initialize(mGroupRequest)) {
				throw new RuntimeException(
						" can't not inintialize group request");
			}
		}
		return mGroupRequest;
	}

	public native boolean initialize(GroupRequest request);

	public native void unInitialize();

	/**
	 * <ul>
	 * delete group. If groupType is {@link V2Group#TYPE_CROWD}, dismiss current
	 * crowd
	 * </ul>
	 * 
	 * @param groupType
	 * @param nGroupID
	 */
	public native void delGroup(int groupType, long nGroupID);

	/**
	 * <ul>
	 * quit from group if user is not administrator or creator
	 * </ul>
	 * 
	 * @param groupType
	 *            {@link V2Group}
	 * @param nGroupID
	 */
	public native void leaveGroup(int groupType, long nGroupID);

	// ɾ��һ�����ѷ���
	public native void delGroupUser(int groupType, long nGroupID, long nUserID);

	/**
	 * 
	 * @param groupType
	 * @param nGroupID
	 * @param sXml
	 */
	public native void modifyGroupInfo(int groupType, long nGroupID, String sXml);

	/**
	 * // sXmlConfData : // <conf canaudio="1" candataop="1" canvideo="1"
	 * conftype="0" haskey="0" // id="0" key="" // layout="1" lockchat="0"
	 * lockconf="0" lockfiletrans="0" mode="2" // pollingvideo="0" //
	 * subject="ss" syncdesktop="0" syncdocument="1" syncvideo="0" //
	 * chairuserid='0' chairnickname=''> // </conf> // szInviteUsers : // <xml>
	 * // <user id="11760" nickname=""/> // <user id="11762" nickname=""/> //
	 * </xml>
	 * 
	 * @param groupType
	 * @param groupInfo
	 * @param userInfo
	 */
	public native void createGroup(int groupType, String groupInfo,
			String userInfo);

	/**
	 * <ul>
	 * Invite user to join group.
	 * <ul>
	 * <ul>
	 * If groupType is {@link V2Group#TYPE_CONF} <br>
	 * groupInfo is :
	 * {@code <conf canaudio="1" candataop="1" canvideo="1" conftype="0" haskey="0" 
	 * id="0" key=""  layout="1" lockchat="0" lockconf="0" lockfiletrans="0"
	 * mode="2"  pollingvideo="0"  subject="ss"  chairuserid='0'
	 * chairnickname=''>  </conf>}
	 * </ul>
	 * 
	 * <ul>
	 * If groupType is {@link V2Group#TYPE_CROWD}<br>
	 * groupInfo is:
	 * {@code <crowd id="" name="" authtype="" size="" announcement=""  summary="" />}
	 * </ul>
	 * 
	 * @param groupType
	 *            {@code V2Group}
	 * @param groupInfo
	 *            group information
	 * @param userInfo
	 *            {@code <userlist><user id='1' /><user id='2' /></userlist>}
	 * @param additInfo
	 */
	public native void inviteJoinGroup(int groupType, String groupInfo,
			String userInfo, String additInfo);

	/**
	 * 
	 * @param groupType
	 * @param srcGroupID
	 * @param dstGroupID
	 * @param nUserID
	 */
	public native void moveUserToGroup(int groupType, long srcGroupID,
			long dstGroupID, long nUserID);

	public native void getGroupInfo(int type, long groupId);

	/**********************************************/

	/**
	 * 
	 * @param groupType
	 * @param nGroupID
	 * @param nUserID
	 * @param reason
	 */
	public native void refuseInviteJoinGroup(int groupType, long nGroupID,
			long nUserID, String reason);

	/**
	 * 
	 * @param groupType
	 * @param sGroupInfo
	 * @param sAdditInfo
	 */
	public native void applyJoinGroup(int groupType, String sGroupInfo,
			String sAdditInfo);

	/**
	 * 
	 * @param groupType
	 * @param sGroupInfo
	 * @param nUserID
	 */
	public native void acceptApplyJoinGroup(int groupType, String sGroupInfo,
			long nUserID);

	/**
	 * 
	 * @param groupType
	 * @param t
	 * @param nUserID
	 */
	public native void acceptInviteJoinGroup(int groupType, long groupId,
			long nUserID);

	/**
	 * 
	 * @param groupType
	 * @param t
	 * @param nUserID
	 */
	private void OnAcceptInviteJoinGroup(int groupType, long groupId,
			long nUserID) {
		V2Log.e("Group Request  OnAcceptInviteJoinGroup  ==>" + groupType
				+ "   " + groupId + "  " + nUserID);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnAcceptInviteJoinGroup(groupType, groupId, nUserID);
			}
		}
	}

	public void OnConfSyncOpenVideo(String str) {

	}

	// �ܾ����Ⱥ
	public native void refuseApplyJoinGroup(int groupType, String sGroupInfo,
			long nUserID, String sReason);

	public native void groupUploadFile(int groupType, long nGroupId, String sXml);

	public native void delGroupFile(int groupType, long nGroupId, String sXml);

	public native void getGroupFileInfo(int groupType, long nGroupId);

	private void OnAddGroupFile(int type, long nGroupId, String sXml) {
		V2Log.e("Group Request  OnAddGroupFile" + type + "   " + nGroupId
				+ "  " + sXml);
	}

	private void OnDelGroupFile(int type, long nGroupId, String sXml) {
		V2Log.e("Group Request  OnDelGroupFile" + type + "   " + nGroupId
				+ "  " + sXml);
	}

	private void OnGetGroupFileInfo(int type, long nGroupId, String sXml) {
		V2Log.e("Group Request  OnGetGroupFileInfo" + type + "   " + nGroupId
				+ "  " + sXml);
	}

	/**
	 * This is unsolicited callback. This function will be call after log in
	 * 
	 * @param groupType
	 *            4 : conference
	 * @param sXml
	 */
	private void OnGetGroupInfo(int groupType, String sXml) {
		V2Log.d("OnGetGroupInfo::" + groupType + ":" + sXml);
		List<V2Group> list = null;
		if (groupType == V2Group.TYPE_CONF) {
			list = XmlAttributeExtractor.parseConference(sXml);
		} else if (groupType == V2Group.TYPE_CROWD) {
			list = XmlAttributeExtractor.parseCrowd(sXml);
		} else if (groupType == V2Group.TYPE_CONTACTS_GROUP) {
			list = XmlAttributeExtractor.parseContactsGroup(sXml);
		} else if (groupType == V2Group.TYPE_ORG) {
			list = XmlAttributeExtractor.parseOrgGroup(sXml);
		}
		for (int i = 0; i < this.mCallbacks.size(); i++) {
			WeakReference<GroupRequestCallback> wrcb = this.mCallbacks.get(i);
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnGetGroupInfoCallback(groupType, list);
			}
		}
	}

	public void addCallback(GroupRequestCallback callback) {
		this.mCallbacks.add(new WeakReference<GroupRequestCallback>(callback));
	}

	public void removeCallback(GroupRequestCallback callback) {
		for (int i = 0; i < mCallbacks.size(); i++) {
			if (mCallbacks.get(i).get() == callback) {
				mCallbacks.remove(i);
				break;
			}
		}
	}

	private void OnGetGroupUserInfo(int groupType, long nGroupID, String sXml) {
		V2Log.d("OnGetGroupUserInfo -> " + groupType + ":" + nGroupID + ":"
				+ sXml);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnGetGroupUserInfoCallback(groupType, nGroupID, sXml);
			}
		}

	}

	private void OnAddGroupUserInfo(int groupType, long nGroupID, String sXml) {
		V2Log.d("OnAddGroupUserInfo ->" + groupType + ":" + nGroupID + ":"
				+ sXml);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnAddGroupUserInfoCallback(groupType, nGroupID, sXml);
			}
		}
	}

	private void OnDelGroupUser(int groupType, long nGroupID, long nUserID) {
		V2Log.d("OnDelGroupUser -> " + groupType + ":" + nGroupID + ":"
				+ nUserID);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnDelGroupUserCallback(groupType, nGroupID, nUserID);
			}
		}
	}

	private void OnAddGroupInfo(int groupType, long nParentID, long nGroupID,
			String sXml) {
		V2Log.d("OnAddGroupInfo:: " + groupType + ":" + nParentID + ":"
				+ nGroupID + ":" + sXml);

		String gid = XmlAttributeExtractor.extract(sXml, " id='", "'");
		String name = XmlAttributeExtractor.extract(sXml, " name='", "'");
		V2Group vg = new V2Group(Long.parseLong(gid), name, groupType);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.onAddGroupInfo(vg);
			}
		}
	}

	/**
	 * TODO to be implement comment
	 * 
	 * @param groupType
	 * @param nGroupID
	 * @param sXml
	 */
	private void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
		V2Log.d("OnModifyGroupInfo::-->" + groupType + ":" + nGroupID + ":"
				+ sXml);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnModifyGroupInfoCallback(groupType, nGroupID, sXml);
			}
		}

	}

	/**
	 * TODO add implement comment
	 * 
	 * @param groupType
	 * @param nGroupID
	 * @param bMovetoRoot
	 */
	private void OnDelGroup(int groupType, long nGroupID, boolean bMovetoRoot) {
		V2Log.d("OnDelGroup::==>" + groupType + ":" + nGroupID + ":"
				+ bMovetoRoot);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnDelGroupCallback(groupType, nGroupID, bMovetoRoot);
			}
		}
	}

	private void OnInviteJoinGroup(int groupType, String groupInfo,
			String userInfo, String additInfo) {
		V2Log.d("OnInviteJoinGroup::==>" + groupType + ":" + groupInfo + ":"
				+ userInfo + ":" + additInfo);
		String id = XmlAttributeExtractor.extract(groupInfo, " id='", "'");
		if (id == null || id.isEmpty()) {
			V2Log.e(" Unknow group information:" + groupInfo);
			return;
		}
		V2Group group = new V2Group(Long.parseLong(id), groupType);
		if (groupType == V2Group.TYPE_CONF) {
			String name = XmlAttributeExtractor.extract(groupInfo, "subject='",
					"'");
			String starttime = XmlAttributeExtractor.extract(groupInfo,
					"starttime='", "'");
			String createuserid = XmlAttributeExtractor.extract(groupInfo,
					"createuserid='", "'");
			group.name = name;
			group.createTime = new Date(Long.parseLong(starttime) * 1000);
			group.chairMan = new V2User(Long.valueOf(createuserid));
			group.owner = new V2User(Long.valueOf(createuserid));

		} else if (groupType == V2Group.TYPE_CROWD) {
			String createuserid = XmlAttributeExtractor.extract(userInfo,
					" id='", "'");
			String auth = XmlAttributeExtractor.extract(groupInfo,
					"authtype='", "'");
			String uname = XmlAttributeExtractor.extract(userInfo,
					"nickname='", "'");
			String name = XmlAttributeExtractor.extract(groupInfo, "name='",
					"'");
			group.name = name;
			group.creator = new V2User(Long.valueOf(createuserid), uname);
			if (auth != null) {
				group.authType = Integer.parseInt(auth);
			}
		}

		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnInviteJoinGroupCallback(group, userInfo, additInfo);
			}
		}

	}

	private void OnRefuseInviteJoinGroup(int groupType, long nGroupID,
			long nUserID, String sxml) {
		V2Log.d("OnRefuseInviteJoinGroup ==>" + "groupType:" + groupType + ","
				+ "nGroupID:" + nGroupID + "," + "nUserID:" + nUserID + ","
				+ "sxml:" + sxml);

		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnRefuseInviteJoinGroup(groupType, nGroupID, nUserID,
						sxml);
			}
		}

		// // ƴװ������Ϣ
		// RefuseMsgType refuseMsgType = new RefuseMsgType();
		// refuseMsgType.setReason(sxml);
		// refuseMsgType.setUserBaseInfo(sxml);
		//
		// Intent addIntent = new Intent(SplashActivity.IM);
		// addIntent.putExtra("MsgType", MsgType.REFUSE_ADD);
		// addIntent.putExtra("MSG", refuseMsgType);
		// context.sendOrderedBroadcast(addIntent,null);
	}

	private void OnMoveUserToGroup(int groupType, long srcGroupID,
			long dstGroupID, long nUserID) {
		V2Log.d("OnMoveUserToGroup:: " + groupType + ":" + srcGroupID + ":"
				+ dstGroupID + ":" + nUserID);
		for (WeakReference<GroupRequestCallback> wrcb : mCallbacks) {
			Object obj = wrcb.get();
			if (obj != null) {
				GroupRequestCallback callback = (GroupRequestCallback) obj;
				callback.OnMoveUserToGroup(groupType, new V2Group(srcGroupID,
						"", groupType), new V2Group(dstGroupID, "", groupType),
						new V2User(nUserID));
			}
		}
	}

	private void OnApplyJoinGroup(int groupType, long nGroupID,
			String userInfo, String reason) {
		Log.e("ImRequest UI", "OnApplyJoinGroup:: " + groupType + ":"
				+ nGroupID + ":" + userInfo + ":" + reason);
	}

	private void OnAcceptApplyJoinGroup(int groupType, String sXml) {
		V2Log.d("OnAcceptApplyJoinGroup ==>" + "groupType:" + groupType + ","
				+ "sXml:" + sXml);
	}

	private void OnRefuseApplyJoinGroup(int groupType, String sGroupInfo,
			String reason) {
		V2Log.d("OnRefuseApplyJoinGroup ==>" + "groupType:" + groupType + ","
				+ "sGroupInfo:" + sGroupInfo + "," + "reason:" + reason);
	}

	/**
	 * 会议中创建白板的回调 TODO implement
	 * 
	 * @param eGroupType
	 * @param nGroupID
	 * @param szWBoardID
	 * @param nWhiteIndex
	 */
	private void OnGroupCreateWBoard(int eGroupType, long nGroupID,
			String szWBoardID, int nWhiteIndex) {
		V2Log.e("GroupRequest UI", "OnGroupCreateWBoard ---> eGroupType :"
				+ eGroupType + " | nGroupID: " + nGroupID + " | szWBoardID: "
				+ szWBoardID + " | nWhiteIndex: " + nWhiteIndex);
	};

	/**
	 * 文件重命名 TODO implement
	 * 
	 * @param eGroupType
	 * @param nGroupID
	 * @param sFileID
	 * @param sNewName
	 */
	private void OnRenameGroupFile(int eGroupType, long nGroupID,
			String sFileID, String sNewName) {
		V2Log.e("GroupRequest UI", "OnGroupCreateWBoard ---> eGroupType :"
				+ eGroupType + " | nGroupID: " + nGroupID + " | sFileID: "
				+ sFileID + " | sNewName: " + sNewName);
	};

	/**
	 * 收到白板会话被关闭的回调 TODO implement
	 * 
	 * @param eGroupType
	 * @param nGroupID
	 * @param szWBoardID
	 */
	private void OnWBoardDestroy(int eGroupType, long nGroupID,
			String szWBoardID) {
		V2Log.e("GroupRequest UI", "OnGroupCreateWBoard ---> eGroupType :"
				+ eGroupType + " | nGroupID: " + nGroupID + " | szWBoardID: "
				+ szWBoardID);
	};

	/**
	 * 会议中创建文档共享的回调 eWhiteShowType白板显示类型 TODO implement
	 * 
	 * @param eGroupType
	 * @param nGroupID
	 * @param szWBoardID
	 * @param szFileName
	 * @param eWhiteShowType
	 */
	private void OnGroupCreateDocShare(int eGroupType, long nGroupID,
			String szWBoardID, String szFileName, int eWhiteShowType) {
		V2Log.e("GroupRequest UI", "OnGroupCreateWBoard ---> eGroupType :"
				+ eGroupType + " | nGroupID: " + nGroupID + " | szWBoardID: "
				+ szWBoardID + " | szFileName: " + szFileName
				+ " | eWhiteShowType: " + eWhiteShowType);
	};
}
