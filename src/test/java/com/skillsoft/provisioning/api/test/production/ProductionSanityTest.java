package com.skillsoft.provisioning.api.test.production;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.RunOnEnvironments;
import io.restassured.http.ContentType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

import static org.junit.Assert.assertTrue;

public class ProductionSanityTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(ProductionSanityTest.class);

    private static String TOKEN = suiteData.getJwt();
    private static final String PROVISIONING_BASE_URL = suiteData.getProvisioningBaseUri();
    private static final String ORG_ID = "orgId";
    public static final String GET_ORGANIZATION_PATH="/v1/organizations";
    public static final String GET_LICENSE_POOLS_PATH = "/v1/license-pools";
    public static final String GET_USERS_PATH="/v1/organizations/{orgId}/users";


    @Test
    @RunOnEnvironments(
            environments = {"eudc"}
    )
    public void productionProvisioningGetAllOrg() {
        String resp =
                given().
                        header("Authorization", "Bearer " + TOKEN).
                        baseUri(PROVISIONING_BASE_URL).
                        basePath(GET_ORGANIZATION_PATH).
                        contentType(ContentType.JSON).
                        when().
                        get().
                        then().
                        statusCode(200).extract().response().asString();
        //log.info(resp);
        int responseLength = JsonPath.parse(resp).read("$.length()");
        assertTrue("response length is greater than zero", responseLength > 0);

    }

    @Test
    @RunOnEnvironments(
            environments = {"eudc"}
    )
    public void productionGetAllLicensePools(){
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LICENSE_POOLS_PATH).
                header("Authorization", "Bearer " + TOKEN).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(200).extract().response().asString();
        //log.info(resp);

        int responseLength = JsonPath.parse(resp).read("$.length()");
        assertTrue("response length is not greater than zero", responseLength > 0);
    }

    @Test
    @RunOnEnvironments(
            environments = {"eudc"})
    public void productionGetUsersForOrganisation(){
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_USERS_PATH).
                header("Authorization", "Bearer " + TOKEN).
                pathParam("orgId",suiteData.getOrgId()).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(200).extract().response().asString();

    }

}
