package com.skillsoft.provisioning.api.test.provisioningbizapps.Organization;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.GetLicensePoolTest;
import com.skillsoft.provisioning.api.testdata.SchemaHelper;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetAssignedContentTest extends BaseTest {
    private static final SuiteData suiteData=SuiteData.getData();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GetLicensePoolTest.class);
    private static final String Get_ORGANIZATION_PATH="/v1/provisioning/organization";
    private static final String ORG_ID="orgId";
    private static final String CONTRACT="contract";
    private static final String CONTRACT_LINE="contractLine";

    @BeforeClass
    public static void start(){
        skipTestsInThisClassOnRelEnv();
    }
    private String getAssignedContentForAnOrganization(int expectedCode, String orgId, String contract, String contractLine,String jwt)
    {
        Map<String, Serializable> pathParams = new HashMap<String, Serializable>();
        pathParams.put("orgId", orgId);
        pathParams.put("contract", contract);
        pathParams.put("contractLine",contractLine);
        String resp= given().
                baseUri(suiteData.getBizappsBaseUri()).
                basePath(Get_ORGANIZATION_PATH ).
                header("Authorization", "Bearer " + jwt).
                queryParams(pathParams).
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(expectedCode)).extract().response().asString();
        return resp;
    }
    //@Test
    public void ensureValidAuthorizationReturnsNonZeroResults(){
        String response = getAssignedContentForAnOrganization(200, String.valueOf(suiteData.getOrgId()),"percipio-dev","12",suiteData.getJwt());
        int responseLength = JsonPath.parse(response).read("$.length()");
        assertTrue("response length is not greater than zero", responseLength > 0);

    }
    // @Test
    public void ensurInvalidAuthorizationReturnsZeroResults() {
        String response = getAssignedContentForAnOrganization(401, "44435220-63b0-4608-9ab7-f398781903b8","percipio-dev","12","Invalid-auth");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }

    // @Test
    public void validateInvalidParamsReturnNotFoundError(){
        String response = getAssignedContentForAnOrganization(404,"44435220-63b0-4608-9ab7-f398781903c9","random-contract","random-contract-line",suiteData.getJwt());
        assertEquals("Not Found", response);
        log.info(response);
    }

    //@Test
    public void validateGetAssignedContentForAnOrganizationSchema() throws Exception{
        String schema = SchemaHelper.loadSchema("getAssignedContentForOrganizationSchema");
        given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(Get_ORGANIZATION_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                queryParam("orgId","44435220-63b0-4608-9ab7-f398781903b8").
                queryParam("contract","percipio-dev").
                queryParam("contractLine","12").
                contentType(ContentType.JSON).
                when().
                get().
                then().
                assertThat().body(JsonSchemaValidator.matchesJsonSchema(schema));

    }
}
