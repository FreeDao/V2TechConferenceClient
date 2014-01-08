package com.V2.jni;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

//import com.xinlan.im.adapter.XiuLiuApplication;
//import com.xinlan.im.bean.msgtype.AddFriMsgType;
//import com.xinlan.im.bean.msgtype.CreateGroupMsgType;
//import com.xinlan.im.bean.msgtype.DelFriMsgType;
//import com.xinlan.im.bean.msgtype.DestoryGroupMsgType;
//import com.xinlan.im.bean.msgtype.FriendMsgType;
//import com.xinlan.im.bean.msgtype.GroupMsgType;
//import com.xinlan.im.bean.msgtype.LoginMsgType;
//import com.xinlan.im.bean.msgtype.ModifyGroupMsgType;
//import com.xinlan.im.bean.msgtype.MoveGroupMsgType;
//import com.xinlan.im.bean.msgtype.MsgType;
//import com.xinlan.im.bean.msgtype.RefuseMsgType;
//import com.xinlan.im.ui.SplashActivity;
//import com.xinlan.im.ui.chat.bean.Group;
//import com.xinlan.im.ui.chat.bean.NYXUser;
//import com.xinlan.im.ui.chat.db.DbHelper;
//import com.xinlan.im.utils.XmlParserUtils;

public class GroupRequest {

	private Activity context;
	public boolean loginResult;
	private static GroupRequest mGroupRequest;
//	private XiuLiuApplication application;
//	private DbHelper dbHelper;
//	private LoginMsgType loginMsgType;

	private GroupRequest(Activity context) {
		this.context = context;
//		application = (XiuLiuApplication) context.getApplication();
//		dbHelper = DbHelper.getInstance(context);
	};

	public static synchronized GroupRequest getInstance(Activity context) {
		if (mGroupRequest == null) {
			mGroupRequest = new GroupRequest(context);
			if (!mGroupRequest.initialize(mGroupRequest)) {
				throw new RuntimeException(" can't not inintialize group request");
			}
		}
		return mGroupRequest;
	}

	public native boolean initialize(GroupRequest request);

	public native void unInitialize();

	// ɾ��һ�����ѷ���
	public native void delGroup(int groupType, long nGroupID);

	// �뿪һ�����ѷ���
	public native void leaveGroup(int groupType, long nGroupID);

	// ɾ��һ�����ѷ���
	public native void delGroupUser(int groupType, long nGroupID,
			long nUserID);

	// �޸ĺ��ѷ���
	public native void modifyGroupInfo(int groupType, long nGroupID,
			String sXml);

	// ����һ������
	public native void createGroup(int groupType, String groupInfo,
			String userInfo);

	// ������˼�����
	/*
	 */
	public native void inviteJoinGroup(int groupType, String groupInfo,
			String userInfo, String additInfo);

	// �ƶ����ѵ������
	public native void moveUserToGroup(int groupType, long srcGroupID,
			long dstGroupID, long nUserID);
	
	public native void getGroupInfo(int type,long userid);
	
	/**********************************************/

	// �ܾ�����������Ⱥ
	public native void refuseInviteJoinGroup(int groupType, long nGroupID,
			long nUserID, String reason);

	// �������Ⱥ
	public native void applyJoinGroup(int groupType, String sGroupInfo,
			String sAdditInfo);

	// ���ܼ���Ⱥ
	public native void acceptApplyJoinGroup(int groupType,
			String sGroupInfo, long nUserID);

	// �ܾ����Ⱥ
	public native void refuseApplyJoinGroup(int groupType,
			String sGroupInfo, long nUserID, String sReason);

	private void OnGetGroupInfo(int groupType, String sXml) {
		Log.e("ImRequest UI", "OnGetGroupInfo:: �õ���������Ϣ" + groupType + ":"
				+ sXml);
		System.out.println(sXml);
		// ƴװ��Ϣ
//		GroupMsgType friendMsgType = new GroupMsgType();
//		friendMsgType.setGroupxml(sXml);
//
//		Intent addIntent = new Intent(SplashActivity.IM);
//		addIntent.putExtra("MsgType", MsgType.FRIENDGROUP);
//		addIntent.putExtra("MSG", friendMsgType);
//		context.sendOrderedBroadcast(addIntent,null);
	}

