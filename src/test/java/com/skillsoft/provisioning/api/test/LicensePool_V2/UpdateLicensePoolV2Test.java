package com.skillsoft.provisioning.api.test.LicensePool_V2;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.LicensePoolPayload;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.createLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.deleteLicensePool;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class UpdateLicensePoolV2Test extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(UpdateLicensePoolV2Test.class);
    private static final String PUT_LICENSE_POOLS_PATH = " /v2/license-pools/contract/{oldContract}/line/{oldContractLine}";
    private static final String GET_LICENSE_POOLS_PATH = " /v2/license-pools/contract/{contract}/line/{contractLine}";
    private static final String PATCH_LICENSE_POOLS_BY_KITID_PATH = " /v2/license-pools/kit/{kitId}";
    private static final String PATCH_LICENSE_POOLS_BY_CONTRACT_PATH = " /v2/license-pools/contract/{oldContract}/line/{oldContractLine}";
    private LicensePoolPayload licensePoolPayload, licensePoolPayload1;
    private static final String OLD_CONTRACT = "oldContract";
    private static final String OLD_CONTRACT_LINE = "oldContractLine";
    private static final String KIT_ID = "kitId";

    public static String updateLicensePool(int expectedCode, LicensePoolPayload licensePoolPayload, String oldContract, int oldContractLine) {
        Map<String, Object> pathParams = new HashMap();
        pathParams.put(OLD_CONTRACT, oldContract);
        pathParams.put(OLD_CONTRACT_LINE, oldContractLine);
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(PUT_LICENSE_POOLS_PATH).
                pathParams(pathParams).
                body(licensePoolPayload.toJSON()).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                put().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return response;
    }

    public static String patchLicensePoolByKitId(int expectedCode, String kitId, String patchPayload){
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(PATCH_LICENSE_POOLS_BY_KITID_PATH).
                pathParam(KIT_ID, kitId).
                body(patchPayload).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                patch().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return response;

    }

    public static String patchLicensePoolByContract(int expectedCode, String oldContract, int oldContractLine, String patchPayload){
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(PATCH_LICENSE_POOLS_BY_CONTRACT_PATH).
                pathParam(OLD_CONTRACT, oldContract).
                pathParam(OLD_CONTRACT_LINE, oldContractLine).
                body(patchPayload).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                patch().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return response;

    }

    private static String getLicensePoolFromContract(int expectedCode, String contract, int contractLine) {
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOLS_PATH).
                pathParam("contract", contract).pathParam("contractLine", contractLine).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return response;
    }

   @Ignore
    public void ensureLicensePoolPropsAreUpdated() {
        int payloadCreate = new Random().nextInt(9000) + 1000;
        licensePoolPayload = new LicensePoolPayload();
        licensePoolPayload.setContract("allcollections" + payloadCreate);
        licensePoolPayload.setContractLine(payloadCreate);
        licensePoolPayload.setKitId("qadomain" + payloadCreate);
        licensePoolPayload.setPeopleSoftOrgId("psoft" + payloadCreate);
        licensePoolPayload.setSalesForceCustId("salesforceid" + payloadCreate);
       JSONObject qualifiers = new JSONObject();
       //put locales as default
       qualifiers.put("locales", new String[]{"en"});
       licensePoolPayload.setQualifiers(qualifiers);
       String createLpResponse = createLicensePool(200, licensePoolPayload);
        log.info("****** LP is created *******");
        log.info(createLpResponse);
        int payloadUpdate = new Random().nextInt(9000) + 1000;
        licensePoolPayload1 = new LicensePoolPayload();
        licensePoolPayload1.setNewContract("updatecollections" + payloadUpdate);
        licensePoolPayload1.setNewContractLine(payloadUpdate);
        licensePoolPayload1.setNewPeopleSoftProdId("active1-PSPID-updated");
        //Kit Id should be same to avoid renewal process
        licensePoolPayload1.setKitId("qadomain" + payloadCreate);
        licensePoolPayload1.setPeopleSoftOrgId("psoft" + payloadUpdate);
        licensePoolPayload1.setSalesForceCustId("salesforceid" + payloadUpdate);
        JSONObject propsExpected = new JSONObject();
        propsExpected.put("channelRestrictionsApply", Boolean.TRUE);
        propsExpected.put("academicRestrictionsApply", Boolean.TRUE);
        propsExpected.put("internationalRestrictionsApply", Boolean.TRUE);
        licensePoolPayload1.setProps(propsExpected);
        String updateLpResponse = updateLicensePool(200, licensePoolPayload1, "allcollections" + payloadCreate, payloadCreate);
        log.info("****** LP is updated *******");
        log.info(updateLpResponse);
        assertEquals(createLpResponse, updateLpResponse);
        String response = getLicensePoolFromContract(200, "updatecollections" + payloadUpdate, payloadUpdate);
        LinkedHashMap propsActual = JsonPath.parse(response).read("$.props");
        assertEquals(propsExpected, propsActual);
        deleteLicensePool("updatecollections" + payloadUpdate, payloadUpdate, 1);
        log.info("****** Updated LP is deleted *******");


    }
}
