package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.form.ClaimManageFormConfig;
import com.hibiscusmc.hmcclaims.config.form.SubClaimManageFormConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

@Singleton
public class SubClaimManageForm extends ClaimManageForm {

    @Inject
    private ConfigHolder<SubClaimManageFormConfig> configHolder;

    private SubClaimManageFormConfig config;

    @Override
    public void loadConfig() {
        SubClaimManageFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        render(player, metadata, config, SubClaimManageForm.class);
    }

    @Override
    protected boolean renderSection(
            @NotNull String section, @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context,
            @NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimManageFormConfig config,
            @NotNull Map<String, String> data
    ) {
        if (!section.equals("inherit") || !(config instanceof SubClaimManageFormConfig sub)) {
            return super.renderSection(section, builder, context, player, metadata, config, data);
        }

        button(builder, context, sub.inheritButton(), player, data,
                () -> confirmInherit(player, metadata, sub, context, data));

        return true;
    }

    private void confirmInherit(
            @NotNull Player player, @NotNull GuiMetadata metadata, @NotNull SubClaimManageFormConfig config,
            @NotNull SharedContext context, @NotNull Map<String, String> data
    ) {
        Claim claim = metadata.claim();
        SubClaimManageFormConfig.Confirm confirm = config.inheritConfirm();

        formService.send(player, new ModalFormSpec(
                FormText.line(confirm.title(), player, data),
                FormText.block(confirm.content(), player, data),
                FormText.line(confirm.confirm(), player, data),
                FormText.line(confirm.cancel(), player, data),
                () -> {
                    claim.inheritPermissions();
                    storageHolder.get().claims().saveClaim(claim);

                    text.send(player, config.inheritSuccess(), data);

                    context.refresh();
                },
                context::refresh,
                context::refresh
        ));
    }
}