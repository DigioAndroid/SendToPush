package com.accesium.sendtopush.datatypes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a task for our server (register/unregister)
 * 
 * @author Isidoro Castell
 * 
 */
public enum ServerTask implements Parcelable {
	/**
	 * Register in the server
	 */
	REGISTER,
	/**
	 * Unregister in the server
	 */
	UNREGISTER;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(ordinal());
	}

	public static final Creator<ServerTask> CREATOR = new Creator<ServerTask>() {
		@Override
		public ServerTask createFromParcel(final Parcel source) {
			return ServerTask.values()[source.readInt()];
		}

		@Override
		public ServerTask[] newArray(final int size) {
			return new ServerTask[size];
		}
	};
}
