package com.skillsoft.provisioning.api.test.authconnection;

import com.google.gson.Gson;
import lombok.Setter;

@Setter
public class ConnectionPayload {
    private String strategy = "username_password";


    public String toJSON() {
        return new Gson().toJson(this);

    }
}
