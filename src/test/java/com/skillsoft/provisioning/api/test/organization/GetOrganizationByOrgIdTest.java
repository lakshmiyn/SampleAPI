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

public class GetOrganizationByOrgIdTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetOrganizationByOrgIdTest.class);
    private static final String ORG_ID="orgId";
    private static final String GET_ORGANIZATION_PATH="/v1/organizations/{orgId}";



    private String getOrganizationByOrgId(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
               pathParam(ORG_ID,suiteData.getOrgId()).
                header("Authorization", "Bearer " + jwt).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }
    @Test
    public void ensureValidAuthorizationReturnsNonZeroResults() {
        String response = getOrganizationByOrgId(200, suiteData.getJwt());
        assertNotNull(response);
        log.info(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is greater than zero", responseLength > 0);
    }
    @Test
    public void validateInvalidAuthorizationReturns401(){
        String response = getOrganizationByOrgId(401, "Invalid-Auth-Token");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }


    @Test
    public void validate400ErrorIsThrownForInvalidOrganizationFormat(){
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
                pathParam(ORG_ID,"bad-data").
                header("Authorization", "Bearer " + suiteData.getJwt())
                .get()
                .then()
                .statusCode(400).extract().response().asString();
        log.info(resp);
    }
    @Test
    public void validate404ErrorIsThrownWhenOrganizationIsNotFound(){
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
                pathParam(ORG_ID,"44435220-63b0-4608-9ab7-f398781903c0").
                header("Authorization", "Bearer " + suiteData.getJwt())
                .get()
                .then()
                .statusCode(404).extract().response().asString();
        log.info(resp);
    }

    @Test
    public void validateSchemaForGetOrganizationByOrgId() throws IOException {

        String schema = SchemaHelper.loadSchema("getOrganizationByOrgIdSchema");
        given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_ORGANIZATION_PATH).
                pathParam(ORG_ID,suiteData.getOrgId()).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));

    }



}

