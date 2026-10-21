/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum InventoryOrder implements INamedEnum {
    MAIN_FIRST("main_first"),
    INCEPTED_FIRST("incepted_first");

    private final String name;

    InventoryOrder(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public InventoryOrder next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static InventoryOrder fromName(String name) {
        for (InventoryOrder value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return MAIN_FIRST;
    }
}
