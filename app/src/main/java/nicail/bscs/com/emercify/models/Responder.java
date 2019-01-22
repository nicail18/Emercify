package nicail.bscs.com.emercify.models;

public class Responder {

    private String user_id;
    private boolean isLegit;
    private String responder_id;

    public Responder(String user_id, boolean isLegit, String responder_id) {
        this.user_id = user_id;
        this.isLegit = isLegit;
        this.responder_id = responder_id;
    }

    public Responder() {
    }

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

    @Override
    public String toString() {
        return "Responder{" +
                "user_id='" + user_id + '\'' +
                ", isLegit=" + isLegit +
                ", responder_id='" + responder_id + '\'' +
                '}';
    }
}
