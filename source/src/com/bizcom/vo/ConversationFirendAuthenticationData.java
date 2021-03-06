package com.bizcom.vo;

public class ConversationFirendAuthenticationData extends Conversation {

	private VerificationMessageType messageType;

	public ConversationFirendAuthenticationData(int mType, long mExtId) {
		super(mType, mExtId);
		messageType = VerificationMessageType.CONTACT_TYPE;
	}

	public VerificationMessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(VerificationMessageType messageType) {
		this.messageType = messageType;
	}

	public enum VerificationMessageType {
		CROWD_TYPE(0), CONTACT_TYPE(1), UNKNOWN(2);

		private int type;

		private VerificationMessageType(int type) {
			this.type = type;
		}

		public static VerificationMessageType fromInt(int code) {
			switch (code) {
			case 0:
				return CROWD_TYPE;
			case 1:
				return CONTACT_TYPE;
			default:
				return UNKNOWN;

			}
		}

		public int intValue() {
			return type;
		}
	}
}
