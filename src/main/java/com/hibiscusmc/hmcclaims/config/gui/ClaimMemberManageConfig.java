package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import lombok.Getter;
import org.bukkit.Material;
import team.hypox.config.core.annotation.Section;

import java.util.List;
import java.util.Map;

@Getter
@Section
public class ClaimMemberManageConfig extends GuiTemplate {

    private LowerGui lowerGui = new LowerGui();

    @Getter
    @Section
    public static class LowerGui {

        private Map<String, Icon> extraIcons = MapUtil.ordered(
                "example-icon", new Icon(22)
        );

        private SimpleIcon kickIcon = new SimpleIcon(ConfigItem.of(
                Material.BARRIER, "Kick Member", List.of("", "<white>Left-Click <gray>to delete role")
        ), 2);

        private ConfigItem cantKickIcon = ConfigItem.of(
                Material.BARRIER, "Kick Member", List.of("", "<red>You can't kick this member")
        );

        private SimpleIcon banIcon = new SimpleIcon(ConfigItem.of(
                Material.BARRIER, "Ban Member", List.of("", "<white>Left-Click <gray>to ban this member")
        ), 6);

        private ConfigItem cantBanIcon = ConfigItem.of(
                Material.BARRIER, "Ban Member", List.of("", "<red>You can't ban this member")
        );

        private SimpleIcon backIcon = new SimpleIcon(ConfigItem.of(
                Material.ARROW, "Back", List.of("", "<white>Left-Click <gray>to go back")
        ), 18);
    }
}