	private void OnGetGroupUserInfo(int groupType, long nGroupID, String sXml) {
		Log.e("ImRequest UI", "OnGetGroupUserInfo:: �õ�����ѵ���ϸ��Ϣ" + groupType
				+ ":" + nGroupID + ":" + sXml);
		System.out.println("�õ�������Ϣ");

		// ƴװ��Ϣ
//		FriendMsgType friendMsgType = new FriendMsgType();
//		friendMsgType.setFriendxml(sXml);
//		friendMsgType.setGroupid(nGroupID);
//
//		Intent addIntent = new Intent(SplashActivity.IM);
//		addIntent.putExtra("MsgType", MsgType.FRIENDLIST);
//		addIntent.putExtra("MSG", friendMsgType);
//		context.sendOrderedBroadcast(addIntent,null);

	}

	private void OnAddGroupUserInfo(int groupType, long nGroupID, String sXml) {
		Log.e("ImRequest UI", "OnAddGroupUserInfo::��Ӻ��ѵ������Ϣ" + groupType + ":"
				+ nGroupID + ":" + sXml);
		
//		List<NYXUser> addUsers = XmlParserUtils
//				.parserNYXUser(new ByteArrayInputStream(sXml.getBytes()),application.getLocalUser().getId());
//
//		// ƴװ��Ϣ
//		AddFriMsgType addFriMsgType = new AddFriMsgType();
//
//		addFriMsgType.setAddUsers(addUsers);
//		addFriMsgType.setGroupid(nGroupID);
//
//		Intent addIntent = new Intent(SplashActivity.IM);
//		addIntent.putExtra("MsgType", MsgType.ADDFRI);
//		addIntent.putExtra("MSG", addFriMsgType);
//		context.sendOrderedBroadcast(addIntent,null);
	}

	private void OnDelGroupUser(int groupType, long nGroupID, long nUserID) {
		Log.e("ImRequest UI", "OnDelGroupUser:: ������ɾ�����" + groupType + ":"
				+ nGroupID + ":" + nUserID);
		// ƴװ��Ϣ
//		DelFriMsgType delFri = new DelFriMsgType();
//		delFri.setUserid(nUserID);
//
//		// ͨ��㲥������Ϣ��֪ͨ,������������˻���
//		Intent delIntent = new Intent(SplashActivity.IM);
//		delIntent.putExtra("MsgType", MsgType.DELFRI);
//		delIntent.putExtra("MSG", delFri);
//		context.sendOrderedBroadcast(delIntent,null);
	}

	private void OnAddGroupInfo(int groupType, long nParentID, long nGroupID,
			String sXml) {
		Log.e("ImRequest UI", "OnAddGroupInfo:: ����һ����" + groupType + ":"
				+ nParentID + ":" + nGroupID + ":" + sXml);
		
		// <friendgroup id='312' name='byhy'/>
//		InputStream in=new ByteArrayInputStream(sXml.getBytes());
//		Group group=XmlParserUtils.parserAddGroup(in);
//		
//		// ƴװ��Ϣ
//		CreateGroupMsgType createMsgType = new CreateGroupMsgType();
//		createMsgType.setmGroup(group);
//
//		Intent createIntent = new Intent(SplashActivity.IM);
//		createIntent.putExtra("MsgType", MsgType.CREATE_GROUP);
//		createIntent.putExtra("MSG", createMsgType);
//		context.sendOrderedBroadcast(createIntent,null);
	}

