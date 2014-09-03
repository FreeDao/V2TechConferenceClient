package com.v2tech.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Bitmap;

import com.V2.jni.ind.V2Group;
import com.V2.jni.util.V2Log;
import com.v2tech.util.GlobalState;
import com.v2tech.vo.ConferenceGroup;
import com.v2tech.vo.ContactGroup;
import com.v2tech.vo.CrowdGroup;
import com.v2tech.vo.Group;
import com.v2tech.vo.Group.GroupType;
import com.v2tech.vo.OrgGroup;
import com.v2tech.vo.User;
import com.v2tech.vo.UserDeviceConfig;

public class GlobalHolder {

	public static final int STATE_IN_AUDIO_CONVERSATION = 0x00001;
	public static final int STATE_IN_VIDEO_CONVERSATION = 0x00002;
	public static final int STATE_IN_MEETING_CONVERSATION = 0x00004;

	private static GlobalHolder holder;

	private User mCurrentUser;

	private List<Group> mOrgGroup = new ArrayList<Group>();

	private List<Group> mConfGroup = new ArrayList<Group>();

	private List<Group> mContactsGroup = new ArrayList<Group>();

	private List<Group> mCrowdGroup = new ArrayList<Group>();

	private Map<Long, User> mUserHolder = new HashMap<Long, User>();
	private Map<Long, Group> mGroupHolder = new HashMap<Long, Group>();
	private Map<Long, String> mAvatarHolder = new HashMap<Long, String>();

	private Map<Long, Set<UserDeviceConfig>> mUserDeviceList = new HashMap<Long, Set<UserDeviceConfig>>();

	private Map<Long, Bitmap> mAvatarBmHolder = new HashMap<Long, Bitmap>();

	private GlobalState mState = new GlobalState();

	public static synchronized GlobalHolder getInstance() {
		if (holder == null) {
			holder = new GlobalHolder();
		}
		return holder;
	}

	private GlobalHolder() {
		BitmapManager.getInstance().registerLastBitmapChangedListener(
				bitmapChangedListener);
	}

	public User getCurrentUser() {
		return mCurrentUser;
	}

	public long getCurrentUserId() {
		if (mCurrentUser == null) {
			return 0;
		} else {
			return mCurrentUser.getmUserId();
		}
	}

	public void setCurrentUser(User u) {
		this.mCurrentUser = u;
		this.mCurrentUser.setCurrentLoggedInUser(true);
		this.mCurrentUser.updateStatus(User.Status.ONLINE);
		User mU = getUser(u.getmUserId());
		if (mU != null) {
			mU.updateStatus(User.Status.ONLINE);
		} else {
			// putUser(u.getmUserId(), u);
		}
	}

	private Object mUserLock = new Object();

	public User putUser(long id, User u) {
		if (u == null) {
			return null;
		}
		synchronized (mUserLock) {
			Long key = Long.valueOf(id);
			User cu = mUserHolder.get(key);
			if (cu != null) {
				if (u.getSignature() != null) {
					cu.setSignature(u.getSignature());
				}
				if (u.getName() != null) {
					cu.setName(u.getName());
				}
				if (u.getGender() != null) {
					cu.setGender(u.getGender());
				}

				if (u.getTelephone() != null) {
					cu.setTelephone(u.getTelephone());
				}
				if (u.getCellPhone() != null) {
					cu.setCellPhone(u.getCellPhone());
				}
				if (u.getAddress() != null) {
					cu.setAddress(u.getAddress());
				}
				if (u.getNickName() != null) {
					cu.setNickName(u.getNickName());
				}
				if (u.getBirthday() != null) {
					cu.setBirthday(u.getBirthday());
				}
				V2Log.i(" merge user information " + id + " " + cu.getName());
				return cu;
			}
			mUserHolder.put(key, u);
			Bitmap avatar = mAvatarBmHolder.get(key);
			if (avatar != null) {
				u.setAvatarBitmap(avatar);
			}
		}

		return u;
	}

	public User getUser(long id) {
		Long key = Long.valueOf(id);
		return mUserHolder.get(key);
	}

	/**
	 * Update group information according server's side push data
	 * 
	 * @param gType
	 * @param list
	 * 
	 */
	public void updateGroupList(Group.GroupType gType, List<V2Group> list) {

		for (V2Group vg : list) {
			Group cache = mGroupHolder.get(Long.valueOf(vg.id));
			if (cache != null) {
				continue;
			}
			Group g = null;
			if (gType == GroupType.CHATING) {
				g = new CrowdGroup(vg.id, vg.name, vg.owner.uid);
				mCrowdGroup.add(g);
			} else if (gType == GroupType.CONFERENCE) {
				g = new ConferenceGroup(vg.id, vg.name, vg.owner.uid,
						vg.createTime);
				mConfGroup.add(g);
			} else if (gType == GroupType.ORG) {
				g = new OrgGroup(vg.id, vg.name);
				mOrgGroup.add(g);
			} else if (gType == GroupType.CONTACT) {
				g = new ContactGroup(vg.id, vg.name);
				mContactsGroup.add(g);
			} else {
				throw new RuntimeException(" Can not support this type");
			}

			mGroupHolder.put(Long.valueOf(g.getmGId()), g);

			populateGroup(gType, g, vg.childs);
		}

	}

