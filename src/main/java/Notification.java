public class Notification {
    private int notifyID;
    private int userID;
    private String message;
    private String createdAt;
    private int readStatus;

    public Notification(int notifyID, int userID, String message, String createdAt, int readStatus) {
        this.notifyID = notifyID;
        this.userID = userID;
        this.message = message;
        this.createdAt = createdAt;
        this.readStatus = readStatus;
    }

    public int getNotifyID() { return notifyID; }
    public int getUserID() { return userID; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
    public int getReadStatus() { return readStatus; }

    public void setReadStatus(int readStatus) { this.readStatus = readStatus; }
}
