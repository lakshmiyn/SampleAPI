package com.skillsoft.provisioning.api.testdata;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;


/*
 * This class holds the configuration data that is essentially hard coded for use across many tests.
 * Some of this data may be environment specific. (develop, release, master, etc.), other values may just be hard coded
 * if they don't need to change across environments.
 *
 * The environments share the same names as their git branches.
 */
public class SuiteData {

    private static SuiteData instance = null;

    public static SuiteData getData() {
        if (instance == null) {
            instance = new SuiteData();
            instance.load();
        }
        return instance;
    }

    private String environment = "dev";


    // Property file loaded based on environment system parameter,
    // getters are below for each property, we don't want the individual test methods to need to know that there are
    // properties files backing the environment specific configurations.

    Properties properties = new Properties();

    protected SuiteData() {
        load();
    }

    @Deprecated
    public void load() {
        String serverEnvironment = System.getProperty("server.environment");
        if (serverEnvironment != null) {
            this.environment = serverEnvironment;
        }

        try {
            String filename = String.format("env/%s.properties", this.environment);
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
            if (is == null) {
                throw new IOException("Cannot find resource: " + filename);
            }
            properties.load(is);

            System.out.println("USING: " + this.environment + ".properties");
            Enumeration keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = (String) properties.get(key);
                System.out.println(key + ": " + value);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error loading test properties", ex);
        }
    }

    public String getEnvironment() { return this.environment; }

    public String getProvisioningBaseUri(){
        return properties.getProperty("provisioning.baseUri");
    }

    public String getBizappsBaseUri(){
        return properties.getProperty("provisioning-bizapps.baseUri");
    }

    public String getJwt() { return properties.getProperty("jwt"); }

    public String getRelayJwt() { return properties.getProperty("relay.Jwt"); }

    public UUID getOrgId() {String orgId= properties.getProperty("orgId");
    return UUID.fromString(orgId); }

    public String getLoginUrl() {
        return properties.getProperty("loginUrl");
    }

    public String getCollectionName() {
        return properties.getProperty("collectionName");
    }

}
