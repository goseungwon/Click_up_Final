package com.example.click_up_final.Model;

import java.util.HashMap;
import java.util.Map;

public class WriteDTO {
    public String imageURL;
    public String imageName;
    public String title;
    public String content;
    public String uid;
    public String userid;
    public String latitude;
    public String longigude;
    public String createdAt;
    public String key;

    public int likecount = 0;
    public Map<String, Boolean> likemembers = new HashMap<>();

}
