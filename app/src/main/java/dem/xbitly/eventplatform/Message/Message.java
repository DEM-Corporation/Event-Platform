package dem.xbitly.eventplatform.Message;

import java.util.ArrayList;

public class Message {
    String text;
    String from;
    String userID;
    String time;

    public Message (String text, String from, String userID, String time){
        this.text = text;
        this.from = from;
        this.userID = userID;
        this.time = time;
    }

    public Message(){

    }

    public String getText(){return text;}

    public void setText(String text){this.text=text;}

    public String getFrom(){return from;}

    public void setFrom(String from){this.from=from;}

    public String getUserID(){return userID;}

    public void setUserID(String userID){this.userID=userID;}

    public String getTime(){return time;}

    public void setTime(String time){this.time=time;}
}
