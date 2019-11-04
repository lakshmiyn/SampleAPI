package com.skillsoft.provisioning.api.test.fulfillmentmessagerelay;

import lombok.Getter;
import lombok.Setter;
import net.minidev.json.JSONObject;

@Setter
@Getter
public class FulfillmentMessage {
    private String fulfillmentId = null;
    private String ts = null;
    private Integer seq = null;
    private JSONObject mesg = null;
}
