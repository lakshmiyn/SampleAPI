package com.skillsoft.provisioning.api.test.provisioningbizapps.Channels;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
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

public class GetChannelByChannelUuidTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GetAllChannelsTest.class);
    private static final String CHANNEL_UUID="channelUuid";
    public static final String GET_ALL_CHANNELS_BY_CHANNEL_UUID_PATH = "/v1/provisioning/channels/{channelUuid}";


    @BeforeClass
    public static void start(){
        skipTestsInThisClassOnRelEnv();
    }
private String getChannelByChannelUuid(int expectedCode,String jwt){
    String resp=given().
            baseUri(suiteData.getBizappsBaseUri()).
            basePath(GET_ALL_CHANNELS_BY_CHANNEL_UUID_PATH).
            pathParam(CHANNEL_UUID,"07cdd140-302d-11e7-b820-8b5df313d8ed").
            header("Authorization","Bearer " +jwt).
            contentType(ContentType.JSON).
            when().get().then().
            statusCode(equalTo(expectedCode)).extract().response().asString();

    return resp;
}

    @Test
    public void ensureValidAuthorizationReturnsNonZeroResults() {
        String response = getChannelByChannelUuid(200, suiteData.getJwt());
        assertNotNull(response);
        log.info(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is greater than zero", responseLength > 0);
    }

    @Test
    public void validateInvalidAuthorizationReturns500(){
        String response = getChannelByChannelUuid(500, "Invalid-Auth-Token");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }

    public void validateNonExistingChannelUuidThrow404Error(){
        String resp=given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(GET_ALL_CHANNELS_BY_CHANNEL_UUID_PATH).
                pathParam(CHANNEL_UUID,"07cdd140-302d-11e7-b820-8b5df313d8e1").
                header("Authorization","Bearer " +suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().get().then().
                statusCode(equalTo(404)).extract().response().asString();
    }
    @Test
    public void validateSchemaForGetChannelByChannelUuid() throws IOException{
        String schema = SchemaHelper.loadSchema("getChannelByChannelUuidSchema");
        given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(GET_ALL_CHANNELS_BY_CHANNEL_UUID_PATH).
                pathParam(CHANNEL_UUID,"07cdd140-302d-11e7-b820-8b5df313d8ed").
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));
    }

}

