package com.accesium.sendtopush.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class which represents the server errors.
 * 
 * @author Isidoro Castell
 * 
 */
public class PushError implements Parcelable {

	private String message;
	private Type type;

	public static final Parcelable.Creator<PushError> CREATOR = new Parcelable.Creator<PushError>() {

		@Override
		public PushError createFromParcel(Parcel source) {
			return new PushError(source);
		}

		@Override
		public PushError[] newArray(int size) {
			return new PushError[size];
		}
	};

	public PushError(String message, Type type) {
		super();
		this.message = message;
		this.type = type;
	}

	public PushError(final Parcel in) {
		this(in.readString(), (Type) in.readParcelable(Type.class.getClassLoader()));
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(message);
		dest.writeParcelable(type, 0);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * A enumerable with the errors type.
	 * 
	 * @author Isidoro Castell
	 * 
	 */
	public enum Type implements Parcelable {
		/**
		 * Some parameters are invalid or missing in the server request. Code:
		 * 40X
		 */
		INCORRECT_PARAM(400),
		/**
		 * An internal error in the server during the operation. Code: 50X
		 */
		SERVER_ERROR(500),
		/**
		 * Connection error
		 */
		CONNECTION_ERROR(-200),
		/**
		 * An error in the token request to GCM
		 */
		GET_PUSH_TOKEN_ERROR(-100),
		/**
		 * Unknown error
		 */
		UNKNOWN(-1);

		private int code;

		Type(int code) {
			this.code = code;
		}

		/**
		 * Generate a {@link Type Type} object from a integer value.
		 * 
		 * @param code
		 * @return
		 */
		public static Type fromInt(int code) {
			if (code >= SERVER_ERROR.code) {
				return SERVER_ERROR;
			} else if (code >= INCORRECT_PARAM.code) {
				return INCORRECT_PARAM;
			} else if (code == CONNECTION_ERROR.code) {
				return CONNECTION_ERROR;
			} else if (code == GET_PUSH_TOKEN_ERROR.code) {
				return GET_PUSH_TOKEN_ERROR;
			}

			return UNKNOWN;
		}

		/**
		 * @return the code
		 */
		public int getCode() {
			return code;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			dest.writeInt(ordinal());
		}

		public static final Creator<Type> CREATOR = new Creator<Type>() {
			@Override
			public Type createFromParcel(final Parcel source) {
				return Type.values()[source.readInt()];
			}

			@Override
			public Type[] newArray(final int size) {
				return new Type[size];
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PushError [message=" + message + ", type=" + type + "]";
	}

}
