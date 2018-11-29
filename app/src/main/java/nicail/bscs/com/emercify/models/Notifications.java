package nicail.bscs.com.emercify.models;

public class Notifications {

    private String user_id;
    private String notification_id;
    private String type;
    private String timestamp;
    private String from_id;

    public Notifications(String user_id, String notification_id, String type, String timestamp, String from_id) {
        this.user_id = user_id;
        this.notification_id = notification_id;
        this.type = type;
        this.timestamp = timestamp;
        this.from_id = from_id;
    }

    public Notifications() {
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
                '}';
    }
}
