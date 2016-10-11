package fhict.design4nature.levendstratego.fcm;

/**
 * Created by fhict.
 */
class NotificationData {

    public static final String TEXT = "TEXT";

    private final int id;
    private final String title;
    private final String textMessage;


    public NotificationData(int id, String title, String textMessage) {
        this.id = id;
        this.title = title;
        this.textMessage = textMessage;
    }


    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTextMessage() {
        return textMessage;
    }



}