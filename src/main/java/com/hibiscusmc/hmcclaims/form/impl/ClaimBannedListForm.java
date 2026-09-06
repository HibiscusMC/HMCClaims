package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.ClaimBannedListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormImages;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.MapUtil;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimBannedListForm implements BaseForm {

    private final static String STATE_KEY = "claim-banlist";

    @Inject
    private ConfigHolder<ClaimBannedListFormConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;
    @Inject
    private FormInputs inputs;

    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private SchedulerUtil scheduler;
    @Inject
    private TextUtil text;

    private ClaimBannedListFormConfig config;

    @Override
    public void loadConfig() {
        ClaimBannedListFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player, @NotNull GuiMetadata metadata) {
        render(player, metadata);
    }

    private void render(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();

        scheduler.scheduleAsync(() -> {
            State state = formService.state(player, STATE_KEY, State::new);

            List<ClaimMember> all = claim.bannedMembers().stream()
                    .sorted(Comparator.comparingLong(member -> member.joinedTimestamp().getEpochSecond()))
                    .toList();

            int limit = state.showAll ? -1 : config.maxEntries();
            boolean truncated = limit >= 0 && all.size() > limit;
            List<ClaimMember> shown = truncated ? all.subList(0, limit) : all;

            scheduler.schedule(() -> {
                SharedContext context = new SharedContext(
                        player, metadata, ClaimBannedListForm.class, formService, forms, () -> render(player, metadata)
                ).navigation(config.nav())
                        .extraButtons(config.extraButtons())
                        .backButton(config.backButton());

                SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                        .title(FormText.line(config.title().text(), player, Map.of(
                                "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                        )))
                        .content(FormText.block(
                                all.isEmpty() ? config.emptyContent() : config.content(), player, Map.of(
                                        "total", String.valueOf(all.size()),
                                        "shown", String.valueOf(shown.size())
                                )
                        ));

                for (String section : config.order()) {
                    switch (section) {
                        case "ban-member" -> builder.button(
                                FormText.line(config.banMemberButton().text(), player),
                                context.image(config.banMemberButton().image(), null),
                                () -> banMember(player, metadata)
                        );

                        case "banned" -> {
                            shown.forEach(member -> builder.button(navigating(
                                    FormText.block(config.bannedEntry().text(), player, bannedPlaceholders(member)),
                                    FormImages.resolvePlayer(
                                            config.bannedEntry().image(), member.lastKnownName(),
                                            member.uuid().toString(), Material.PLAYER_HEAD,
                                            formService.materialImageFallback()
                                    ),
                                    context,
                                    () -> openActions(player, metadata, member)
                            )));

                            if (truncated) {
                                builder.button(
                                        FormText.line(config.moreButton().text(), player, Map.of(
                                                "remaining", String.valueOf(all.size() - shown.size())
                                        )),
                                        context.image(config.moreButton().image(), null),
                                        () -> {
                                            state.showAll = true;
                                            render(player, metadata);
                                        }
                                );
                            }
                        }

                        default -> renderShared(section, builder, context);
                    }
                }

                formService.send(player, builder.build());
            });
        });
    }

    private void openActions(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimMember member) {
        FormTemplate.SubForm actions = config.bannedActions();
        Map<String, String> data = bannedPlaceholders(member);
        Runnable reopen = () -> openActions(player, metadata, member);

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(actions.title().text(), player, Map.of(
                        "name", FormText.shorten(member.lastKnownName(), actions.title().maxLength())
                )))
                .content(FormText.block(actions.content(), player, data));

        for (Map.Entry<String, FormTemplate.Button> entry : ordered(actions.buttons(), "unban").entrySet()) {
            FormTemplate.Button button = entry.getValue();
            String label = FormText.line(button.text(), player, data);

            switch (entry.getKey()) {
                case "unban" -> builder.button(label, image(button.image(), formService), () -> {
                    Claim claim = metadata.claim();

                    claim.removeMember(member.uuid());
                    storageHolder.get().claims().saveMembers(claim);

                    formService.back(player);
                });

                case "back" ->
                        builder.button(label, image(button.image(), formService), () -> formService.back(player));

                default -> builder.button(label, image(button.image(), formService), reopen);
            }
        }

        formService.send(player, builder.build());
    }

    private void banMember(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        Runnable reopen = () -> render(player, metadata);

        inputs.promptPlayer(
                player, inputs.config().banMember(), Map.of("claim_name", claim.name()),
                target -> {
                    Messages messages = messagesHolder.get();

                    ClaimMember member = claim.getMember(target.id())
                            .orElseGet(() -> claim.addMember(target));

                    ClaimMember self = claim.getMember(player.getUniqueId()).orElse(null);

                    if (member.uuid().equals(player.getUniqueId()) || (self != null && !self.canManage(member))) {
                        text.send(player, messages.claims().cantBanMember());
                        reopen.run();
                        return;
                    }

                    if (member.banned()) {
                        text.send(player, messages.claims().memberAlreadyBanned());
                        reopen.run();
                        return;
                    }

                    member.role(claim.roleRegistry().defaultRole());
                    member.permissions(new Object2BooleanArrayMap<>());
                    member.joinedTimestamp(Instant.now());
                    member.banned(true);

                    storageHolder.get().claims().saveMembers(claim);

                    text.send(player, messages.claims().memberBanned(), Map.of(
                            "name", target.name(),
                            "player_head", "",
                            "claim", claim.name()
                    ));

                    reopen.run();
                },
                reopen
        );
    }

    private static final class State {

        private boolean showAll;
    }

    @NotNull
    private Map<String, String> bannedPlaceholders(@NotNull ClaimMember member) {
        return MapUtil.add(
                placeholders.memberInfo(member),
                "banned_date", StringUtil.formatDate(member.joinedTimestamp())
        );
    }
}