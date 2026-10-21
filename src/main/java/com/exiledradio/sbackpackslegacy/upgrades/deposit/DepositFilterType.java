/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.deposit;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum DepositFilterType implements INamedEnum {
    ALLOW("allow"),
    BLOCK("block"),
    INVENTORY("inventory");

    private final String name;

    DepositFilterType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public DepositFilterType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static DepositFilterType fromName(String name) {
        for (DepositFilterType value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return BLOCK;
    }
}
