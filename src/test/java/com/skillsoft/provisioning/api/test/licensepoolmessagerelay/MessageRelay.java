package com.skillsoft.provisioning.api.test.licensepoolmessagerelay;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;

@Setter
@Getter
public class MessageRelay {
    private String lpId = null;
    private String ts = null;
    private Integer seq = null;
    private JSONObject mesg = null;

    public String toJSON() {
        return new Gson().toJson(this);

    }
}
