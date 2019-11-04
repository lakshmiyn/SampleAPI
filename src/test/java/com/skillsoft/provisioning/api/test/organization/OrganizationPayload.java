package com.skillsoft.provisioning.api.test.organization;

import com.google.gson.Gson;
import lombok.Setter;
import net.minidev.json.JSONObject;

@Setter
public class OrganizationPayload {

    private String peopleSoftId = null;
    private String salesForceCustId = null;
    private String orgName = null;
    private String domain = null;
    private String orgType = "client";
    private int contractValueUSD = 1;
    private JSONObject props = null;
    //(JSONObject)new JSONObject().put("PINTS", "HYD");
    private JSONObject contactInfo = (JSONObject) new JSONObject().put("contactEmail", "contact@email.qa");
    private JSONObject supportInfo = (JSONObject) new JSONObject().put("supportEmail", "support@email.qa");
    private JSONObject compliance = null;

    public String toJSON() {
        return new Gson().toJson(this);

    }
}
