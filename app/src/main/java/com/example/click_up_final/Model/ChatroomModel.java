package com.example.click_up_final.Model;

import java.util.HashMap;
import java.util.Map;

public class ChatroomModel {
    public String openChat_Title;
    public String openChat_Memo;
    public String makeUserNickname;
    public String openChat_latitude;
    public String openChat_longitude;
    public String openChat_createdAt;
    public String makeUserImage;
    public String makeUserUID;

    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, ChatModel.Comment> comments = new HashMap<>();

    public static class Comment {
        public String uid;
        public String message;
        public String nick;
        public String url;
    }

}
