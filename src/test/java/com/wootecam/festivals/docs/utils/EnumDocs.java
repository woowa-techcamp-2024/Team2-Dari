package com.wootecam.festivals.docs.utils;

import java.util.Map;


public class EnumDocs {

    Map<String, String> GreetStatus;

    public EnumDocs() {
    }

    public EnumDocs(Map<String, String> greetStatus) {
        this.GreetStatus = greetStatus;
    }

    public Map<String, String> getGreetStatus() {
        return GreetStatus;
    }

    public void setGreetStatus(Map<String, String> greetStatus) {
        GreetStatus = greetStatus;
    }

    @Override
    public String toString() {
        return "EnumDocs{" +
                "GreetStatus=" + GreetStatus +
                '}';
    }
}
