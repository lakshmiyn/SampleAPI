package com.skillsoft.provisioning.api.test.fulfillmentmessagerelay;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.LicensePoolPayload;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.createLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.deleteLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.UpdateLicensePoolTest.patchLicensePoolByContract;
import static com.skillsoft.provisioning.api.test.licensepool.UpdateLicensePoolTest.updateLicensePool;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

/*
Fulfillment messages are created only when adminUserCreated = true
Which implies authConnectionCreated = true
In properties file, orgId specified satisfies this condition
 */

public class FulfillmentMessageRelayTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(FulfillmentMessageRelayTest.class);
    private static final String GET_FULFILLMENT_MESSAGES = " /v1/fulfillment-mesgs/";
    private static final String POST_FULFILLMENT_MESSAGES = " /v1/fulfillment-mesgs/ack/id/{id}/seq/{seq}";
    private static final List<Integer> contractItems = new ArrayList<Integer>();
    private LicensePoolPayload licensePoolPayload;

    private int acknowledgeFulfillmentMessages(String id, int seq) {
        Map<String, Serializable> queryParams = new HashMap<String, Serializable>();
        queryParams.put("id", id);
        queryParams.put("seq", seq);
        return given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(POST_FULFILLMENT_MESSAGES).
                header("Authorization", "Bearer " + suiteData.getRelayJwt()).
                pathParams(queryParams).
                contentType(ContentType.JSON).
                when().
                post().
                then().
                extract().statusCode();

    }

    private FulfillmentMessage[] getFulfillmentMessage(int expectedCode) {
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_FULFILLMENT_MESSAGES).
                header("Authorization", "Bearer " + suiteData.getRelayJwt()).
                queryParam("limit", 80000).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();

        return new Gson().fromJson(response, FulfillmentMessage[].class);
    }

    private LicensePoolPayload createLicensePayload(int uniqueNumber) {
        contractItems.add(uniqueNumber);
        LicensePoolPayload licensePoolPayload = new LicensePoolPayload();
        licensePoolPayload.setContract("allcollections" + uniqueNumber);
        licensePoolPayload.setContractLine(uniqueNumber);
        licensePoolPayload.setKitId("qadomain" + uniqueNumber);
        licensePoolPayload.setPeopleSoftOrgId("psoft" + uniqueNumber);
        licensePoolPayload.setSalesForceCustId("salesforceid" + uniqueNumber);
        licensePoolPayload.setOrderNumber(uniqueNumber);
        return licensePoolPayload;

    }


    @Ignore
    public void clearRelayMessage() {
        FulfillmentMessage[] fulfillmentMessages = getFulfillmentMessage(200);
        if (fulfillmentMessages.length > 0) {
            log.info("************Clearing Fulfillment Messages if any exist***********");
            int i = fulfillmentMessages.length;
            acknowledgeFulfillmentMessages(fulfillmentMessages[i - 1].getFulfillmentId(), fulfillmentMessages[i - 1].getSeq());
        }
    }

    @Ignore
    public void getFulfillmentMessageOnCreatingLicense() {
        int payloadCreate = new Random().nextInt(9000) + 1000;
        licensePoolPayload = createLicensePayload(payloadCreate);
        licensePoolPayload.setCollectionId("CRS_ITComprehensive");
        createLicensePool(200, licensePoolPayload);
        FulfillmentMessage[] fulfillmentMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadCreate, JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.orderNumber"));
        assertEquals(suiteData.getOrgId().toString(), JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.orgId"));
        assertEquals("CRS_ITComprehensive", JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.collectionId"));

    }

    @Ignore
    public void getFulfillmentMessageOnUpdatingOrderNumberViaPutCall() {
        int payloadCreate = new Random().nextInt(9000) + 1000;
        licensePoolPayload = createLicensePayload(payloadCreate);
        createLicensePool(200, licensePoolPayload);
        log.info("-------------------License Created------------------------------------");
        FulfillmentMessage[] fulfillmentMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadCreate, JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Created------------------------------------");

        int payloadUpdate = new Random().nextInt(9000) + 1000;
        contractItems.add(payloadUpdate);
        LicensePoolPayload updateLicensePayload = new LicensePoolPayload();
        updateLicensePayload.setNewContract("allcollections" + payloadUpdate);
        updateLicensePayload.setNewContractLine(payloadUpdate);
        updateLicensePayload.setNewPeopleSoftProdId("active1-PSPID-updated");
        //Kit Id should be same to avoid renewal process
        updateLicensePayload.setKitId("qadomain" + payloadCreate);
        updateLicensePayload.setOrderNumber(payloadUpdate);
        updateLicensePool(200, updateLicensePayload, "allcollections" + payloadCreate, payloadCreate);
        log.info("----------------------License Updated---------------------------------");
        FulfillmentMessage[] updatedMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadUpdate, JsonPath.parse(updatedMessage[1].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Updated------------------------------------");
    }

    @Ignore
    public void getFulfillmentMessageOnUpdatingOrderNumberViaPatchByContract() {
        int payloadCreate = new Random().nextInt(9000) + 1000;
        licensePoolPayload = createLicensePayload(payloadCreate);
        createLicensePool(200, licensePoolPayload);
        log.info("-------------------License Created------------------------------------");
        FulfillmentMessage[] fulfillmentMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadCreate, JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Created------------------------------------");

        String patchPayload = "{\"orderNumber\":" + payloadCreate + ",\"contractValueUSD\": 2}";
        patchLicensePoolByContract(200, "allcollections" + payloadCreate, payloadCreate, patchPayload);
        log.info("----------------------License Updated---------------------------------");
        FulfillmentMessage[] updatedMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadCreate, JsonPath.parse(updatedMessage[1].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Updated------------------------------------");

    }

    @Ignore
    public void getFulfillmentMessageOnRenewingLicense() {
        int payloadCreate = new Random().nextInt(9000) + 1000;
        licensePoolPayload = createLicensePayload(payloadCreate);
        createLicensePool(200, licensePoolPayload);
        log.info("-------------------License Created------------------------------------");
        FulfillmentMessage[] fulfillmentMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadCreate, JsonPath.parse(fulfillmentMessage[0].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Created------------------------------------");


        int payloadRenew = new Random().nextInt(9000) + 1000;
        contractItems.add(payloadRenew);
        LicensePoolPayload renewLicensePoolPayload = new LicensePoolPayload();
        renewLicensePoolPayload.setOldContract("allcollections" + payloadCreate);
        renewLicensePoolPayload.setOldContractLine(payloadCreate);
        renewLicensePoolPayload.setOldKitId("qadomain" + payloadCreate);
        renewLicensePoolPayload.setLineStartDate("2040-01-01T00:00:00.000Z");
        renewLicensePoolPayload.setLineEndDate("2050-01-01T00:00:00.000Z");
        renewLicensePoolPayload.setLicenseExpDate("2050-01-01T00:00:00.000Z");
        renewLicensePoolPayload.setOrderNumber(payloadRenew);
        renewLicensePoolPayload.setContract("allcollections" + payloadRenew);
        renewLicensePoolPayload.setContractLine(payloadRenew);
        renewLicensePoolPayload.setKitId("qadomain" + payloadRenew);
        renewLicensePoolPayload.setPeopleSoftOrgId("psoft" + payloadRenew);
        renewLicensePoolPayload.setSalesForceCustId("salesforceid" + payloadRenew);
        createLicensePool(200, renewLicensePoolPayload);
        log.info("----------------------License Renewed---------------------------------");
        FulfillmentMessage[] updatedMessage = getFulfillmentMessage(200);
        assertEquals((double) payloadRenew, JsonPath.parse(updatedMessage[1].getMesg()).read("$.orderNumber"));
        log.info("-------------------Fulfillment message Updated------------------------------------");
    }


    @Ignore
    public static void deleteAllTheLpCreatedForThisTest() {
        for (Integer contractItem : contractItems) {
            deleteLicensePool("allcollections" + contractItem, contractItem, contractItem);
        }
        log.info("LPs created for this test are deleted");
    }


}
