package com.bizcom.vo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.V2.jni.util.EscapedcharactersProcessing;
import com.V2.jni.util.V2Log;
import com.bizcom.util.BitmapUtil;
import com.bizcom.vc.application.GlobalHolder;
import com.bizcom.vc.application.V2GlobalConstants;

public class User implements Comparable<User> {

	// Server transfer fields
	// 通过OnGetGroupUserInfo传来
	// 登录用的帐号字符串
	private String mAccount;
	private String mAddress;
	private int mAuthtype = 0;// 取值0允许任何人，1需要验证，2不允许任何人
	private Date mBirthday;
	private String mStringBirthday;
	// bsystemavatar='1'
	private String mEmail;
	private String mFax;
	// homepage='http://wenzongliang.com'
	private long mUserId;
	private String mJob;
	private String mMobile;

	// privacy='0'
	private String mSex;
	private String mSignature;
	private String mTelephone;

	// group
	private String mCompany;
	private String mDepartment;
	// end Server transfer fields
	// custom fields
	private boolean isCurrentLoggedInUser;
	private NetworkStateCode mResult;
	private DeviceType mType;
	private Status mStatus;
	// 登录后显示的昵称
	private String mNickName;
	private String mCommentName;

	private Set<Group> mBelongsGroup;
	private String mAvatarPath;
	private String abbra;
	// For GroupMemberActivity 
	public boolean isShowDelete;
	private static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
	// This value indicate this object is dirty, construct locally without any
	// user information
	private boolean isFromService;
	// 会议中的快速入会用户
	private boolean isRapidInitiation = false;
	public boolean isContain;

	public User(long mUserId) {
		this(mUserId, null, null, null);
	}

	public User(long mUserId, String nickName) {
		this(mUserId, nickName, null, null);
	}

	private User(long mUserId, String name, String email, String signature) {
		this.mUserId = mUserId;
		this.mNickName = name;
		this.mEmail = email;
		this.mSignature = signature;
		mBelongsGroup = new CopyOnWriteArraySet<Group>();
		isCurrentLoggedInUser = false;
		this.mStatus = Status.OFFLINE;
		initAbbr();
		this.isFromService = false;
	}

