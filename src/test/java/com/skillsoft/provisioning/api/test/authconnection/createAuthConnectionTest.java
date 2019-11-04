package com.skillsoft.provisioning.api.test.authconnection;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.organization.CreateOrganizationTest;
import com.skillsoft.provisioning.api.test.organization.DeleteOrganizationTest;
import com.skillsoft.provisioning.api.test.organization.OrganizationPayload;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.skillsoft.provisioning.api.test.organization.CreateOrganizationTest.createOrganization;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class createAuthConnectionTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(createAuthConnectionTest.class);
    private static final String POST_AUTHCONNECTION0_PATH = "/v1/organizations/{orgId}/auth/connections";
    public ConnectionPayload connectionPayload;
    public OrganizationPayload organizationPayload;
    private static final String ORG_ID = "orgId";
    CreateOrganizationTest createOrganizationTest = new CreateOrganizationTest();
    private static List<String> orgIds = new ArrayList<String>();


    public String createAuthConnection(int expectedCode, ConnectionPayload connectionPayload) {

        //Create a new Organization
        int payloadUniqueNumber = new Random().nextInt(900) + 100;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        String orgIdResponse = createOrganization(200, organizationPayload);
        log.info("****************ORG IS CREATED***************");
        String orgId = JsonPath.parse(orgIdResponse).read("$.orgId");
        log.info("ORG_ID:"+orgId);
        orgIds.add(orgId);

        Map pathParams = new HashMap();
        pathParams.put(ORG_ID,orgId);
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(POST_AUTHCONNECTION0_PATH).
                body(connectionPayload.toJSON()).
                pathParams(pathParams).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                post().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();

        return resp;
    }

    @Test
    public void validateNameFormatForAuthConnectionCreated() throws  Exception{

        //Create an AuthConnection
        connectionPayload = new ConnectionPayload();
        String connectionResponse = createAuthConnection(200, connectionPayload);
        log.info("*************Connection is Created****************");
        String connectionId = JsonPath.parse(connectionResponse).read("$.id");
        log.info(connectionId);

        //Extract name from the response

        String name = JsonPath.parse(connectionResponse).read("$.name");
        log.info(name);

        //Validate that name matches the specified format
        String nameSplit[] = name.split("-");

        // Base keyword check.
        assertEquals(nameSplit[0], "Base");

        //Validate Time stamp is bigger than the least epoch time possible.
        Long timeStamp = Long.parseLong(nameSplit[nameSplit.length-1]);
        long MIN_TIMESTAMP = new SimpleDateFormat("yyyy/MM/dd").parse("1970/01/01").getTime();
        log.info(timeStamp.toString());

        assertTrue(MIN_TIMESTAMP<timeStamp);

    }


    @AfterClass
    public static void deleteAllTheOrganizationsCreatedForThisTest() {

        for (String orgId : orgIds)
            DeleteOrganizationTest.deleteOrganization(orgId, suiteData.getJwt());
        log.info("Orgs created for this test are deleted");
    }
}


