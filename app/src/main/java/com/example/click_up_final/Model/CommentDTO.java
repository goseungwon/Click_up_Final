package com.example.click_up_final.Model;

import java.util.HashMap;
import java.util.Map;

public class CommentDTO {

        public String nick;
        public String comments;
        public String url;

        public int like = 0;
        public Map<String, Boolean> users = new HashMap<>();
}
