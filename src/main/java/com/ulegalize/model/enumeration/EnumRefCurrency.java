package com.ulegalize.model.enumeration;

public enum EnumRefCurrency {
    CFA("CFA", "CFA"),
    CHF("CHF", "CHF"),
    CLP("CLP", "$"),
    CNY("CNY", "¥"),
    COP("COP", "$"),
    CUC("CUC", "$"),
    CEV("CEV", "$"),
    DJF("DJF", "Fdj"),
    DKK("DKK", "kr"),
    DOP("DOP", "$"),
    EKK("EKK", "kr"),
    EGP("EGP", "£"),
    ERN("ERN", "Nfk"),
    ETB("ETB", "Br"),
    EUR("EUR", "€"),
    FJD("FJD", "$"),
    FKP("FKP", "$"),
    GBP("GBP", "£"),
    USD("USD", "$");

    private String code;
    private String symbol;

    EnumRefCurrency(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    public static EnumRefCurrency fromCode(String code) {
        for (EnumRefCurrency enumDossierType : EnumRefCurrency.values()) {
            if (enumDossierType.getCode().equalsIgnoreCase(code))
                return enumDossierType;
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

}
