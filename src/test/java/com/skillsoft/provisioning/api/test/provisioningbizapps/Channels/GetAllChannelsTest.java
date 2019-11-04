package com.skillsoft.provisioning.api.test.provisioningbizapps.Channels;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.testdata.SchemaHelper;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class GetAllChannelsTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetAllChannelsTest.class);
    public static final String GET_ALL_CHANNELS_PATH = "/v1/provisioning/channels";

    @BeforeClass
    public static void start() {
        skipTestsInThisClassOnRelEnv();
    }

    private String getAllChannels(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(GET_ALL_CHANNELS_PATH).
                header("Authorization", "Bearer " + jwt).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }

    @Test
    public void ensureResponseIsNotEmptyWithCorrectJwt() {
        String response = getAllChannels(200, suiteData.getJwt());
        assertNotNull(response);
        //log.info(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is not greater than zero", responseLength > 0);
    }

    @Test
    public void validateInvalidJwtReturns500UnauthorizedError() {
        String response = getAllChannels(500, "Bad Jwt");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }

    @Test
    public void validateSchemaForGetAllChannels() throws Exception {
        String schema = SchemaHelper.loadSchema("getAllChannelsSchema.json");
        given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(GET_ALL_CHANNELS_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().get().then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));
        log.info(schema);
    }


}
