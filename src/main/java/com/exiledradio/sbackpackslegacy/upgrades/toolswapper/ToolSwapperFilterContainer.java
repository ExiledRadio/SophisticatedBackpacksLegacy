/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.toolswapper;

import com.exiledradio.sbackpackslegacy.SophisticatedBackpacksLegacy;
import com.exiledradio.scorelegacy.common.gui.IServerUpdater;
import com.exiledradio.scorelegacy.upgrades.FilterLogicContainerBase;
import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.Slot;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** The weapon filter slot, then one slot per tool type (with an outline icon where one exists). */
public class ToolSwapperFilterContainer extends FilterLogicContainerBase<ToolSwapperFilterLogic, ToolFilterSlot> {
    public static final Set<String> TOOL_TYPES_WITH_BACKGROUND = ImmutableSet.of("axe", "hammer", "pickaxe", "shovel", "hoe");
    public static final String EMPTY_WEAPON_SLOT_SPRITE = SophisticatedBackpacksLegacy.MODID + ":item/empty_weapon_slot";

    public static String getEmptyToolSlotSprite(String toolType) {
        return SophisticatedBackpacksLegacy.MODID + ":item/empty_" + toolType + "_slot";
    }

    public ToolSwapperFilterContainer(IServerUpdater serverUpdater, Supplier<ToolSwapperFilterLogic> filterLogic, Consumer<Slot> addSlot) {
        super(serverUpdater, filterLogic, addSlot);
        ToolFilterSlot weaponFilterSlot = new ToolFilterSlot(() -> filterLogic.get().getWeaponFilter(), stack -> filterLogic.get().setWeaponFilter(stack), s -> true);
        weaponFilterSlot.setBackgroundName(EMPTY_WEAPON_SLOT_SPRITE);
        filterSlots.add(weaponFilterSlot);
        for (String toolType : filterLogic.get().getToolFilterTypes()) {
            ToolFilterSlot toolFilterSlot = new ToolFilterSlot(() -> filterLogic.get().getToolFilter(toolType), stack -> filterLogic.get().setToolFilter(toolType, stack),
                    s -> ToolTypes.of(s).contains(toolType));
            if (TOOL_TYPES_WITH_BACKGROUND.contains(toolType)) {
                toolFilterSlot.setBackgroundName(getEmptyToolSlotSprite(toolType));
            }
            toolFilterSlot.setEmptyTooltip(StringUtils.capitalize(toolType));
            filterSlots.add(toolFilterSlot);
        }
        filterSlots.forEach(addSlot);
    }
}
