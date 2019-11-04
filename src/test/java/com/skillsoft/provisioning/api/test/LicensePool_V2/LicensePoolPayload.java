package com.skillsoft.provisioning.api.test.LicensePool_V2;

import com.google.gson.Gson;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import lombok.Setter;
import net.minidev.json.JSONObject;

import java.util.UUID;

@Setter
public class LicensePoolPayload {
    private transient SuiteData suiteData = SuiteData.getData();
    private String contract = null;
    private Integer contractLine = null;
    private String oldContract = null;
    private Integer oldContractLine = null;
    private String newContract = null;
    private Integer newContractLine = null;
    private String peopleSoftProdId = "active1-PSPID";
    private String newPeopleSoftProdId = null;
    private Integer orderNumber = 1;
    private String kitId = null;
    private String oldKitId = null;
    private String collectionId = "CRS_DigitalSkills";
    private Integer seats = 295;
    private String lineStartDate = "2018-01-01T00:00:00.000Z";
    private String lineEndDate = "2040-01-01T00:00:00.000Z";
    private String licenseExpDate = "2040-01-01T00:00:00.000Z";
    private String peopleSoftOrgId = null;
    private String salesForceCustId = null;
    private String customerName = "PINTS HYD";
    private UUID orgId = suiteData.getOrgId();
    private Integer contractValueUSD = 1;
    private Integer contractValue = null;
    private JSONObject props = null;
    private JSONObject qualifiers=null;
    private Boolean royaltyRestrictionsApply = null;
    //(JSONObject)new JSONObject().put("PINTS", "HYD");



    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}
