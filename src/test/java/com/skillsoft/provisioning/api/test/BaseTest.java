package com.skillsoft.provisioning.api.test;

import com.skillsoft.provisioning.api.testdata.SuiteData;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matcher;
import org.junit.*;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

/*
 * This is a common parent for all tests
 */
public class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    protected static final SuiteData suiteData = SuiteData.getData();


    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void resetRestAssured() {
        RestAssured.reset();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Before
    public void runBeforeMethod() throws NoSuchMethodException {

        log.info("runBeforeMethod called before method {}.{}", this.getClass().getName(), name.getMethodName());
        try {
            Method testMethod = this.getClass().getMethod(name.getMethodName());

            //If the tests are run on staging, then only run those tests which have
            //@RunONStaging Annotation. All others tests should be ignored.
            if (suiteData.getEnvironment().equals("stg")) {
                assumeTrue(testMethod.isAnnotationPresent(RunOnStaging.class));
            }
        }
        catch (AssumptionViolatedException exc) {
            log.info("skipping test for staging {} ", name.getMethodName());
            throw exc;
        }
        catch (NoSuchMethodException e) {
            log.info("skipping test for staging {} ", name.getMethodName());
            throw e;
        }

    }

    protected boolean environmentIsOneOf(String... environments) {
        return environmentMatcher().matches(environments);
    }

    private Matcher<String[]> environmentMatcher() {
        return hasItemInArray(suiteData.getEnvironment());
    }

    /**
     * Ignore this test if we're not running in one of the given environments.
     *
     * @param environments
     */
    protected void ignoreUnlessEnvironmentIs(String... environments) {
        log.debug("About to skip the rest of this test unless {} is one of {}", suiteData.getEnvironment(), environments);
        try {
            assumeThat(environments, environmentMatcher());
        } catch (AssumptionViolatedException e) {
            log.info("Skipping the rest of this test because environment '{}' is not one of: {}", suiteData.getEnvironment(), environments);
            throw e;
        }
    }

    public static RequestSpecification bearer(String jwt) {
        return newRequestSpecBuilder().addHeader("Authorization", String.format("bearer %s", jwt)).build();
    }

    public static RequestSpecification serviceAccount(String orgId, String serviceAccountId) {
        return newRequestSpecBuilder()
            .addHeader("X-AUTHOR-ORG-ID", orgId)
            .addHeader("X-AUTHOR-SERVICE-ACCOUNTID", serviceAccountId)
            .build();
    }

    public static RequestSpecification serviceName(String serviceName) {
        return newRequestSpecBuilder()
            .addHeader("X-Author-Servicename", serviceName)
            .build();
    }

    public static RequestSpecification serviceName() {
        return serviceName("PINTS_INTEGRATION_TESTS");
    }

    /**
     * Just for adding configuration to ALL request-spec builders we make from convenience methods.
     *
     * @return A new RequestSpecBuilder
     */
    protected static RequestSpecBuilder newRequestSpecBuilder() {
        return new RequestSpecBuilder();
    }

    public static ResponseSpecification hasErrorCount(int count) {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectBody("errors.code", hasSize(1))
            .build();
    }

    public static ResponseSpecification hasMessage(String message) {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectBody("message", hasItem(message))
            .build();
    }

    public static ResponseSpecification hasError(String resource, String code) {
        return new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectBody("errors.resource", hasItem(resource))
            .expectBody("errors.code", hasItem(code))
            .build();
    }
    protected static void skipTestsInThisClassOnRelEnv() {
        try {
            assumeFalse(suiteData.getEnvironment().equals("rel"));

        } catch (AssumptionViolatedException exc) {
            log.info("Skipping these tests on release environment");
            throw exc;
        }
    }

}
