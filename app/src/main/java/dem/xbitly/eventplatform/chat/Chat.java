package dem.xbitly.eventplatform.chat;

public class Chat {
    String name;
    String privacy;

    public Chat(String name, String privacy){
        this.name = name;
        this.privacy = privacy;
    }

    public Chat(){

    }

    public String getName(){return name;}

    public void setName(String name){this.name=name;}

    public String getPrivacy(){return privacy;}

    public void setPrivacy(String privacy){this.privacy=privacy;}
}
