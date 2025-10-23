package com.postgresql.StudentMarket.Util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.fluent.Request;
import java.io.IOException;
import com.postgresql.StudentMarket.Entities.User;

public class GoogleUtils {

    public static User getUserInfo(final String accessToken) throws IOException {
        // Link request thông tin user từ Google
        String link = Iconstant.GOOGLE_LINK_GET_USER_INFO + "?access_token=" + accessToken;

        // Gửi request và nhận response JSON
        String response = Request.Get(link).execute().returnContent().asString();

        // Parse JSON
        JsonObject json = new Gson().fromJson(response, JsonObject.class);

        // Map sang entity User
        User user = new User();
        user.setGoogleId(json.get("sub").getAsString());   // Google ID
        user.setEmail(json.get("email").getAsString());
        user.setFullName(json.get("name").getAsString());  // Hoặc tách given_name + family_name
        user.setPicture(json.get("picture").getAsString());
        user.setVerifiedEmail(json.get("email_verified").getAsBoolean());
        user.setProvider("GOOGLE");

        return user;
    }
}
