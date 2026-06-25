package com.hibiscusmc.hmcclaims.config.gui;

import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@Getter
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public class GuiTemplate {

    @Getter
    @ConfigSerializable
    public static class DynamicIcon {

        private String name;

        private List<String> lore;

        public DynamicIcon() {
        }

        protected DynamicIcon(String name, List<String> lore) {
            this.name = name;
            this.lore = lore;
        }
    }

    @Getter
    @ConfigSerializable
    public static class SimpleIcon {

        private ItemStack item = ItemStack.of(Material.STONE);

        private int slot = 0;

        public SimpleIcon() {
        }

        protected SimpleIcon(ItemStack item, int slot) {
            this.item = item;
            this.slot = slot;
        }
    }

    @Getter
    @ConfigSerializable
    public static class Icon {

        private ItemStack item = ItemUtil.build(Material.PAPER, "<aqua>Example Icon", List.of(
                "",
                "<gray>This is an example icon!"
        ));

        private int slot = 40;

        @Setting("left-click-actions")
        protected List<Action> leftClickActions = List.of(
                Action.parse("command: say hello!")
        );

        @Setting("right-click-actions")
        protected List<Action> rightClickActions = List.of(
                Action.parse("console: say %player_name% says hello!"),
                Action.parse("message: <green>saying hello on your behalf, <white>%player_name%</white>!")
        );
    }
}