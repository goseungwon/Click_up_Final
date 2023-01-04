package com.gsw.click_up_final.Model;

import java.util.HashMap;
import java.util.Map;

public class FriendRequsetDTO {
    public Map<String, Boolean> friends = new HashMap<>();
    public String request_nickname;
    public String request_url;
    public String request_uid;

    public Map<String, Integer> posts = new HashMap<>();
}
