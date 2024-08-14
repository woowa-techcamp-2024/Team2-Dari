package com.wootecam.festivals.docs.utils;

import java.util.Map;


public class EnumDocs {

    private Map<String, String> OrganizationRole;

    public EnumDocs() {
    }

    public EnumDocs(Map<String, String> organizationRole) {
        OrganizationRole = organizationRole;
    }

    public Map<String, String> getOrganizationRole() {
        return OrganizationRole;
    }

    public void setOrganizationRole(Map<String, String> organizationRole) {
        OrganizationRole = organizationRole;
    }
}
