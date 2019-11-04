package com.skillsoft.provisioning.api.test.organization;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import net.minidev.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateOrganizationTest extends BaseTest {
    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(CreateOrganizationTest.class);
    private static final String POST_ORGANIZATION_PATH = "/v1/organizations";
    private OrganizationPayload organizationPayload;
    private static List<String> orgIds = new ArrayList<String>();


    public static String createOrganization(int expectedCode, OrganizationPayload organizationPayload) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(POST_ORGANIZATION_PATH).
                body(organizationPayload.toJSON()).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                contentType(ContentType.JSON).
                when().
                post().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }

//    private OrganizationPayload createOrganizationPayload(int payloaduniqueNumber) {
//        OrganizationPayload organizationPayload = new OrganizationPayload();
//        organizationPayload.setPeopleSoftId("peopleSoftId"+payloaduniqueNumber);
//        organizationPayload.setSalesForceCustId("salesForceCustId"+payloaduniqueNumber);
//        organizationPayload.setOrgName("orgName"+payloaduniqueNumber);
//        JSONObject compliance = new JSONObject();
//        compliance.put("complianceFeatureEnabled", true);
//        compliance.put("academyOrgId", UUID.randomUUID());
//        organizationPayload.setCompliance(compliance);
//        return organizationPayload;
//
//    }

    @Test
    public void createNewOrganization() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        JSONObject props = new JSONObject();
        props.put("hccEnabled", true);
        organizationPayload.setProps(props);
        String orgIdResponse = createOrganization(200, organizationPayload);
        log.info("****************ORG IS CREATED***************");
        String orgId = JsonPath.parse(orgIdResponse).read("$.orgId");
        log.info(orgIdResponse);
        orgIds.add(orgId);

    }

    @Test
    public void throw500ErrorForInValidDomainValue() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setDomain("dom ain");
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        String orgIdCreated = createOrganization(500, organizationPayload);
    }


    @Test
    public void validateOrgNameIsMandatoryForOrganizationCreation() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setDomain("dom ain");
        //organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        createOrganization(500, organizationPayload);
    }

    @Test
    public void validateInvalidPayloadDataThrows500Error() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        //Invalid Domain
        organizationPayload.setDomain("dom ain");
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        createOrganization(500, organizationPayload);
    }
     //Ignoring test case for now. Will be tested again after organization api changes are made.
    //@Test
    public void throwConflictErrorForDuplicateOrganization() {
        int payloadUniqueNumber = new Random().nextInt(9000) + 1000;
        organizationPayload = new OrganizationPayload();
        organizationPayload.setPeopleSoftId("psoft" + payloadUniqueNumber);
        organizationPayload.setSalesForceCustId("salesforceid" + payloadUniqueNumber);
        organizationPayload.setOrgName("Pints_Test_Org" + payloadUniqueNumber);
        JSONObject compliance = new JSONObject();
        compliance.put("complianceFeatureEnabled", true);
        compliance.put("academyOrgId", UUID.randomUUID());
        organizationPayload.setCompliance(compliance);
        //1st create an Organization
        String orgIdResponse = createOrganization(200, organizationPayload);
        log.info(orgIdResponse);
        String orgId = JsonPath.parse(orgIdResponse).read("$.orgId");
        orgIds.add(orgId);
        //Now create another Organization with same payload
        String ConflictError = createOrganization(409, organizationPayload);

        log.info(ConflictError);

    }

   // @AfterClass
    public static void deleteAllTheOrganizationsCreatedForThisTest() {

        for (String orgId : orgIds)
            DeleteOrganizationTest.deleteOrganization(orgId, suiteData.getJwt());
        log.info("Orgs created for this test are deleted");
    }


}

