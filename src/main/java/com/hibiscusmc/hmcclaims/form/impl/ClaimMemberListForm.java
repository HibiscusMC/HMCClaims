package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
import com.hibiscusmc.hmcclaims.form.FormImages;
import com.hibiscusmc.hmcclaims.form.FormRegistry;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.SharedContext;
import com.hibiscusmc.hmcclaims.form.input.FormInputs;
import com.hibiscusmc.hmcclaims.form.spec.CustomFormSpec;
import com.hibiscusmc.hmcclaims.form.spec.SimpleFormSpec;
import com.hibiscusmc.hmcclaims.gui.GuiMetadata;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.StringUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Singleton
public class ClaimMemberListForm implements BaseForm {

    private final static String STATE_KEY = "claim-members";

    @Inject
    private ConfigHolder<ClaimMemberListFormConfig> configHolder;
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

    private ClaimMemberListFormConfig config;

    @Override
    public void loadConfig() {
        ClaimMemberListFormConfig loaded = configHolder.get();
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

            ClaimMember self = claim.getMember(player.getUniqueId()).orElse(null);

            List<ClaimMember> all = claim.members().stream()
                    .sorted(Comparator.<ClaimMember, Boolean>comparing(
                                    member -> member.uuid().equals(claim.owner()), Comparator.reverseOrder()
                            )
                            .thenComparingLong(member -> member.joinedTimestamp().getEpochSecond()))
                    .toList();

            List<ClaimMember> matching = all.stream()
                    .filter(member -> matches(member, state))
                    .toList();

            int limit = state.showAll ? -1 : config.maxEntries();
            boolean truncated = limit >= 0 && matching.size() > limit;
            List<ClaimMember> shown = truncated ? matching.subList(0, limit) : matching;

            scheduler.schedule(() -> {
                SharedContext context = new SharedContext(
                        player, metadata, ClaimMemberListForm.class, formService, forms, () -> render(player, metadata)
                ).navigation(config.nav())
                        .extraButtons(config.extraButtons())
                        .backButton(config.backButton());

                SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                        .title(FormText.line(config.title().text(), player, Map.of(
                                "claim_name", FormText.shorten(claim.name(), config.title().maxLength())
                        )))
                        .content(FormText.block(
                                matching.isEmpty() ? config.emptyContent() : config.content(), player, Map.of(
                                        "filter", filterName(claim, state),
                                        "query", state.query.isEmpty() ? config.noQuery() : state.query,
                                        "shown", String.valueOf(shown.size()),
                                        "total", String.valueOf(all.size())
                                )
                        ));

                for (String section : config.order()) {
                    switch (section) {
                        case "add-member" -> builder.button(
                                FormText.line(config.addMemberButton().text(), player),
                                context.image(config.addMemberButton().image(), null),
                                () -> addMember(player, metadata)
                        );

                        case "search" -> builder.button(
                                FormText.line(config.searchButton().text(), player),
                                context.image(config.searchButton().image(), null),
                                () -> openSearch(player, metadata, state)
                        );

                        case "members" -> {
                            shown.forEach(member -> addMemberEntry(builder, context, player, self, member, metadata));

                            if (truncated) {
                                builder.button(
                                        FormText.line(config.moreButton().text(), player, Map.of(
                                                "remaining", String.valueOf(matching.size() - shown.size())
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

    private void addMemberEntry(
            @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context, @NotNull Player player,
            @Nullable ClaimMember self, @NotNull ClaimMember member, @NotNull GuiMetadata metadata
    ) {
        boolean manageable = self != null && self.canManage(member);
        FormTemplate.Entry entry = manageable ? config.memberEntry() : config.unmanageableMemberEntry();

        builder.button(navigating(
                FormText.block(entry.text(), player, placeholders.memberInfo(member)),
                FormImages.resolvePlayer(
                        entry.image(), member.lastKnownName(), member.uuid().toString(),
                        Material.PLAYER_HEAD, formService.materialImageFallback()
                ),
                context,
                () -> openActions(player, metadata, member)
        ));
    }

    private void openActions(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimMember member) {
        Claim claim = metadata.claim();
        FormTemplate.SubForm actions = config.memberActions();

        ClaimMember self = claim.getMember(player.getUniqueId()).orElse(null);
        boolean canManage = self != null && self.canManage(member);

        boolean canKick = canManage && self.hasPermission(Permission.MANAGE_MEMBERS);
        boolean canBan = canManage && self.hasPermission(Permission.BAN_MEMBERS);
        boolean canRole = canManage && self.hasPermission(Permission.MANAGE_MEMBER_ROLES);
        boolean canPermissions = canManage && self.hasPermission(Permission.MANAGE_MEMBER_PERMISSIONS);

        Map<String, String> data = placeholders.memberInfo(member);
        GuiMetadata scoped = metadata.member(member);

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(actions.title().text(), player, Map.of(
                        "name", FormText.shorten(member.lastKnownName(), actions.title().maxLength())
                )))
                .content(FormText.block(actions.content(), player, data));

        Runnable reopen = () -> openActions(player, metadata, member);

        for (Map.Entry<String, FormTemplate.Button> entry
                : ordered(actions.buttons(), "role", "permissions", "kick", "ban").entrySet()) {
            FormTemplate.Button button = entry.getValue();
            String label = FormText.line(button.text(), player, data);

            switch (entry.getKey()) {
                case "role" -> {
                    if (canRole) {
                        builder.button(label, image(button.image(), formService), () -> {
                            formService.push(player, reopen);

                            forms.get(ClaimMemberRoleForm.class).send(player, scoped);
                        });
                    }
                }

                case "permissions" -> {
                    if (canPermissions) {
                        builder.button(label, image(button.image(), formService), () -> {
                            formService.push(player, reopen);

                            forms.get(ClaimMemberPermissionsForm.class).send(player, scoped);
                        });
                    }
                }

                case "kick" -> {
                    if (canKick) {
                        builder.button(label, image(button.image(), formService), () -> kick(player, metadata, member));
                    }
                }

                case "ban" -> {
                    if (canBan) {
                        builder.button(label, image(button.image(), formService), () -> ban(player, metadata, member));
                    }
                }

                case "back" ->
                        builder.button(label, image(button.image(), formService), () -> formService.back(player));

                default -> builder.button(label, image(button.image(), formService), reopen);
            }
        }

        formService.send(player, builder.build());
    }

    private void kick(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimMember member) {
        Claim claim = metadata.claim();

        claim.removeMember(member.uuid());
        storageHolder.get().claims().saveMembers(claim);

        text.send(player, messagesHolder.get().claims().memberRemoved(), Map.of(
                "name", member.lastKnownName(),
                "player_head", "",
                "claim", claim.name()
        ));

        formService.back(player);
    }

    private void ban(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull ClaimMember member) {
        Claim claim = metadata.claim();

        member.role(claim.roleRegistry().defaultRole());
        member.permissions(new Object2BooleanArrayMap<>());
        member.joinedTimestamp(Instant.now());
        member.banned(true);

        storageHolder.get().claims().saveMembers(claim);

        text.send(player, messagesHolder.get().claims().memberBanned(), Map.of(
                "name", member.lastKnownName(),
                "player_head", "",
                "claim", claim.name()
        ));

        formService.back(player);
    }

    private void addMember(@NotNull Player player, @NotNull GuiMetadata metadata) {
        Claim claim = metadata.claim();
        Runnable reopen = () -> render(player, metadata);

        inputs.promptPlayer(
                player, inputs.config().addMember(), Map.of("claim_name", claim.name()),
                target -> {
                    ClaimMember added = claim.addMember(target);

                    if (added == null) {
                        text.send(player, messagesHolder.get().claims().memberAlreadyAdded());
                        reopen.run();
                        return;
                    }

                    storageHolder.get().claims().saveMembers(claim);

                    text.send(player, messagesHolder.get().claims().memberAdded(), Map.of(
                            "name", target.name(),
                            "player_head", "",
                            "claim", claim.name()
                    ));

                    reopen.run();
                },
                reopen
        );
    }

    private void openSearch(@NotNull Player player, @NotNull GuiMetadata metadata, @NotNull State state) {
        ClaimMemberListFormConfig.Search search = config.search();
        Claim claim = metadata.claim();

        List<String> keys = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        keys.add("ALL");
        labels.add(FormText.line(search.filter().options().getOrDefault("ALL", "All roles"), player));

        String rolePattern = search.filter().options().getOrDefault("ROLE", "<role>");
        for (ClaimRole role : filterableRoles(claim)) {
            keys.add(role.id().toString());
            labels.add(FormText.line(rolePattern.replace("<role>", role.name()), player));
        }

        CustomFormSpec spec = CustomFormSpec.builder()
                .title(FormText.line(search.title(), player))
                .input(
                        "query",
                        FormText.line(search.query().label(), player),
                        FormText.line(search.query().placeholder(), player),
                        state.query
                )
                .dropdown(
                        "filter",
                        FormText.line(search.filter().label(), player),
                        labels,
                        Math.max(0, keys.indexOf(state.filter))
                )
                .onSubmit(values -> {
                    String query = values.input("query");
                    state.query = query == null ? "" : query.trim();

                    int filter = values.index("filter", 0);
                    state.filter = filter >= 0 && filter < keys.size() ? keys.get(filter) : "ALL";

                    render(player, metadata);
                })
                .onClose(() -> render(player, metadata))
                .build();

        formService.send(player, spec);
    }

    @NotNull
    private List<ClaimRole> filterableRoles(@NotNull Claim claim) {
        return claim.roleRegistry().allRoles().stream()
                .filter(role -> !role.id().equals(claim.roleRegistry().everyoneRole().id()))
                .toList();
    }

    private boolean matches(@NotNull ClaimMember member, @NotNull State state) {
        if (!state.query.isEmpty() && !StringUtil.has(member.lastKnownName(), state.query)) {
            return false;
        }

        return state.filter.equals("ALL") || member.role().id().toString().equals(state.filter);
    }

    @NotNull
    private String filterName(@NotNull Claim claim, @NotNull State state) {
        Map<String, String> options = config.search().filter().options();

        if (state.filter.equals("ALL")) {
            return options.getOrDefault("ALL", "All roles");
        }

        return filterableRoles(claim).stream()
                .filter(role -> role.id().toString().equals(state.filter))
                .findFirst()
                .map(role -> options.getOrDefault("ROLE", "<role>").replace("<role>", role.name()))
                .orElse(options.getOrDefault("ALL", "All roles"));
    }

    private static final class State {

        private String query = "";
        private String filter = "ALL";

        private boolean showAll;
    }
}