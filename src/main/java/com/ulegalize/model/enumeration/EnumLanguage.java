package com.ulegalize.model.enumeration;

public enum EnumLanguage {
    FR("fr"),
    NL("nl"),
    EN("en");

    private String shortCode;

    EnumLanguage(String shortCode) {
        this.shortCode = shortCode;
    }

    public static EnumLanguage fromshortCode(String shortCode) {
        for (EnumLanguage language : EnumLanguage.values()) {
            if (language.getShortCode().equalsIgnoreCase(shortCode)) {
                return language;
            }
        }

        return null;
    }

    public String getShortCode() {
        return shortCode;
    }
}
