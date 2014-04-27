package com.v2tech.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.V2.jni.AudioRequest;
import com.V2.jni.AudioRequestCallback;
import com.V2.jni.ChatRequest;
import com.v2tech.util.V2Log;
import com.v2tech.vo.UserAudioDevice;
import com.v2tech.vo.VImageMessage;
import com.v2tech.vo.VMessage;

public class ChatService extends Handler {

	private AudioRequestCallback callback;

	private Message mCaller;

	private Handler thread;

	public ChatService() {
		super();
		init();
	}

	public ChatService(Callback callback) {
		super(callback);
		init();
	}

	public ChatService(Looper looper, Callback callback) {
		super(looper, callback);
		init();
	}

	public ChatService(Looper looper) {
		super(looper);
		init();
	}

	private void init() {
		callback = new AudioRequestCallbackImpl();
		AudioRequest.getInstance().registerCallback(callback);

		HandlerThread backEnd = new HandlerThread("back-end");
		backEnd.start();
		thread = new Handler(backEnd.getLooper());
	}

	/**
	 * send message
	 * 
	 * @param msg
	 * @param caller
	 */
	public void sendVMessage(final VMessage msg, final Message caller) {
		if (msg == null || msg.getToUser() == null) {
			V2Log.w(" ToUser is null can not send message");
			return;
		}
		thread.post(new Runnable() {
			@Override
			public void run() {
				ChatRequest.getInstance().sendChatText(msg.mGroupId,
						msg.getToUser().getmUserId(), msg.getUUID(), msg.toXml(),
						msg.getMsgCode());
				if (msg.getType() == VMessage.MessageType.IMAGE
						|| msg.getType() == VMessage.MessageType.IMAGE_AND_TEXT) {
					byte[] data = ((VImageMessage) msg).getWrapperData();
					ChatRequest.getInstance().sendChatPicture(msg.mGroupId,
							msg.getToUser().getmUserId(), msg.getUUID(),data, data.length,
							msg.getMsgCode());
				}
				if (caller != null) {
					caller.sendToTarget();
				} else {
					V2Log.w(" requester don't expect response");
				}
			}

		});
	}

	/**
	 * Invite contact for chat
	 * @param ud
	 * @param caller
	 */
	public void inviteUserAudioChat(UserAudioDevice ud, Message caller) {
		if (mCaller != null) {
			V2Log.e(" audio call is on going");
			return;
		}
		this.mCaller = caller;

		AudioRequest.getInstance().InviteAudioChat(0,
				ud.getUser().getmUserId(), AudioRequest.BT_IM);
	}

	/**
	 * 
	 * @param ud
	 * @param caller
	 */
	public void cancelAudioCall(UserAudioDevice ud, Message caller) {
		this.mCaller = null;
		AudioRequest.getInstance().CloseAudioChat(0, ud.getUser().getmUserId(),
				AudioRequest.BT_IM);
		caller.sendToTarget();
	}

	public void acceptAudioChat(UserAudioDevice ud, Message caller) {
		AudioRequest.getInstance().AcceptAudioChat(0,
				ud.getUser().getmUserId(), AudioRequest.BT_IM);
	}

	public void refuseAudioChat(UserAudioDevice ud, Message caller) {

	}

	class AudioRequestCallbackImpl implements AudioRequestCallback {

		@Override
		public void OnAudioChatAccepted(long nGroupID, long nBusinessType,
				long nFromUserID) {
			mCaller.arg1 = 1;
			mCaller.arg2 = 1;
			mCaller.sendToTarget();
		}

		@Override
		public void OnAudioChatRefused(long nGroupID, long nBusinessType,
				long nFromUserID) {

		}

	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
	}

}
