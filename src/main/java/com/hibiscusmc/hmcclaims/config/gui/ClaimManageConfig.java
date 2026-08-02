package com.hibiscusmc.hmcclaims.config.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

public abstract class ClaimManageConfig extends GuiTemplate {

    public abstract GuiTitle title();

    public abstract int rows();

    public abstract GuiScreenType screenType();

    public abstract Map<String, Icon> extraIcons();

    public abstract SimpleIcon deleteIcon();

    public abstract SimpleIcon renameIcon();

    public abstract SimpleIcon lockIcon();

    public abstract ItemStack unlockIcon();

    public abstract SimpleIcon bannedIcon();

    public abstract SimpleIcon resizeIcon();

    public abstract SimpleIcon backIcon();

    public abstract Map<String, SimpleIcon> tabs();

    public abstract BaseListGuiConfig lowerGui();
}