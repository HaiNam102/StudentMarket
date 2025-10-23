package com.postgresql.StudentMarket.Services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.postgresql.StudentMarket.Util.Iconstant;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class GoogleService {

    public String getToken(String code) throws IOException {
        String response = Request.Post(Iconstant.GOOGLE_LINK_GET_TOKEN)
                .bodyForm(
                        Form.form()
                                .add("client_id", Iconstant.GOOGLE_CLIENT_ID)
                                .add("client_secret", Iconstant.GOOGLE_CLIENT_SECRET)
                                .add("redirect_uri", Iconstant.GOOGLE_REDIRECT_URI)
                                .add("code", code)
                                .add("grant_type", Iconstant.GOOGLE_GRANT_TYPE)
                                .build()
                )
                .execute()
                .returnContent()
                .asString();

        JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
        return jobj.get("access_token").getAsString();
    }

    public JsonObject getUserInfo(String accessToken) throws IOException {
        String url = "https://www.googleapis.com/oauth2/v2/userinfo";

        // ✅ Dùng URI thay vì new URL(String) để tránh deprecated warning
        URL requestUrl = URI.create(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            return new Gson().fromJson(response.toString(), JsonObject.class);
        }
    }
}
