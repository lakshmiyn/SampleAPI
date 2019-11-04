package com.skillsoft.provisioning.api.test.licensepoolmessagerelay;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.LicensePoolPayload;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.createLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.deleteLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.UpdateLicensePoolTest.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GetLicensePoolChannelChangeMessageTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetLicensePoolChannelChangeMessageTest.class);
    public static final String GET_LICENSE_POOLS_RELAY_MESSAGE = "/v1/lp-chan-change-mesgs/";
    private static final String POST_LICENSE_POOL_ACKNOWLEDGE_RELAY = "/v1/lp-chan-change-mesgs/ack/id/{id}/seq/{seq}";
    private LicensePoolPayload licensePoolPayload, updateLicensePoolPayload;
    private static final List<Integer> contractItems = new ArrayList<Integer>();


    private int acknowledgeRelayAndDeleteMessages(String id, int seq) {
        Map<String, java.io.Serializable> queryParams = new HashMap<String, java.io.Serializable>();
        queryParams.put("id", id);
        queryParams.put("seq", seq);
        return given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(POST_LICENSE_POOL_ACKNOWLEDGE_RELAY).
                header("Authorization", "Bearer " + suiteData.getRelayJwt()).
                pathParams(queryParams).
                contentType(ContentType.JSON).
                when().
                post().
                then().
                extract().statusCode();

    }

    private MessageRelay[] getLicensePoolRelayMessage(int expectedCode) {
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOLS_RELAY_MESSAGE).
                header("Authorization", "Bearer " + suiteData.getRelayJwt()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();

        MessageRelay[] messageRelay = new Gson().fromJson(response, MessageRelay[].class);

        return messageRelay;
    }

    @Before
    public void clearRelayMessage() {
        MessageRelay[] messageRelays = getLicensePoolRelayMessage(200);
        log.info("************Clearing Relay Messages if any exist***********");
        for (MessageRelay messageRelay : messageRelays) {
            acknowledgeRelayAndDeleteMessages(messageRelay.getLpId(), messageRelay.getSeq());
        }
    }

    private LicensePoolPayload createLicensePayload(int uniqueNumber) {
        contractItems.add(uniqueNumber);
        LicensePoolPayload licensePoolPayload = new LicensePoolPayload();
        licensePoolPayload.setContract("allcollections" + uniqueNumber);
        licensePoolPayload.setContractLine(uniqueNumber);
        licensePoolPayload.setKitId("qadomain" + uniqueNumber);
        licensePoolPayload.setPeopleSoftOrgId("psoft" + uniqueNumber);
        licensePoolPayload.setSalesForceCustId("salesforceid" + uniqueNumber);
        return licensePoolPayload;

    }

    @Test
    public void getRelayMessageOnCreatingLicensePoolWhenPropsAreSetToDefaultValues() {
        clearRelayMessage();
        int payloadUniqueNumber = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        String response = createLicensePool(200, licensePoolPayload);
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        log.info("-------------------------------------------------------");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.channelRestrictionsApply"));
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.academicRestrictionsApply"));
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.internationalRestrictionsApply"));

    }

    @Test
    public void getRelayMessageOnCreatingLicensePoolWhenPropsAreSetToTrue() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        JSONObject props = new JSONObject();
        props.put("channelRestrictionsApply", Boolean.TRUE);
        props.put("academicRestrictionsApply", Boolean.TRUE);
        props.put("internationalRestrictionsApply", Boolean.TRUE);
        licensePoolPayload.setProps(props);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertTrue((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.channelRestrictionsApply"));
        assertTrue((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.academicRestrictionsApply"));
        assertTrue((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.internationalRestrictionsApply"));

    }

    @Test
    public void getRelayMessageWhenRoyaltyRestrictionIsSetToTrue() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        licensePoolPayload.setRoyaltyRestrictionsApply(Boolean.TRUE);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertTrue((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));

    }

    @Test
    public void getRelayMessageWhenRoyaltyRestrictionIsSetToFalse() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        licensePoolPayload.setRoyaltyRestrictionsApply(Boolean.FALSE);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));

    }

    @Test
    public void getRelayMessageWhenRoyaltyRestrictionIsUpdatedToTrue() {
        int payloadCreate = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadCreate);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals("lpId creation mismatch", lpIdCreated, msgRelay[0].getLpId());
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));
        acknowledgeRelayAndDeleteMessages(msgRelay[0].getLpId(), msgRelay[0].getSeq());
        int payloadUpdate = new Random().nextInt(9000) + 100;
        contractItems.add(payloadUpdate);
        updateLicensePoolPayload = new LicensePoolPayload();
        updateLicensePoolPayload.setNewContract("allcollections" + payloadUpdate);
        updateLicensePoolPayload.setNewContractLine(payloadUpdate);
        updateLicensePoolPayload.setNewPeopleSoftProdId("active1-PSPID-updated");
        //Kit Id should be same to avoid renewal process
        updateLicensePoolPayload.setKitId("qadomain" + payloadCreate);
        updateLicensePoolPayload.setRoyaltyRestrictionsApply(Boolean.TRUE);
        String updateLpResponse = updateLicensePool(200, updateLicensePoolPayload, "allcollections" + payloadCreate, payloadCreate);
        log.info("-------------------------------------------------------");
        assertEquals(response, updateLpResponse);
        MessageRelay[] updatedRelay = getLicensePoolRelayMessage(200);
        assertEquals("lpId updation mismatch", lpIdCreated, updatedRelay[1].getLpId());
        assertTrue((Boolean) JsonPath.parse(updatedRelay[1].getMesg()).read("$.royaltyRestrictionsApply"));

    }


    @Test
    public void getRelayMessageWhenRoyaltyRestrictionIsUpdatedToTrueViaPatchByKitId() {
        int payloadCreate = new Random().nextInt(90000) + 100;
        licensePoolPayload = createLicensePayload(payloadCreate);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));
        acknowledgeRelayAndDeleteMessages(msgRelay[0].getLpId(), msgRelay[0].getSeq());
        String patchPayload = "{\"royaltyRestrictionsApply\":\"true\",\"contractValueUSD\": 2}";
        String updateLpResponse = patchLicensePoolByKitId(200, "qadomain" + payloadCreate, patchPayload);
        log.info("-------------------------------------------------------");
        MessageRelay[] updatedRelay = getLicensePoolRelayMessage(200);
        assertEquals("true", JsonPath.parse(updatedRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));

    }

    @Test
    public void getRelayMessageWhenRoyaltyRestrictionIsUpdatedToTrueViaPatchByContract() {
        int payloadCreate = new Random().nextInt(9000) + 100;
        licensePoolPayload = createLicensePayload(payloadCreate);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("-------------------------------------------------------");
        String lpIdCreated = JsonPath.parse(response).read("$.lpId");
        MessageRelay[] msgRelay = getLicensePoolRelayMessage(200);
        assertEquals(lpIdCreated, msgRelay[0].getLpId());
        assertFalse((Boolean) JsonPath.parse(msgRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));
        acknowledgeRelayAndDeleteMessages(msgRelay[0].getLpId(), msgRelay[0].getSeq());
        String patchPayload = "{\"royaltyRestrictionsApply\":\"true\",\"contractValueUSD\": 2}";
        String updateLpResponse = patchLicensePoolByContract(200, "allcollections" + payloadCreate, payloadCreate, patchPayload);
        log.info("-------------------------------------------------------");
        assertEquals(response, updateLpResponse);
        MessageRelay[] updatedRelay = getLicensePoolRelayMessage(200);
        assertEquals("true", JsonPath.parse(updatedRelay[0].getMesg()).read("$.royaltyRestrictionsApply"));

    }


    @AfterClass
    public static void deleteAllTheLpCreatedForThisTest() {
        for (Integer contractItem : contractItems) {
            deleteLicensePool("allcollections" + contractItem, contractItem, 1);
        }
        log.info("LPs created for this test are deleted");
    }


}
