package com.skillsoft.provisioning.api.test.organization;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.skillsoft.provisioning.api.test.organization.CreateOrganizationTest.createOrganization;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class UpdateOrganizationTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(CreateOrganizationTest.class);
    private static final String PATCH_ORGANIZATION_PATH = "/v1/organizations/{orgId}";
    private OrganizationPayload organizationPayload, updateOrganizationPayload;

    public static final String ORG_ID = "orgId";
    private static List<String> orgIds = new ArrayList<String>();


    private static String updateOrganization(int expectedCode, OrganizationPayload organizationPayload, String orgId) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(PATCH_ORGANIZATION_PATH).
                body(organizationPayload.toJSON()).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                pathParam(ORG_ID, orgId).
                contentType(ContentType.JSON).
                when().
                patch().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }

    private String getOrganizationByOrgId(int expectedCode, String jwt, String orgId) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(PATCH_ORGANIZATION_PATH).
                pathParam("orgId", orgId).
                header("Authorization", "Bearer " + jwt).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }

    @Test
    public void validateOrganizationDataIsUpdateSuccessfully() {
        //Create an Organization
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        String orgResponse = createOrganization(200, organizationPayload);
        String orgIdCreated = JsonPath.parse(orgResponse).read("$.orgId");
        log.info("****************ORG IS CREATED***************");
        log.info(orgIdCreated);
        orgIds.add(orgIdCreated);
        int payloadUpdate = new Random().nextInt(9000) + 1000;
        updateOrganizationPayload = new OrganizationPayload();
        updateOrganizationPayload.setPeopleSoftId("peopleSoftId" + payloadUpdate);
        updateOrganizationPayload.setSalesForceCustId("salesforceid" + payloadUpdate);
        updateOrganizationPayload.setOrgName("Updated_Test_Org_Name" + payloadUpdate);
        updateOrganizationPayload.setDomain("Demo"+payloadUniqueNumber);
        JSONObject compliance1 = new JSONObject();
        compliance1.put("complianceFeatureEnabled", false);
        compliance1.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance1);
        String organizationUpdate = updateOrganization(200, updateOrganizationPayload, orgIdCreated);
        log.info(organizationUpdate);
        log.info("Organization is Updated");

    }

    @Test
    public void validateComplianceIsUpdatedWithoutOrgNameAndDomain() {
        //Create an Organization without compliance
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        String orgResponse = createOrganization(200, organizationPayload);
        String orgIdCreated = JsonPath.parse(orgResponse).read("$.orgId");
        log.info("****************ORG IS CREATED***************");
        log.info(orgIdCreated);
        orgIds.add(orgIdCreated);

        //Update the organization with compliance
        int payloadUpdate = new Random().nextInt(9000) + 1000;
        updateOrganizationPayload = new OrganizationPayload();
        JSONObject compliance1 = new JSONObject();
        compliance1.put("complianceFeatureEnabled", false);
        compliance1.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance1);
        String organizationUpdate = updateOrganization(200, updateOrganizationPayload, orgIdCreated);
        log.info(organizationUpdate);
        log.info("Organization is Updated");

    }

    @AfterClass
    public static void deleteAllTheOrganizationsCreatedForThisTest() {
        for (String orgId : orgIds)
            DeleteOrganizationTest.deleteOrganization(orgId, suiteData.getJwt());
        log.info("Orgs created for this test are deleted");
    }


}
