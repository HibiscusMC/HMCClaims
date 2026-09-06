package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.ClaimDeleteFormConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

@Singleton
public class ClaimDeleteForm implements BaseForm {

    @Inject
    private ConfigHolder<ClaimDeleteFormConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private TextUtil text;

    private ClaimDeleteFormConfig config;

    @Override
    public void loadConfig() {
        ClaimDeleteFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        Map<String, String> data = placeholders.claimInfo(claim);

        Runnable cancel = () -> {
            if (!formService.back(player)) {
                // Reached straight from /claim delete, so there is nothing to return to.
                formService.clear(player);
            }
        };

        formService.send(player, new ModalFormSpec(
                FormText.line(config.title().text(), player, Map.of(
                        "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                )),
                FormText.block(
                        claim.main() == null ? config.content() : config.subClaimContent(), player, data
                ),
                FormText.line(config.confirm(), player, data),
                FormText.line(config.cancel(), player, data),
                () -> {
                    claimManager.deleteClaim(claim);

                    text.send(player, messagesHolder.get().claims().deleted(), Map.of(
                            "claim_name", claim.name()
                    ));

                    // The claim this menu tree was built around is gone, so start over.
                    formService.root(player, () -> forms.get(ClaimListForm.class).send(player));
                },
                cancel,
                cancel
        ));
    }
}