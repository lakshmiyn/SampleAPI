package com.skillsoft.provisioning.api.test.LicensePool_V2;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.RunOnStaging;
import com.skillsoft.provisioning.api.test.licensepool.GetLicensePoolTest;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetLicensePoolWithOrgId {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetLicensePoolTest.class);
    public static final String GET_LICENSE_POOLS_PATH = "/v2/license-pools/organization/{orgId}";
    private static final String ORGID="orgId";

    private String getLicensePoolsWithOrgId(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOLS_PATH).
                header("Authorization", "Bearer " + jwt).
                pathParam("orgId",suiteData.getOrgId()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }
    @Test
    public void ensureValidAuthorizationReturnsNonZeroResults() {
        String response = getLicensePoolsWithOrgId(200, suiteData.getJwt());
        assertNotNull(response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is not greater than zero", responseLength > 0);
    }

    @Test
    //This should be 401 inplace of 500
    public void ensurInvalidAuthorizationReturnsZeroResults() {
        String response = getLicensePoolsWithOrgId(401, "Invalid JWT");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }

    @Test
    public void validateSchema() throws IOException {
        String schema = SchemaHelper.loadSchema("getLicensePoolsSchema");
        given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOLS_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                pathParam("orgId",suiteData.getOrgId()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));

    }
}