	private void initAbbr() {
		abbra = "";
		if (this.mNickName != null) {
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			char[] cs = this.mNickName.toCharArray();
			for (char c : cs) {
				try {
					String[] ars = PinyinHelper.toHanyuPinyinStringArray(c,
							format);
					if (ars != null && ars.length > 0) {
						abbra += ars[0].charAt(0);
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			}
			if (abbra.equals("")) {
				abbra = this.mNickName.toLowerCase(Locale.getDefault());
			}
		}
	}

	public boolean isCurrentLoggedInUser() {
		return isCurrentLoggedInUser;
	}

	public void setCurrentLoggedInUser(boolean isCurrentLoggedInUser) {
		this.isCurrentLoggedInUser = isCurrentLoggedInUser;
	}

	public long getmUserId() {
		return mUserId;
	}

	public void setmUserId(long mUserId) {
		this.mUserId = mUserId;
	}

	public NetworkStateCode getmResult() {
		return mResult;
	}

	public void setmResult(NetworkStateCode mResult) {
		this.mResult = mResult;
	}

	public String getNickName() {
		return mNickName;
	}

	public String getDisplayName() {
		boolean isFriend = GlobalHolder.getInstance().isFriend(this);
		if (isFriend && !TextUtils.isEmpty(mCommentName))
			return mCommentName;
		else
			return mNickName;
	}

	public void setNickName(String nickName) {
		this.mNickName = nickName;
		initAbbr();
	}

	public String getmEmail() {
		return mEmail;
	}

	public void setEmail(String mail) {
		this.mEmail = mail;
	}

	public String getSignature() {
		return mSignature;
	}

	public DeviceType getDeviceType() {
		return mType;
	}

	public void setDeviceType(DeviceType type) {
		this.mType = type;
	}

	public void setSignature(String signature) {
		this.mSignature = signature;
	}

	public Set<Group> getBelongsGroup() {
		return mBelongsGroup;
	}

	public void setmBelongsGroup(Set<Group> belongsGroup) {
		this.mBelongsGroup = belongsGroup;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	public String getMobile() {
		return mMobile;
	}

	public void setMobile(String mCellPhone) {
		this.mMobile = mCellPhone;
	}

	public String getCompany() {
		if (TextUtils.isEmpty(mCompany)) {
			mCompany = loadCompany(this.getFirstBelongsGroup());
		}
		return mCompany;
	}

	private String loadCompany(Group g) {
		if (g == null) {
			return "";
		}
		if (g.getParent() != null) {
			return loadCompany(g.getParent());
		} else {
			List<Group> contacts = GlobalHolder.getInstance().getGroup(
					V2GlobalConstants.GROUP_TYPE_CONTACT);
			if (contacts.contains(g))
				return "";
			else
				return g.getName();
		}
	}

	public void setCompany(String mCompany) {
		this.mCompany = mCompany;
	}

	public String getDepartment() {
		// FIXME first group is real department
		Group g = this.getFirstBelongsGroup();
		if (g != null) {
			if (g.getParent() == null) {
				mDepartment = "";
			} else {
				mDepartment = g.getName();
			}
		}
		return mDepartment;
	}

	public void setDepartment(String mDepartment) {
		this.mDepartment = mDepartment;
	}

	public String getSex() {
		return mSex;
	}

	public void setSex(String mGender) {
		this.mSex = mGender;
	}

	public Date getBirthday() {
		return mBirthday;
	}

	public String getBirthdayStr() {
		if (mBirthday != null) {
			DateFormat sd = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
			return sd.format(mBirthday);
		} else {
			return mStringBirthday;
		}
	}

	public String getmStringBirthday() {
		return mStringBirthday;
	}

	public void setmStringBirthday(String mStringBirthday) {
		this.mStringBirthday = mStringBirthday;
	}

	public void setBirthday(Date mBirthday) {
		this.mBirthday = mBirthday;
	}

	public String getTelephone() {
		return mTelephone;
	}

	public void setTelephone(String mTelephone) {
		this.mTelephone = mTelephone;
	}

	public String getJob() {
		return mJob;
	}

	public void setJob(String mJob) {
		this.mJob = mJob;
	}

	public Status getmStatus() {
		return mStatus;
	}

	public String getArra() {
		return this.abbra;
	}

	public void updateStatus(Status mStatus) {
		this.mStatus = mStatus;
	}

	public String getFax() {
		return this.mFax;
	}

	public void setFax(String fax) {
		this.mFax = fax;
	}

	public int getAuthtype() {
		return this.mAuthtype;
	}

	public void setAuthtype(int authtype) {
		this.mAuthtype = authtype;
	}

	public String getAccount() {
		return this.mAccount;
	}

	public void setAccount(String acc) {
		this.mAccount = acc;
	}

	public void addUserToGroup(Group g) {
		if (g == null) {
			V2Log.e(" group is null , can't add user to this group");
			return;
		}
		this.mBelongsGroup.add(g);
	}

	public void removeUserFromGroup(Group g) {
		if (g == null) {
			V2Log.e(" group is null , can't remove user to this group");
			return;
		}
		this.mBelongsGroup.remove(g);
	}

	public String getCommentName() {
		return mCommentName;
	}

	public void setCommentName(String comentName) {
		this.mCommentName = comentName;
	}

	public boolean isFromService() {
		return isFromService;
	}

	public void setFromService(boolean isFromService) {
		this.isFromService = isFromService;
	}

	public Group getFirstBelongsGroup() {
		if (this.mBelongsGroup.size() > 0) {
			for (Group g : mBelongsGroup) {
				if (g.getGroupType() != Group.GroupType.CONFERENCE) {
					return g;
				}
			}
		}
		return null;
	}

	public String getAvatarPath() {
		return mAvatarPath;
	}

	public void setAvatarPath(String avatarPath) {
		this.mAvatarPath = avatarPath;
	}

	private Bitmap avatar;

	public Bitmap getAvatarBitmap() {
		if (avatar == null) {
			avatar = GlobalHolder.getInstance().getUserAvatar(this.mUserId);
			if (avatar == null) {
				// FIXME if get null from loadAvatarFromPath
				return loadAvatarBitmap(mAvatarPath);
			} else {
				if (avatar.isRecycled())
					return loadAvatarBitmap(mAvatarPath);
			}
		} else if (avatar.isRecycled()) {
			avatar = GlobalHolder.getInstance().getUserAvatar(this.mUserId);
			if (avatar == null) {
				return loadAvatarBitmap(mAvatarPath);
			} else {
				if (avatar.isRecycled())
					return loadAvatarBitmap(mAvatarPath);
			}
		}
		return avatar;
	}

	private Bitmap loadAvatarBitmap(String mAvatarPath) {
		if (mAvatarPath == null) {
			mAvatarPath = GlobalHolder.getInstance().getAvatarPath(mUserId);
		}
		Bitmap bitmap = BitmapUtil.loadAvatarFromPath(mAvatarPath);
		if (bitmap != null) {
			if (!bitmap.isRecycled())
				return bitmap;
			else
				throw new RuntimeException(
						"User loadAvatarBitmap --> Loading avatar from file path faield... bitmap is recycled!");

		} else
			throw new RuntimeException(
					"User loadAvatarBitmap --> Loading avatar from file path faield... bitmap is null!");
	}

	public void setAvatarBitmap(Bitmap bm) {
		this.avatar = bm;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (mUserId != other.mUserId)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mUserId ^ (mUserId >>> 32));
		return result;
	}

	@Override
	public int compareTo(User another) {
		// make sure current user align first position
		if (this.mUserId == GlobalHolder.getInstance().getCurrentUserId()) {
			return -1;
		}
		if (another.getmUserId() == GlobalHolder.getInstance()
				.getCurrentUserId()) {
			return 1;
		}

		if (another.getmStatus() == this.mStatus) {
			return this.abbra.compareTo(another.abbra);
		}

		if (this.mStatus == Status.ONLINE || this.mStatus == Status.LEAVE
				|| this.mStatus == Status.DO_NOT_DISTURB
				|| this.mStatus == Status.BUSY) {
			if (another.mStatus == Status.ONLINE
					|| another.getmStatus() == Status.LEAVE
					|| another.getmStatus() == Status.DO_NOT_DISTURB
					|| another.getmStatus() == Status.BUSY) {
				return this.abbra.compareTo(another.abbra);
			} else {
				return -1;
			}
		} else if (another.mStatus == Status.ONLINE
				|| another.getmStatus() == Status.LEAVE
				|| another.getmStatus() == Status.DO_NOT_DISTURB
				|| another.getmStatus() == Status.BUSY) {
			return 1;
		}

		return this.abbra.compareTo(another.abbra);
	}

	public String toXml() {
		DateFormat dp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String xml = "<user " + " address='"
				+ (this.getAddress() == null ? "" : EscapedcharactersProcessing
						.convert(this.getAddress()))
				+ "' "
				+ "authtype='"
				+ this.getAuthtype()
				+ "' "
				+ "birthday='"
				+ (this.mBirthday == null ? "" : dp.format(this.mBirthday))
				+ "' "
				+ "job='"
				+ (this.getJob() == null ? "" : this.getJob())
				+ "' "
				+ "mobile='"
				+ (this.getMobile() == null ? "" : EscapedcharactersProcessing
						.convert(this.getMobile()))
				+ "' "
				+ "nickname='"
				+ (this.getDisplayName() == null ? ""
						: EscapedcharactersProcessing.convert(this
								.getDisplayName()))
				+ "'  "
				+ "sex='"
				+ (this.getSex() == null ? "" : this.getSex())
				+ "'  "
				+ "sign='"
				+ (this.getSignature() == null ? ""
						: EscapedcharactersProcessing.convert(this
								.getSignature())) + "' " + "telephone='"
				+ (this.getTelephone() == null ? "" : this.getTelephone())
				+ "'> " + "<videolist/> </user> ";
		return xml;
	}

	/**
	 * 
	 * @param xml
	 *            <xml><user account='wenzl1' address='地址' authtype='1'
	 *            birthday='1997-12-30' bsystemavatar='1'
	 *            email='youxiang@qww.com' fax='22222'
	 *            homepage='http://wenzongliang.com' id='130' job='职务'
	 *            mobile='18610297182' nickname='显示名称' privacy='0' sex='1'
	 *            sign='签名' telephone='03702561038'/></xml>
	 * @return
	 */
	public static List<User> paserXml(String xml) {
		List<User> l = new ArrayList<User>();

		InputStream is = null;

		DateFormat dp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			NodeList gList = doc.getElementsByTagName("user");
			Element element;
			for (int i = 0; i < gList.getLength(); i++) {
				element = (Element) gList.item(i);
				String strId = element.getAttribute("id");
				if (strId == null || strId.isEmpty()) {
					continue;
				}

				User u = new User(Long.parseLong(strId));

				u.setNickName(getAttribute(element, "nickname"));
				u.setCommentName(getAttribute(element, "commentname"));

				u.setAccount(getAttribute(element, "account"));
				u.setSignature(getAttribute(element, "sign"));
				u.setSex(getAttribute(element, "sex"));
				u.setTelephone(getAttribute(element, "telephone"));
				u.setMobile(getAttribute(element, "mobile"));

				u.setFax(getAttribute(element, "fax"));
				u.setJob(getAttribute(element, "job"));

				u.setEmail(getAttribute(element, "email"));
				u.setAddress(getAttribute(element, "address"));
				u.setmStringBirthday(getAttribute(element, "birthday"));

				String authType = getAttribute(element, "authtype");
				if (authType == null) {
					u.setAuthtype(0);
				} else {
					u.setAuthtype(Integer.parseInt(authType));
				}
				u.setFromService(true);
				l.add(u);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return l;
	}

	private static String getAttribute(Element el, String name) {
		Attr atr = el.getAttributeNode(name);
		if (atr != null) {
			return EscapedcharactersProcessing.reverse(atr.getValue());
		}
		return null;
	}

	public boolean isRapidInitiation() {
		return isRapidInitiation;
	}

	public void setRapidInitiation(boolean isRapidInitiation) {
		this.isRapidInitiation = isRapidInitiation;
	}

	public enum DeviceType {
		CELL_PHONE(2), PC(1), UNKNOWN(-1);
		private int code;

		private DeviceType(int code) {
			this.code = code;
		}

		public int toIntValue() {
			return code;
		}

		public static DeviceType fromInt(int type) {
			switch (type) {
			case 1:// pc
				return PC;
			case 2:// 安卓
			case 3:// IOS
			case 4:// sip,h323
				return CELL_PHONE;
			default:
				return UNKNOWN;
			}
		}
	}

	public enum Status {
		LEAVE(2), BUSY(3), DO_NOT_DISTURB(4), HIDDEN(5), ONLINE(1), OFFLINE(0), UNKNOWN(
				-1);
		private int code;

		private Status(int code) {
			this.code = code;
		}

		public int toIntValue() {
			return code;
		}

		public static Status fromInt(int status) {
			switch (status) {
			case 0:
				return OFFLINE;
			case 1:
				return ONLINE;
			case 2:
				return LEAVE;
			case 3:
				return BUSY;
			case 4:
				return DO_NOT_DISTURB;
			case 5:
				return HIDDEN;
			default:
				return UNKNOWN;
			}
		}
	}

}
