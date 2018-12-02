package nicail.bscs.com.emercify.models;

public class Notifications {

    private String user_id;
    private String notification_id;
    private String type;
    private String timestamp;
    private String from_id;
    private String message;
    private boolean status_seen;
    private String activity_id;

    public Notifications(String user_id, String notification_id,
                         String type, String timestamp, String from_id,
                         String message, boolean status_seen, String activity_id) {
        this.user_id = user_id;
        this.notification_id = notification_id;
        this.type = type;
        this.timestamp = timestamp;
        this.from_id = from_id;
        this.message = message;
        this.status_seen = status_seen;
        this.activity_id = activity_id;
    }

    public Notifications() {
    }

    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
    }

    public boolean isStatus_seen() {
        return status_seen;
    }

    public void setStatus_seen(boolean status_seen) {
        this.status_seen = status_seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getNotification_id() {
        return notification_id;
    }

    public void setNotification_id(String notification_id) {
        this.notification_id = notification_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    @Override
    public String toString() {
        return "Notifications{" +
                "user_id='" + user_id + '\'' +
                ", notification_id='" + notification_id + '\'' +
                ", type='" + type + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", from_id='" + from_id + '\'' +
                ", message='" + message + '\'' +
                ", status_seen=" + status_seen +
                '}';
    }
}
