package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@Getter
@ConfigSerializable
public class ClaimMemberManageConfig extends GuiTemplate {

    @Setting("lower-gui")
    private LowerGui lowerGui = new LowerGui();

    @Getter
    @ConfigSerializable
    public static class LowerGui {

        @Setting("extra-icons")
        private Map<String, Icon> extraIcons = Map.of(
                "example-icon", new Icon(22)
        );

        @Setting("kick-icon")
        private SimpleIcon kickIcon = new SimpleIcon(ItemUtil.build(
                Material.BARRIER, "Kick Member", List.of("", "<white>Left-Click <gray>to delete role")
        ), 2);

        @Setting("cant-kick-icon")
        private ItemStack cantKickIcon = ItemUtil.build(
                Material.BARRIER, "Kick Member", List.of("", "<red>You can't kick this member")
        );

        @Setting("ban-icon")
        private SimpleIcon banIcon = new SimpleIcon(ItemUtil.build(
                Material.BARRIER, "Ban Member", List.of("", "<white>Left-Click <gray>to ban this member")
        ), 6);

        @Setting("cant-ban-icon")
        private ItemStack cantBanIcon = ItemUtil.build(
                Material.BARRIER, "Ban Member", List.of("", "<red>You can't ban this member")
        );
    }
}