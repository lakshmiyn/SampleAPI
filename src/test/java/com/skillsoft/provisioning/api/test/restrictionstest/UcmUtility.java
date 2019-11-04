package com.skillsoft.provisioning.api.test.restrictionstest;

import com.skillsoft.provisioning.api.testdata.SuiteData;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.annotations.findby.How;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class UcmUtility extends PageObject{
    private static final Logger log = LoggerFactory.getLogger(UcmUtility.class);
    public UcmUtility(WebDriver driver) {
        super(driver);
    }
    SuiteData suiteData = SuiteData.getData();

    @FindBy(how = How.XPATH, using = "//input[@name='loginName']")
    private WebElementFacade userNameEditBox;

    @FindBy(how = How.XPATH, using = "//input[@name='password']")
    private WebElementFacade passWordEditBox;

    @FindBy(how = How.XPATH, using = "//button[.='Log in']")
    private WebElementFacade loginButton;

    @FindBy(how = How.XPATH, using = "//button[.='Licenses']")
    private WebElementFacade licensesMenu;

    @FindBy(how = How.XPATH, using = "//a[@href='/admin/licenses/associations']")
    private WebElementFacade audienceAssociationSubMenu;

    @FindBy(how = How.XPATH, using = "//a[@href='/admin/licenses/associations/new']")
    private WebElementFacade newAudienceAssociationButton;

    @FindBy(how = How.XPATH, using = "//*[@id='audienceAssociation-audienceId']")
    private WebElementFacade audienceAssociationDropdown;

    @FindBy(how = How.XPATH, using = "//*[@id='audienceAssociation-licensePoolSetId']")
    private WebElementFacade licensepoolAssociationDropdown;

    @FindBy(how = How.XPATH, using = "//button[.='Create']")
    private WebElementFacade createAssocationButton;

    @FindBy(how = How.XPATH, using = "//input[@name='search_query']")
    private WebElementFacade searchEditBox;

    @FindBy(how = How.XPATH, using = "//button[@aria-label='Search']")
    private WebElementFacade searchButton;

    @FindBy(how = How.XPATH, using = "//span[.='My Profile']")
    private WebElementFacade myProfileMenu;

    @FindBy(how = How.XPATH, using = "//a[.='Log Out']")
    private WebElementFacade logOutSubMenu;


    public void loginAsAdmin(String username, String password) {
        userNameEditBox.withTimeoutOf(60, TimeUnit.SECONDS).waitUntilVisible().sendKeys(username);
        passWordEditBox.waitUntilVisible().sendKeys(password);
        loginButton.waitUntilClickable().click();

    }

    public void associateAudienceToLicensePool() {
        licensesMenu.withTimeoutOf(60, TimeUnit.SECONDS).waitUntilVisible().waitUntilClickable().click();
        audienceAssociationSubMenu.waitUntilClickable().click();
        newAudienceAssociationButton.waitUntilVisible().waitUntilClickable().click();
        audienceAssociationDropdown.waitUntilClickable().selectByVisibleText("All Users");
        licensepoolAssociationDropdown.waitUntilClickable().selectByVisibleText(suiteData.getCollectionName());
        createAssocationButton.waitUntilEnabled().click();

    }

    public void logout(){
        myProfileMenu.waitUntilClickable().click();
        logOutSubMenu.waitUntilClickable().click();
    }

    public boolean searchLibrary(String channelName, String course) throws Exception{
        boolean present;
        Thread.sleep(30000);
        getDriver().navigate().refresh();
        searchEditBox.withTimeoutOf(60, TimeUnit.SECONDS).waitUntilVisible().waitUntilClickable().then().sendKeys(channelName);
        searchButton.waitUntilClickable().click();
        try {
            find(By.xpath("//h2/a[contains(text(),'" + channelName + "')]")).waitUntilPresent().click();
            Thread.sleep(5000);
            present = find(By.xpath("//h2/a[contains(text(),'" + course + "')]")).isDisplayed();
            log.info(find(By.xpath("//h2/a[contains(text(),'" + course + "')]")).getText() + " is present");

        } catch (NoSuchElementException e) {
            log.info(course + " is not present");
            present = false;
        }
        return present;
    }
}
