package nicail.bscs.com.emercify.models;

public class Message {

    private static final String TAG = "Message";

    private String message_id, user_id, conversation_id, date_send, date_created, message;

    public Message(String message_id, String user_id, String conversation_id, String date_send, String date_created, String message) {
        this.message_id = message_id;
        this.user_id = user_id;
        this.conversation_id = conversation_id;
        this.date_send = date_send;
        this.date_created = date_created;
        this.message = message;
    }

    public Message() {
        
    }

    public String getConversation_id() {
        return conversation_id;
    }

    public void setConversation_id(String conversation_id) {
        this.conversation_id = conversation_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDate_send() {
        return date_send;
    }

    public void setDate_send(String date_send) {
        this.date_send = date_send;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message_id='" + message_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", conversation_id='" + conversation_id + '\'' +
                ", date_send='" + date_send + '\'' +
                ", date_created='" + date_created + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
