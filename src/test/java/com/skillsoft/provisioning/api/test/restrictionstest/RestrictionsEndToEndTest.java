package com.skillsoft.provisioning.api.test.restrictionstest;

import com.skillsoft.provisioning.api.test.BaseTest;
import com.skillsoft.provisioning.api.test.licensepool.LicensePoolPayload;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.ManagedPages;
import net.thucydides.core.annotations.Steps;
import net.thucydides.core.pages.Pages;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.createLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.CreateLicensePoolTest.deleteLicensePool;
import static com.skillsoft.provisioning.api.test.licensepool.UpdateLicensePoolTest.patchLicensePoolByContract;
import static org.junit.Assert.*;


//@RunWith(SerenityRunner.class)
public class RestrictionsEndToEndTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(RestrictionsEndToEndTest.class);
    private static final String DOMAIN = "restrictions-test";
    private static final String USERNAME = "endtoendadmin";
    private static final String PASSWORD = "Test1234";
    public static final List<Integer> contractItems = new ArrayList<Integer>();
    private LicensePoolPayload licensePoolPayload;

    @Steps
    private SerenityStepLibrary serenityStepLibrary;

    @Managed
    WebDriver driver;

    @ManagedPages
    public Pages pages;

    @Before
    public void setUpUrlBasedOnEnvironment() {
        String newloginUrl = "https://" + DOMAIN + "." + suiteData.getLoginUrl();
        pages.getConfiguration().getEnvironmentVariables().setProperty(ThucydidesSystemProperty.WEBDRIVER_BASE_URL.getPropertyName(), newloginUrl);
    }

    @After
    public void deleteAllTheLpCreatedForThisTest() {
        for (Integer contractItem : contractItems) {
            deleteLicensePool("allcollections" + contractItem, contractItem, 1);
        }
        log.info("LPs created for this test are deleted");
    }

    private LicensePoolPayload createLicensePayload(int uniqueNumber) {
        contractItems.add(uniqueNumber);
        LicensePoolPayload licensePoolPayload = new LicensePoolPayload();
        licensePoolPayload.setContract("allcollections" + uniqueNumber);
        licensePoolPayload.setContractLine(uniqueNumber);
        licensePoolPayload.setKitId("qadomain" + uniqueNumber);
        licensePoolPayload.setPeopleSoftOrgId("psoft" + uniqueNumber);
        licensePoolPayload.setSalesForceCustId("salesforceid" + uniqueNumber);
        return licensePoolPayload;

    }


    @Ignore
    public void royaltyRestrictionTest() throws Exception {
        log.info("**************Creating LicensePool for the Org" + suiteData.getOrgId() + "***************");
        int payloadUniqueNumber = new Random().nextInt(90000) + 10000;
        licensePoolPayload = createLicensePayload(payloadUniqueNumber);
        licensePoolPayload.setCollectionId("CRS_BusinessSkills");
        String response = createLicensePool(200, licensePoolPayload);
        log.info("**************Created LicensePool for the Org*************");
        log.info(response);
        serenityStepLibrary.loginAsAdmin(USERNAME, PASSWORD);
        serenityStepLibrary.associateAudienceToLicensePool();
        assertTrue(serenityStepLibrary.searchLibrary("Personal Productivity", "Forming New Habits"));
        serenityStepLibrary.logout();
        log.info("**************Updating LicensePool for the Org" + suiteData.getOrgId() + "***************");
        String patchPayload = "{\"royaltyRestrictionsApply\":\"true\",\"contractValueUSD\": 2}";
        String updateLpResponse = patchLicensePoolByContract(200, "allcollections" + payloadUniqueNumber, payloadUniqueNumber, patchPayload);
        log.info("**************Updated LicensePool for the Org*************");
        assertEquals(response, updateLpResponse);
        serenityStepLibrary.loginAsAdmin(USERNAME, PASSWORD);
        assertFalse(serenityStepLibrary.searchLibrary("Personal Productivity", "Forming New Habits"));
        serenityStepLibrary.logout();

    }


}
