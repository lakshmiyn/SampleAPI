package com.skillsoft.provisioning.api.test.restrictionstest;

import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

public class SerenityStepLibrary extends ScenarioSteps {

    public UcmUtility ucmUtility;
    //This will create driver object and pass it ; extended functionality of ScenarioSteps

    @Step
    public void loginAsAdmin(String username, String password) {
        getDriver().manage().window().maximize();
        ucmUtility.open();
        ucmUtility.loginAsAdmin(username, password);
    }

    @Step
    public void associateAudienceToLicensePool() {
        ucmUtility.associateAudienceToLicensePool();
    }

    @Step
    public void logout() {
        ucmUtility.logout();
        getDriver().quit();
    }

    @Step
    public boolean searchLibrary(String channelName, String course) throws Exception{
        return ucmUtility.searchLibrary(channelName, course);
    }
}
