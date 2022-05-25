package com.example.click_up_final.Model;

import java.util.HashMap;
import java.util.Map;

public class OpenChatModel {
    public Map<String, Boolean> users = new HashMap<>();
    public Map<String, ChatModel.Comment> comments = new HashMap<>();

    public static class Comment {
        public String uid;
        public String message;
        public String nick;
        public String url;
        public Map<String, Object> readUsers = new HashMap<>();
    }
}