	public void addGroupToList(Group.GroupType gType, Group g) {
		if (gType == Group.GroupType.ORG) {
		} else if (gType == Group.GroupType.CONFERENCE) {
			mConfGroup.add(g);
		} else if (gType == Group.GroupType.CHATING) {
			this.mCrowdGroup.add(g);
		} else if (gType == Group.GroupType.CONTACT) {
			this.mContactsGroup.add(g);
		}
		mGroupHolder.put(Long.valueOf(g.getmGId()), g);
	}
	

	public Group getGroupById(Group.GroupType gType, long gId) {
		return mGroupHolder.get(Long.valueOf(gId));
	}

	private void populateGroup(GroupType gType, Group parent, Set<V2Group> list) {
		for (V2Group vg : list) {
			Group cache = mGroupHolder.get(Long.valueOf(vg.id));

			Group g = null;
			if (cache != null) {
				g = cache;
				//Update new name
				cache.setName(vg.name);
			} else {
				if (gType == GroupType.CHATING) {
					g = new CrowdGroup(vg.id, vg.name, vg.owner.uid);
				} else if (gType == GroupType.CONFERENCE) {
					g = new ConferenceGroup(vg.id, vg.name, vg.owner.uid,
							vg.createTime);
				} else if (gType == GroupType.ORG) {
					g = new OrgGroup(vg.id, vg.name);
				} else if (gType == GroupType.CONTACT) {
					g = new ContactGroup(vg.id, vg.name);
				} else {
					throw new RuntimeException(" Can not support this type");
				}
			}
			
			
			parent.addGroupToGroup(g);
			
			mGroupHolder.put(Long.valueOf(g.getmGId()), g);
			
			populateGroup(gType, g, vg.childs);

		}
	}

	/**
	 * Group information is server active call, we can't request from server
	 * directly.<br>
	 * Only way to get group information is waiting for server call.<br>
	 * So if this function return null, means service doesn't receive any call
	 * from server. otherwise server already sent group information to service.<br>
	 * If you want to know indication, please register receiver:<br>
	 * category: {@link #JNI_BROADCAST_CATEGROY} <br>
	 * action : {@link #JNI_BROADCAST_GROUP_NOTIFICATION}<br>
	 * Notice: maybe you didn't receive broadcast forever, because this
	 * broadcast is sent before you register
	 * 
	 * @param gType
	 * @return return null means server didn't send group information to
	 *         service.
	 */
	public List<Group> getGroup(Group.GroupType gType) {
		switch (gType) {
		case ORG:
			return this.mOrgGroup;
		case CONTACT:
			return mContactsGroup;
		case CHATING:
			List<Group> ct = new CopyOnWriteArrayList<Group>();
			ct.addAll(this.mCrowdGroup);
			return ct;
		case CONFERENCE:
			List<Group> confL = new ArrayList<Group>();
			confL.addAll(this.mConfGroup);
			Collections.sort(confL);
			List<Group> sortConfL = new CopyOnWriteArrayList<Group>(confL);
			return sortConfL;
		default:
			throw new RuntimeException("Unkonw type");
		}

	}

	/**
	 * Find all types of group information according to group ID
	 * 
	 * @param gid
	 * @return null if doesn't find group, otherwise return Group information
	 * 
	 * @see Group
	 */
	public Group findGroupById(long gid) {
		return mGroupHolder.get(Long.valueOf(gid));
	}

	/**
	 * Add user collections to group collections
	 * 
	 * @param gList
	 * @param uList
	 * @param belongGID
	 */
	public void addUserToGroup(List<Group> gList, List<User> uList,
			long belongGID) {
		for (Group g : gList) {
			if (belongGID == g.getmGId()) {
				g.addUserToGroup(uList);
				return;
			}
			addUserToGroup(g.getChildGroup(), uList, belongGID);
		}
	}

	public void removeGroupUser(long gid, long uid) {
		Group g = this.findGroupById(gid);
		if (g != null) {
			g.removeUserFromGroup(uid);
		}
	}

	/**
	 * Add user collections to group collections
	 * 
	 * @param gList
	 * @param uList
	 * @param belongGID
	 */
	public void addUserToGroup(List<User> uList, long belongGID) {
		Group g = findGroupById(belongGID);
		if (g == null) {
			V2Log.e("Doesn't receive group<" + belongGID + "> information yet!");
			return;
		}
		g.addUserToGroup(uList);

		// update reference for conference group
		for (Group confG : mConfGroup) {
			User u = getUser(confG.getOwner());
			if (u != null) {
				confG.setOwnerUser(u);
			}
		}
	}

