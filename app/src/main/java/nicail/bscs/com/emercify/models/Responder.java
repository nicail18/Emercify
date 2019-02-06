package nicail.bscs.com.emercify.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Responder implements Parcelable {

    private String user_id;
    private boolean isLegit;
    private String responder_id;
    private String status;

    public Responder(String user_id, boolean isLegit, String responder_id, String status) {
        this.user_id = user_id;
        this.isLegit = isLegit;
        this.responder_id = responder_id;
        this.status = status;
    }

    public Responder() {
    }

    protected Responder(Parcel in) {
        user_id = in.readString();
        isLegit = in.readByte() != 0;
        responder_id = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeByte((byte) (isLegit ? 1 : 0));
        dest.writeString(responder_id);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Responder> CREATOR = new Creator<Responder>() {
        @Override
        public Responder createFromParcel(Parcel in) {
            return new Responder(in);
        }

        @Override
        public Responder[] newArray(int size) {
            return new Responder[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public boolean isLegit() {
        return isLegit;
    }

    public void setLegit(boolean legit) {
        isLegit = legit;
    }

    public String getResponder_id() {
        return responder_id;
    }

    public void setResponder_id(String responder_id) {
        this.responder_id = responder_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Responder{" +
                "user_id='" + user_id + '\'' +
                ", isLegit=" + isLegit +
                ", responder_id='" + responder_id + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
