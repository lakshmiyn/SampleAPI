package com.skillsoft.provisioning.api.test.organization;

import com.jayway.jsonpath.JsonPath;
import com.skillsoft.provisioning.api.test.RunOnStaging;
import com.skillsoft.provisioning.api.testdata.SchemaHelper;
import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.jruby.RubyProcess;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetLocalesTest {

    private static final SuiteData suiteData = SuiteData.getData();
    private static final Logger log = LoggerFactory.getLogger(GetLocalesTest.class);
    public static final String GET_LOCALE_PATH="/v1/organizations/{orgId}/locales";
    private static ArrayList<String> Locales = new ArrayList<String>();
    private static ArrayList<String> ExpLocales = new ArrayList<String>();




    private String getLocalesForSpeciOrg(int expectedCode, String jwt) {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LOCALE_PATH).
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
    @RunOnStaging
    public void GetLocalesReturns_200() {
        String response = getLocalesForSpeciOrg(200, suiteData.getJwt());
        log.info("Log"+response);
        int responseLength = JsonPath.parse(response).read("$.length()");
        System.out.println(responseLength);
        Locales = JsonPath.parse(response).read("$.[*]");
        System.out.println(Locales);
        List<Map> apiResponse = JsonPath.parse(Locales).read("$.[*]");

        String[] ExpectedLocales = {"en", "fr", "de", "es", "pt", "BR", "pt-BR"};
        //ExpLocales=["en", "fr", "de", "es"];
        if (responseLength<1){
            log.info("No Locales present for the Org");
        }
        else {
            log.info(responseLength+" Locales mapped to the Org "+SuiteData.getData().getOrgId());
        }
    }
    @Test
    public void GetLocalesReturns_404() {
        String resp = given().
                baseUri(suiteData.getProvisioningBaseUri()).
                basePath(GET_LOCALE_PATH).
                header("Authorization", "Bearer " + suiteData.getJwt()).
                pathParam("orgId","8745566a-9af9-4bfb-b817-1e5949e87c0a").
                contentType(ContentType.JSON).
                when().
                get().
                then().
                statusCode(equalTo(404)).extract().response().asString();
        log.info(resp);

    }
    @Test
    public void validateInvalidAuthorizationReturns401(){
        String response = getLocalesForSpeciOrg(401, "Invalid-Auth-Token");
        assertEquals("Failed verifying signature of JWT", response);
        log.info(response);
    }


}
