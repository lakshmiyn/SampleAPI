package com.skillsoft.provisioning.api.test.licensepool;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
@Ignore
public class CreateLicensePoolTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(CreateLicensePoolTest.class);
    public static final String POST_LICENSE_POOLS_PATH = "/v1/license-pools";
    public static final String DELETE_LICENSE_POOLS_PATH = "/v1/license-pools/contract/{contract}/line/{contractLine}/order/{orderNumber}";
    private static final String GET_LICENSE_POOL_SETS = "/v1/license-pools/sets/{lpSetId}";
    private LicensePoolPayload licensePoolPayload;
    public static final List<Integer> contractItems = new ArrayList<Integer>();

    public static String createLicensePool(int expectedCode, LicensePoolPayload licensePoolPayload) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(POST_LICENSE_POOLS_PATH).
                body(licensePoolPayload.toJSON()).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                post().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }


    public static int deleteLicensePool(String contract, int contractLine, int orderNumber) {
        Map pathParams = new HashMap();
        pathParams.put("contract", contract);
        pathParams.put("contractLine", contractLine);
        pathParams.put("orderNumber", orderNumber);
        int responseCode = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(DELETE_LICENSE_POOLS_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                pathParams(pathParams).
                when().
                delete().
                then().
                extract().statusCode();

        return responseCode;
    }

    private static String getLpSetsAssociatedWithLpSetId(int expectedCode, String lpSetId) {
        String response = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOL_SETS).
                pathParam("lpSetId", lpSetId).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return response;
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
    public void createLicensePoolForNewCustomer() {
        int payloadUniqueNumber = new Random().nextInt(3000) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        String response = createLicensePool(200, licensePoolPayload);
        String id = JsonPath.parse(response).read("$.id");
        String lpId = JsonPath.parse(response).read("$.lpId");
        String lpSetId = JsonPath.parse(response).read("$.lpSetId");
        assertNotNull("id id not created", id);
        assertNotNull("lpId id not created", lpId);
        assertNotNull("lpSetId id not created", lpSetId);
    }


    @Test
    public void conflictErrorIsCreatedWhenSameContractIsUsed() {
        int payloadUniqueNumber = new Random().nextInt(4000) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        String response = createLicensePool(200, licensePoolPayload);
        //Trying to create with same data again
        String conflictResponse = createLicensePool(409, licensePoolPayload);
        //Error format can be standardized
        log.info(conflictResponse);
    }

    @Test
    public void licensePoolIsNotCreatedOnPassingEmptyContractDetails() {
        int payloadUniqueNumber = new Random().nextInt(5000) + 1000;
        licensePoolPayload = new LicensePoolPayload();
        licensePoolPayload.setContract("");
        licensePoolPayload.setContractLine(0);
        licensePoolPayload.setKitId("qadomain" + payloadUniqueNumber);
        licensePoolPayload.setPeopleSoftOrgId("psoft" + payloadUniqueNumber);
        licensePoolPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        String errorResponse = createLicensePool(400, licensePoolPayload);
        log.info(errorResponse);
    }

    @Test
    public void lpIdShouldBeSameOnRenewingForSameCollectionId() {
        int payloadUniqueNumber1 = new Random().nextInt(6000) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber1);
        String response = createLicensePool(200, licensePoolPayload);
        log.info(response);
        String lpId = JsonPath.parse(response).read("$.lpId");
        int payloadUniqueNumber2 = new Random().nextInt(7000) + 1000;
        LicensePoolPayload renewLicensePoolPayload = createLicensePayload(payloadUniqueNumber2);
        renewLicensePoolPayload.setOldContract("allcollections" + payloadUniqueNumber1);
        renewLicensePoolPayload.setOldContractLine(payloadUniqueNumber1);
        renewLicensePoolPayload.setOldKitId("qadomain" + payloadUniqueNumber1);
        renewLicensePoolPayload.setLineStartDate("2040-01-01T00:00:00.000Z");
        renewLicensePoolPayload.setLineEndDate("2050-01-01T00:00:00.000Z");
        renewLicensePoolPayload.setLicenseExpDate("2050-01-01T00:00:00.000Z");
        String renewResponse = createLicensePool(200, renewLicensePoolPayload);
        log.info(renewResponse);
        String renewedLpId = JsonPath.parse(renewResponse).read("$.lpId");
        assertEquals(lpId, renewedLpId);
    }

    @Test
    public void lpSetIdShouldBeSameForDifferentVariantsOfCollections() {
        int payloadUniqueNumber1 = new Random().nextInt(8000) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber1);
        String response1 = createLicensePool(200, licensePoolPayload);
        log.info("************LP is created***********");
        log.info(response1);
        String lpSetId1 = JsonPath.parse(response1).read("$.lpSetId");
        int payloadUniqueNumber2 = new Random().nextInt(8100) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber2);
        licensePoolPayload.setKitId("qadomain" + payloadUniqueNumber1);
        licensePoolPayload.setCollectionId("BK_DigitalSkills");
        String response2 = createLicensePool(200, licensePoolPayload);
        log.info("************LP is created***********");
        log.info(response2);
        String lpSetId2 = JsonPath.parse(response2).read("$.lpSetId");
        assertEquals(lpSetId1, lpSetId2);
        String lpSetResponse = getLpSetsAssociatedWithLpSetId(200, lpSetId1);
        log.info(lpSetResponse);

    }

    @Test
    public void crossWiringOfLpSetIdIsNotAllowedOnChangingCollectionDuringRenewal() {
        int payloadUniqueNumber1 = new Random().nextInt(3100) + 1000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber1);
        String response = createLicensePool(200, licensePoolPayload);
        log.info("************LP is created***********");
        log.info(response);
        int payloadUniqueNumber2 = new Random().nextInt(3200) + 1000;
        LicensePoolPayload renewLicensePoolPayload = createLicensePayload(payloadUniqueNumber2);
        renewLicensePoolPayload.setOldKitId("qadomain" + payloadUniqueNumber1);
        renewLicensePoolPayload.setCollectionId("BK_BusinessSkills");
        //Could have been 400, but handled by middleware and they are ok with this, Prov being an internal application
        String errorResponse = createLicensePool(500, renewLicensePoolPayload);
        log.info(errorResponse);
        assertEquals("Collection(s) of the oldKit qadomain" + payloadUniqueNumber1 + " does not match the collection of the newKit qadomain" + payloadUniqueNumber2 +", source: undefined", errorResponse);
        int payloadUniqueNumber3 = new Random().nextInt(3300) + 1000;
        LicensePoolPayload renewLicensePoolPayload1 = createLicensePayload(payloadUniqueNumber3);
        renewLicensePoolPayload1.setOldKitId("qadomain" + payloadUniqueNumber1);
        renewLicensePoolPayload1.setCollectionId("CRS_BusinessSkills");
        String errorResponse1 = createLicensePool(500, renewLicensePoolPayload1);
        log.info(errorResponse1);

    }


    @AfterClass
    public static void deleteAllTheLpCreatedForThisTest() {
        for (Integer contractItem : contractItems)
            deleteLicensePool("allcollections" + contractItem, contractItem, 1);
        log.info("LPs created for this test are deleted");
    }

}
