/*
 * Modified from Sophisticated Backpacks (c) P3pp3rF1y, GPLv3. Ported to 1.12.2 by ExiledRadio.
 */
package com.exiledradio.sbackpackslegacy.upgrades.inception;

import com.exiledradio.scorelegacy.api.IStorageWrapper;

import com.exiledradio.scorelegacy.api.IUpgradeWrapperAccessor;
import com.exiledradio.sbackpackslegacy.backpack.wrapper.BackpackWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Upgrade lookup that also returns the upgrades of backpacks inside this one. */
public class InceptionWrapperAccessor implements IUpgradeWrapperAccessor {
    private final Map<Class<?>, List<?>> interfaceWrappers = new HashMap<>();
    private final Map<Class<?>, List<?>> mainBackpackInterfaceWrappers = new HashMap<>();
    private final IStorageWrapper storageWrapper;
    private final SubBackpacksHandler subBackpacksHandler;

    public InceptionWrapperAccessor(IStorageWrapper storageWrapper, SubBackpacksHandler subBackpacksHandler) {
        this.storageWrapper = storageWrapper;
        this.subBackpacksHandler = subBackpacksHandler;
        addRefreshCallbacks(subBackpacksHandler.getSubBackpacks());
        subBackpacksHandler.addBeforeRefreshListener(this::removeCallBacks);
        subBackpacksHandler.addRefreshListener(subBackpacks -> {
            clearCache();
            addRefreshCallbacks(subBackpacks);
        });
    }

    private void addRefreshCallbacks(Collection<BackpackWrapper> subBackpacks) {
        subBackpacks.forEach(sb -> sb.getUpgradeHandler().setRefreshCallBack(this::clearCache));
    }

    private void removeCallBacks(Collection<BackpackWrapper> subBackpacks) {
        subBackpacks.forEach(sb -> sb.getUpgradeHandler().setRefreshCallBack(null));
    }

    @Override
    public <T> List<T> getWrappersThatImplement(Class<T> upgradeClass) {
        //noinspection unchecked
        return (List<T>) interfaceWrappers.computeIfAbsent(upgradeClass, this::collect);
    }

    @Override
    public <T> List<T> getWrappersThatImplementFromMainBackpack(Class<T> upgradeClass) {
        //noinspection unchecked
        return (List<T>) mainBackpackInterfaceWrappers.computeIfAbsent(upgradeClass, uc -> storageWrapper.getUpgradeHandler().getListOfWrappersThatImplement(uc));
    }

    private <T> List<T> collect(Class<T> upgradeClass) {
        List<T> ret = new ArrayList<>(storageWrapper.getUpgradeHandler().getListOfWrappersThatImplement(upgradeClass));
        subBackpacksHandler.getSubBackpacks().forEach(sbp -> ret.addAll(sbp.getUpgradeHandler().getWrappersThatImplement(upgradeClass)));
        return ret;
    }

    @Override
    public void onBeforeDeconstruct() {
        removeCallBacks(subBackpacksHandler.getSubBackpacks());
    }

    @Override
    public void clearCache() {
        interfaceWrappers.clear();
        mainBackpackInterfaceWrappers.clear();
    }
}
