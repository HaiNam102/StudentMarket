package com.postgresql.StudentMarket.Controllers;

import com.google.gson.JsonObject;
import com.postgresql.StudentMarket.Services.GoogleService;
import com.postgresql.StudentMarket.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class OAuthController {

    @Autowired
    private GoogleService googleService;

    @Autowired
    private UserService userService;

    @GetMapping("/login/oauth2/code/google")
    public String googleCallback(@RequestParam("code") String code) throws IOException {

        System.out.println("DEBUG: Google callback code = " + code);

        String token = googleService.getToken(code);
        JsonObject userInfo = googleService.getUserInfo(token);

        String googleId = userInfo.get("id").getAsString();
        String email = userInfo.get("email").getAsString();
        String name = userInfo.get("name").getAsString();
        String picture = userInfo.has("picture") ? userInfo.get("picture").getAsString() : null;
        boolean verifiedEmail = userInfo.get("verified_email").getAsBoolean();

        userService.processOAuthPostLogin(googleId, email, name, picture, verifiedEmail);

        return "redirect:/StudentMarket";
    }
}