	public void addUserToGroup(User u, long belongGID) {
		Group g = findGroupById(belongGID);
		if (g == null) {
			V2Log.e("Doesn't receive group<" + belongGID + "> information yet!");
			return;
		}
		g.addUserToGroup(u);
	}

	public boolean removeGroup(GroupType gType, long gid) {
		List<Group> list = null;
		if (gType == GroupType.CONFERENCE) {
			list = mConfGroup;
		} else if (gType == GroupType.CONTACT) {
			list = mContactsGroup;
		}else if (gType == GroupType.CHATING) {
			list = mCrowdGroup;
		}else if (gType == GroupType.ORG) {
			list = mOrgGroup;
		}
		for (int i = 0; i < list.size(); i++) {
			Group g = list.get(i);
			if (g.getmGId() == gid) {
				list.remove(g);
				mGroupHolder.remove(Long.valueOf(gid));
				return true;
			}
		}
		return false;
	}

	public String getAvatarPath(long uid) {
		Long key = Long.valueOf(uid);
		return this.mAvatarHolder.get(key);
	}

	public void putAvatar(long uid, String path) {
		Long key = Long.valueOf(uid);
		this.mAvatarHolder.put(key, path);
	}

	/**
	 * Get user's video device according to user id.<br>
	 * This function never return null, even through we don't receive video
	 * device data from server.
	 * 
	 * @param uid
	 *            user's id
	 * @return list of user device
	 */
	public List<UserDeviceConfig> getAttendeeDevice(long uid) {
		Set<UserDeviceConfig> list = mUserDeviceList.get(Long.valueOf(uid));
		if (list == null) {
			return null;
		}

		return new ArrayList<UserDeviceConfig>(list);
	}

	/**
	 * Update user video device and clear existed user device first
	 * 
	 * @param id
	 * @param udcList
	 */
	public void updateUserDevice(long id, List<UserDeviceConfig> udcList) {
		Long key = Long.valueOf(id);
		Set<UserDeviceConfig> list = mUserDeviceList.get(key);
		if (list != null) {
			list.clear();
		} else {
			list = new HashSet<UserDeviceConfig>();
			mUserDeviceList.put(key, list);
		}
		list.addAll(udcList);
	}

	public void setAudioState(boolean flag, long uid) {
		int st = this.mState.getState();
		if (flag) {
			st |= STATE_IN_AUDIO_CONVERSATION;
		} else {
			st &= (~STATE_IN_AUDIO_CONVERSATION);
		}
		this.mState.setState(st);
		this.mState.setUid(uid);
	}

	public void setVideoState(boolean flag, long uid) {
		int st = this.mState.getState();
		if (flag) {
			st |= STATE_IN_VIDEO_CONVERSATION;
		} else {
			st &= (~STATE_IN_VIDEO_CONVERSATION);
		}
		this.mState.setState(st);
		this.mState.setUid(uid);
	}

	public void setMeetingState(boolean flag, long gid) {
		int st = this.mState.getState();
		if (flag) {
			st |= STATE_IN_MEETING_CONVERSATION;
		} else {
			st &= (~STATE_IN_MEETING_CONVERSATION);
		}
		this.mState.setState(st);
		this.mState.setGid(gid);
	}

	public boolean isInAudioCall() {
		int st = this.mState.getState();
		return (st & STATE_IN_AUDIO_CONVERSATION) == STATE_IN_AUDIO_CONVERSATION;
	}

	public boolean isInVideoCall() {
		int st = this.mState.getState();
		return (st & STATE_IN_VIDEO_CONVERSATION) == STATE_IN_VIDEO_CONVERSATION;
	}

	public boolean isInMeeting() {
		int st = this.mState.getState();
		return (st & STATE_IN_MEETING_CONVERSATION) == STATE_IN_MEETING_CONVERSATION;
	}
	
	public GlobalState getGlobalState() {
		return new GlobalState(this.mState);
	}

	public Bitmap getUserAvatar(long id) {
		Long key = Long.valueOf(id);
		return mAvatarBmHolder.get(key);
	}

	
	
	/**
	 * Use to update cache avatar
	 */
	private BitmapManager.BitmapChangedListener bitmapChangedListener = new BitmapManager.BitmapChangedListener() {

		@Override
		public void notifyAvatarChanged(User user, Bitmap newAvatar) {
			Long key = Long.valueOf(user.getmUserId());
			Bitmap cache = mAvatarBmHolder.get(key);
			if (cache != null) {
				cache.recycle();
			}
			mAvatarBmHolder.put(key, newAvatar);
		}

	};

}
