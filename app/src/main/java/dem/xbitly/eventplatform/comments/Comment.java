package dem.xbitly.eventplatform.comments;

public class Comment {

    private String username, text, date;


    public Comment(String username, String text, String date){
        this.username = username;
        this.text = text;
        this.date = date;
    }


    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }
}
