package com.skillsoft.provisioning.api.test.organization;

import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

public class DeleteOrganizationTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(DeleteOrganizationTest.class);
    private static final String DELETE_ORGANIZATION_PATH = "/v1/organizations/{orgId}";
    private static final String ORG_ID = "orgId";


    public static int deleteOrganization(String orgId, String jwt) {
        int responseCode = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(DELETE_ORGANIZATION_PATH).
                pathParams(ORG_ID, orgId).
                header("Authorization", "Bearer " + jwt).
                when().
                delete().
                then().
                extract().statusCode();
        return responseCode;
    }



}

