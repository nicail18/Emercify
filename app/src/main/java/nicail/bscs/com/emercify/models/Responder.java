package nicail.bscs.com.emercify.models;

public class Responder {

    private String user_id;
    private boolean isLegit;

    public Responder(String user_id, boolean isLegit) {
        this.user_id = user_id;
        this.isLegit = isLegit;
    }

    public Responder(String user_id) {
        this.user_id = user_id;
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

    @Override
    public String toString() {
        return "Responder{" +
                "user_id='" + user_id + '\'' +
                ", isLegit=" + isLegit +
                '}';
    }
}
