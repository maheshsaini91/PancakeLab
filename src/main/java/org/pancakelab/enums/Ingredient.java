package org.pancakelab.enums;

public enum Ingredient {
    DARK_CHOCOLATE("dark chocolate"),
    MILK_CHOCOLATE("milk chocolate"),
    WHIPPED_CREAM("whipped cream"),
    HAZELNUTS("hazelnuts");

    private final String displayName;

    Ingredient(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
