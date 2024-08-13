package com.wootecam.festivals.docs.utils;

import java.util.Map;


public class EnumDocs {

    private Map<String, String> GreetStatus;
    private Map<String, String> OrganizationRole;

    public EnumDocs() {
    }

    public EnumDocs(Map<String, String> greetStatus, Map<String, String> organizationRole) {
        GreetStatus = greetStatus;
        OrganizationRole = organizationRole;
    }

    public Map<String, String> getGreetStatus() {
        return GreetStatus;
    }

    public void setGreetStatus(Map<String, String> greetStatus) {
        GreetStatus = greetStatus;
    }

    public Map<String, String> getOrganizationRole() {
        return OrganizationRole;
    }

    public void setOrganizationRole(Map<String, String> organizationRole) {
        OrganizationRole = organizationRole;
    }
}