	private void OnModifyGroupInfo(int groupType, long nGroupID, String sXml) {
		Log.e("ImRequest UI", "OnModifyGroupInfo::�޸�һ������Ϣ" + groupType + ":"
				+ nGroupID + ":" + sXml);

//		InputStream in=new ByteArrayInputStream(sXml.getBytes());
//		Group group=XmlParserUtils.parserAddGroup(in);
//		
//		// ƴװ��Ϣ
//		ModifyGroupMsgType modifyMsgType = new ModifyGroupMsgType();
//		modifyMsgType.setMgGroup(group);
//		
//		Intent modifyIntent = new Intent(SplashActivity.IM);
//		modifyIntent.putExtra("MsgType", MsgType.MODIFY_GROUP);
//		modifyIntent.putExtra("MSG", modifyMsgType);
//		context.sendOrderedBroadcast(modifyIntent,null);
	}

	private void OnDelGroup(int groupType, long nGroupID, boolean bMovetoRoot) {
		Log.e("ImRequest UI", "OnDelGroup::ɾ��һ����" + groupType + ":" + nGroupID
				+ ":" + bMovetoRoot);

		// ƴװ��Ϣ
//		DestoryGroupMsgType destoryMsgType = new DestoryGroupMsgType();
//		destoryMsgType.setnGroupID(nGroupID);
//
//		Intent destoryIntent = new Intent(SplashActivity.IM);
//		destoryIntent.putExtra("MsgType", MsgType.DESTORY_GROUP);
//		destoryIntent.putExtra("MSG", destoryMsgType);
//		context.sendOrderedBroadcast(destoryIntent,null);
	}

	private void OnInviteJoinGroup(int groupType, String groupInfo,
			String userInfo, String additInfo) {
		Log.e("ImRequest UI", "OnInviteJoinGroup::������Ѽ�����" + groupType + ":"
				+ groupInfo + ":" + userInfo + ":" + additInfo);
	}

	private void OnMoveUserToGroup(int groupType, long srcGroupID,
			long dstGroupID, long nUserID) {
		Log.e("ImRequest UI", "OnMoveUserToGroup:: �ƶ����ѵ�ʲô��" + groupType + ":"
				+ srcGroupID + ":" + dstGroupID + ":" + nUserID);

		// ƴװ��Ϣ
//		MoveGroupMsgType moveMsgtype = new MoveGroupMsgType();
//		moveMsgtype.setnDstGroupID(dstGroupID);
//		moveMsgtype.setnDstUserID(dstGroupID);
//
//		// ͨ��㲥������Ϣ��֪ͨ,������������˻���
//		Intent moveIntent = new Intent(SplashActivity.IM);
//		moveIntent.putExtra("MsgType", MsgType.MOVE_GROUP);
//		moveIntent.putExtra("MSG", moveMsgtype);
//		context.sendOrderedBroadcast(moveIntent,null);
	}

	private void OnRefuseInviteJoinGroup(int groupType, long nGroupID,
			long nUserID, String sxml) {
		Log.e("ImRequest UI", "OnRefuseInviteJoinGroup:: �ܾ���˵��������ʲô��"
				+ groupType + ":" + nGroupID + ":" + nUserID + ":" + sxml);
		
//		// ƴװ������Ϣ
//				RefuseMsgType refuseMsgType = new RefuseMsgType();
//				refuseMsgType.setReason(sxml);
//				refuseMsgType.setUserBaseInfo(sxml);
//
//				Intent addIntent = new Intent(SplashActivity.IM);
//				addIntent.putExtra("MsgType", MsgType.REFUSE_ADD);
//				addIntent.putExtra("MSG", refuseMsgType);
//				context.sendOrderedBroadcast(addIntent,null);
	}

	private void OnApplyJoinGroup(int groupType, long nGroupID,
			String userInfo, String reason) {
		Log.e("ImRequest UI", "OnApplyJoinGroup:: ���������" + groupType + ":"
				+ nGroupID + ":" + userInfo + ":" + reason);
	}

	private void OnAcceptApplyJoinGroup(int groupType, String sXml) {
		Log.e("ImRequest UI", "OnAcceptApplyJoinGroup:: �������������" + groupType
				+ ":" + sXml);
	}

	private void OnRefuseApplyJoinGroup(int groupType, String sGroupInfo,
			String reason) {
		Log.e("ImRequest UI", "OnRefuseApplyJoinGroup:: �ܾ����������" + groupType
				+ ":" + sGroupInfo + ":" + reason);
	}

}
