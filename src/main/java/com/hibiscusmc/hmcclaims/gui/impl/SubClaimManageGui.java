package com.hibiscusmc.hmcclaims.gui.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.config.gui.SubClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.storage.Storage;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.window.Window;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class SubClaimManageGui extends ClaimManageGui {

    @Inject
    private ConfigHolder<SubClaimManageConfig> configHolder;

    @Inject
    private StorageHolder storageHolder;

    private GuiTemplate.SimpleIcon inheritPermissionsIcon;
    private ItemStack inheritPermissionsSuccessIcon;

    @Override
    public void loadConfig() {
        SubClaimManageConfig config = configHolder.get();
        if (config == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        loadConfig(config);

        inheritPermissionsIcon = config.inheritPermissionsIcon();
        inheritPermissionsSuccessIcon = config.inheritPermissionsSucesssIcon();
    }

    @Override
    public void open(@NotNull Player player, Object... args) {
        Claim claim = (Claim) args[0];

        scheduler.scheduleAsync(() -> {
            Gui.Builder<?, ?> gui = Gui.builder();
            InventoryStructure invStructure = build(player, claim);
            List<String> structure = invStructure.structure();
            structure.set(inheritPermissionsIcon.slot(), "!");

            String[] structureArray = new String[rows];
            for (int r = 0; r < rows; r++) {
                List<String> rowList = structure.subList(r * 9, (r + 1) * 9);

                structureArray[r] = String.join("", rowList);
            }

            gui.setStructure(structureArray);

            gui.addIngredient('!', buildInheritIcon(claim));
            invStructure.builder().accept(gui);

            Gui lowerGui = screenType == GuiTemplate.GuiScreenType.FULL ? buildLowerGui(player) : null;
            Gui upperGui = gui.build();

            scheduler.schedule(() -> {
                Window.Builder.Normal.Split window = Window.builder()
                        .setTitle(TextUtil.parse(title, Map.of(
                                "claim_name", claim.name()
                        )))
                        .setUpperGui(upperGui);

                if (lowerGui != null) {
                    window.setLowerGui(lowerGui);
                }

                window.open(player);
            });
        });
    }

    private Item buildInheritIcon(@NotNull Claim claim) {
        AtomicBoolean processed = new AtomicBoolean(false);

        return Item.builder()
                .setItemProvider(p -> new ItemWrapper(processed.get() ? inheritPermissionsSuccessIcon : inheritPermissionsIcon.item()))
                .addClickHandler((it, click) -> {
                    if (processed.getAndSet(true)) {
                        return;
                    }

                    claim.inheritPermissions();

                    Storage storage = storageHolder.get();
                    storage.claims().saveClaim(claim);

                    it.notifyWindows();
                })
                .build();
    }
}