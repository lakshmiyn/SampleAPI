package com.skillsoft.provisioning.api.test.provisioningbizapps.Assets;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.GetLicensePoolTest;
import com.skillsoft.provisioning.api.testdata.SchemaHelper;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;



public class GetAssetInfoByUuidTest extends BaseTest{
    private static final SuiteData suiteData = SuiteData.getData();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GetLicensePoolTest.class);
    private static final String Get_ASSET_BY_UUID_PATH = "/v1/provisioning/assets/{assetUuid}";
    private static final String ASSET_UUID="assetUuid";

    @BeforeClass
    public static void start(){
        skipTestsInThisClassOnRelEnv();
    }


    public String callGetAssetInfoByUuidTest(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(Get_ASSET_BY_UUID_PATH).
                pathParam(ASSET_UUID,"270f2cf7-05cd-11e7-b6c3-0242c0a80802").
                header("Authorization", "Bearer " + jwt).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }

    @Test
    public void ensureResponseIsNotEmptyWithCorrectJwt(){
        String response=callGetAssetInfoByUuidTest(200,suiteData.getJwt());
        assertNotNull(response);
        log.info(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is not greater than zero", responseLength > 0);
    }

    @Test
    public void validateInvalidJwtReturns500UnauthorizedError(){
        String response=callGetAssetInfoByUuidTest(500,"Bad Jwt");
        assertEquals("Failed verifying signature of JWT",response);
        log.info(response);
    }
    @Test
    public void validateNonExistingAssetUuidThrow500Error(){
        String resp=given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(Get_ASSET_BY_UUID_PATH).
                pathParam(ASSET_UUID,"270f2cf7-05cd-11e7-b6c3-0242c0a80805").
                header("Authorization","Bearer " +suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().get().then().
                statusCode(equalTo(500)).extract().response().asString();

        log.info(resp);
    }

    @Test
    public void validateSchemaForGetAssetByAssetId() throws IOException {
        String schema = SchemaHelper.loadSchema("getAssetInfoByAssetUuidSchema");
        given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(Get_ASSET_BY_UUID_PATH).
                pathParam(ASSET_UUID,"270f2cf7-05cd-11e7-b6c3-0242c0a80802").
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));
    }


}
