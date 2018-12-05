package nicail.bscs.com.emercify.models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    private String user_id;
    private long phone_number;
    private String username;
    private String email;
    private String device_token;
    private boolean online_status;

    public User(String user_id, long phone_number,
                String username, String email, String device_token, boolean online_status) {
        this.user_id = user_id;
        this.phone_number = phone_number;
        this.username = username;
        this.email = email;
        this.device_token = device_token;
        this.online_status = online_status;
    }

    public User() {
    }

    protected User(Parcel in) {
        user_id = in.readString();
        phone_number = in.readLong();
        username = in.readString();
        email = in.readString();
        device_token = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public boolean isOnline_status() {
        return online_status;
    }

    public void setOnline_status(boolean online_status) {
        this.online_status = online_status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeLong(phone_number);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(device_token);
        dest.writeByte((byte) (online_status ? 1 : 0));
    }
}
