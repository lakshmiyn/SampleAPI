package com.skillsoft.provisioning.api.test.organization;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.RunOnStaging;
import com.skillsoft.provisioning.api.testdata.SchemaHelper;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class GetAllOrganizationTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetAllOrganizationTest.class);
    public static final String GET_ORGANIZATION_PATH="/v1/organizations";



    private String getAllOrganizations(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
                header("Authorization", "Bearer " + jwt).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }
    @Test
    @RunOnStaging
    public void ensureValidAuthorizationReturnsNonZeroResults() {
        String response = getAllOrganizations(200, suiteData.getJwt());
        assertNotNull(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is greater than zero", responseLength > 0);
    }
    @Test
    public void validateInvalidAuthorizationReturns401(){
        String response = getAllOrganizations(401, "Invalid-Auth-Token");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }

    @Test
    public void validateSchemaForGetOrganization() throws IOException {

        String schema = SchemaHelper.loadSchema("getOrganizationSchema");
        given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));

    }

}

