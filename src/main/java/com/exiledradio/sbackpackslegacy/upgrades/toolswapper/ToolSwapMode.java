/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.scorelegacy.util.INamedEnum;

public enum ToolSwapMode implements INamedEnum {
    ANY("any"),
    ONLY_TOOLS("onlyTools"),
    NO_SWAP("noSwap");

    private final String name;

    ToolSwapMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ToolSwapMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static ToolSwapMode fromName(String name) {
        for (ToolSwapMode value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return ANY;
    }
}
