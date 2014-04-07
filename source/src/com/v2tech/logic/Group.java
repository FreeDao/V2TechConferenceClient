package com.v2tech.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.v2tech.util.V2Log;

/**
 * Group information
 * 
 * @author 28851274
 * 
 */
public class Group {

	protected long mGId;

	protected GroupType mGroupType;

	protected String mName;

	private long mOwner;

	private User mOwnerUser;

	private Date mCreateDate;

	protected Group mParent;

	protected List<Group> mChild;

	private List<User> users;

	protected int level;

	private Object mLock = new Object();

	public enum GroupType {
		ORG(1), CONTACT(2), CHATING(3), CONFERENCE(4), UNKNOWN(-1);

		private int type;

		private GroupType(int type) {
			this.type = type;
		}

		public static GroupType fromInt(int code) {
			switch (code) {
			case 1:
				return ORG;
			case 2:
				return CONTACT;
			case 3:
				return CHATING;
			case 4:
				return CONFERENCE;
			default:
				return UNKNOWN;

			}
		}

		public int intValue() {
			return type;
		}
	}

	public Group(long gId, GroupType groupType, String name) {
		this(gId, groupType, name, 0, null);
	}

	public Group(long gId, GroupType groupType, String name, long owner) {
		this(gId, groupType, name, owner, null);
	}

	/**
	 * 
	 * @param gId
	 * @param groupType
	 * @param name
	 * @param owner
	 * @param createDate
	 */
	public Group(long gId, GroupType groupType, String name, String owner,
			String createDate) {
		this.mGId = gId;
		this.mGroupType = groupType;
		this.mName = name;
		if (owner != null) {
			this.mOwner = Long.parseLong(owner);
		}

		if (createDate != null && createDate.trim().length() > 0) {
			this.mCreateDate = new Date(Long.parseLong(createDate) * 1000);
		}

		users = new ArrayList<User>();
		mChild = new ArrayList<Group>();
		level = 1;

	}

	public Group(long gId, GroupType groupType, String name, long owner,
			Date createDate) {
		this.mGId = gId;
		this.mGroupType = groupType;
		this.mName = name;
		this.mOwner = owner;
		this.mCreateDate = createDate;

		users = new ArrayList<User>();
		mChild = new ArrayList<Group>();
		level = 1;
	}

	public long getmGId() {
		return mGId;
	}

	public void setGId(long mGId) {
		this.mGId = mGId;
	}

	public GroupType getGroupType() {
		return mGroupType;
	}

	public void setGroupType(GroupType mGroupType) {
		this.mGroupType = mGroupType;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public long getOwner() {
		return mOwner;
	}

	public void setOwner(long mOwner) {
		this.mOwner = mOwner;
	}

	public Date getCreateDate() {
		return mCreateDate;
	}

	public String getStrCreateDate() {
		if (this.mCreateDate != null) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm",
					Locale.getDefault());
			return df.format(this.mCreateDate);
		} else {
			return null;
		}

	}

	public void setCreateDate(Date createDate) {
		this.mCreateDate = createDate;
	}

	public User getOwnerUser() {
		return mOwnerUser;
	}

	public void setOwnerUser(User mOwnerUser) {
		this.mOwnerUser = mOwnerUser;
	}

	public Group getParent() {
		return mParent;
	}

	public void setmParent(Group parent) {
		this.mParent = parent;
		level = this.getParent().getLevel() + 1;
	}

	public void addUserToGroup(User u) {
		if (u == null) {
			V2Log.e(" Invalid user data");
			return;
		}
		synchronized (mLock) {
			this.users.add(u);
		}
	}

	public List<User> getUsers() {
		return this.users;
	}

	public List<Group> getChildGroup() {
		return this.mChild;
	}

	public void addGroupToGroup(Group g) {
		if (g == null) {
			V2Log.e(" Invalid group data");
			return;
		}
		synchronized (mLock) {
			this.mChild.add(g);
			g.setmParent(this);
		}
	}

	public boolean findUser(User u, Group g) {
		for (User tu : g.getUsers()) {
			if (tu.getmUserId() == u.getmUserId()) {
				return true;
			}
		}
		for (Group subG : g.getChildGroup()) {
			boolean flag = findUser(u, subG);
			if (flag == true) {
				return flag;
			}
		}
		return false;
	}

	public List<User> searchUser(String text) {
		List<User> l = new ArrayList<User>();
		Group.searchUser(text, l, this);
		return l;
	}

	public static void searchUser(String text, List<User> l, Group g) {
		if (l == null || g == null) {
			return;
		}
		for (User u : g.getUsers()) {
			if (u.getName().contains(text)) {
				l.add(u);
			}
		}
		for (Group subG : g.getChildGroup()) {
			searchUser(text, l, subG);
		}
	}

	// FIXME need to be optimize
	public int getOnlineUserCount() {
		// To make sure that prevent update list when iterating
		synchronized (mLock) {
			return getUserOnlineCount(this);
		}
	}

	private int getUserOnlineCount(Group g) {
		int c = 0;
		if (g == null) {
			return 0;
		}
		List<User> l = g.getUsers();
		for (int i = 0; i < l.size(); i++) {
			User u = l.get(i);
			if (u.getmStatus() == User.Status.ONLINE
					|| u.getmStatus() == User.Status.BUSY
					|| u.getmStatus() == User.Status.DO_NOT_DISTURB
					|| u.getmStatus() == User.Status.LEAVE) {
				c++;
			}
		}

		for (Group subG : g.getChildGroup()) {
			c += getUserOnlineCount(subG);
		}
		return c;
	}

	public int getUserCount() {
		return getUserCount(this);
	}

	private int getUserCount(Group g) {
		int c = g.getUsers().size();
		for (Group subG : g.getChildGroup()) {
			c += getUserCount(subG);
		}
		return c;
	}

	public void addUserToGroup(List<User> l) {
		synchronized (mLock) {
			for (User u : l) {
				this.users.add(u);
				u.addUserToGroup(this);
			}
		}
	}

	public int getLevel() {
		return level;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mGId ^ (mGId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (mGId != other.mGId)
			return false;
		return true;
	}

}
