package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;
import java.util.Map;

@ToString
@ConfigSerializable
@Getter(onMethod_ = {@Override})
public class SubClaimManageConfig extends ClaimManageConfig {

    private String title = "<claim_name>";

    private int rows = 6;

    @Setting("extra-icons")
    private Map<String, Icon> extraIcons = Map.of(
            "example-icon", new Icon()
    );

    @Setting("delete-icon")
    private SimpleIcon deleteIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Delete claim", List.of("", "<white>Left-Click <gray>to delete claim")
    ), 8);

    @Setting("rename-icon")
    private SimpleIcon renameIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Rename claim", List.of("", "<white>Left-Click <gray>to rename claim")
    ), 18);

    @Setting("lock-icon")
    private SimpleIcon lockIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Lock claim", List.of("", "<white>Left-Click <gray>to make your claim private")
    ), 20);

    @Setting("unlock-icon")
    private SimpleIcon unlockIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Unlock claim", List.of("", "<white>Left-Click <gray>to make your claim public")
    ), 20);

    @Setting("banned-icon")
    private SimpleIcon bannedIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Banned members", List.of("", "<white>Left-Click <gray>to show the list of banned members")
    ), 22);

    @Getter
    @Setting("inherit-icon")
    private SimpleIcon inheritPermissionsIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Inherit Permissions", List.of("", "<white>Left-Click <gray>to inherit the permissions of the", "<gray>main claim. This will add every member, role and permission", "<gray>from the main claim to this claim.")
    ), 24);

    @Getter
    @Setting("inherit-success-icon")
    private SimpleIcon inheritPermissionsSucesssIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Inherit Permissions", List.of("", "<green>Permissions inherited successfully!")
    ), 24);

    @Setting("resize-icon")
    private SimpleIcon resizeIcon = new SimpleIcon(ItemUtil.build(
            Material.OAK_BUTTON, "Resize claim", List.of("", "<white>Left-Click <gray>to resize claim borders")
    ), 26);

    @Setting("back-icon")
    private SimpleIcon backIcon = new SimpleIcon(ItemUtil.build(
            Material.BARRIER, "Back", List.of("", "<white>Left-Click <gray>to go back")
    ), 45);

    private Map<String, SimpleIcon> tabs = Map.of(
            "members-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "Members", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 0),
            "roles-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Roles", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 1),
            "settings-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Settings", List.of("", "<white>Left-Click <gray>to go to this tab")
            ), 2),
            "manage-tab", new SimpleIcon(ItemUtil.build(
                    Material.STONE_BUTTON, "<gray>Manage", List.of("", "<red>You're here!")
            ), 3)
    );
}