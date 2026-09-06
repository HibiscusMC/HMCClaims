package com.hibiscusmc.hmcclaims.form.impl;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.form.ClaimListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.form.BaseForm;
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class ClaimListForm implements BaseForm {

    /**
     * Key the per-player search and filter state is stored under.
     */
    private final static String STATE_KEY = "claim-list";

    @Inject
    private ConfigHolder<ClaimListFormConfig> configHolder;
    @Inject
    private ConfigHolder<Messages> messagesHolder;
    @Inject
    private ConfigHolder<Settings> settingsHolder;

    @Inject
    private StorageHolder storageHolder;

    @Inject
    private FormRegistry forms;
    @Inject
    private FormService formService;
    @Inject
    private FormInputs inputs;

    @Inject
    private ClaimManager claimManager;

    @Inject
    private PlaceholderUtil placeholders;
    @Inject
    private SchedulerUtil scheduler;
    @Inject
    private TextUtil text;

    private ClaimListFormConfig config;

    @Override
    public void loadConfig() {
        ClaimListFormConfig loaded = configHolder.get();
        if (loaded == null) {
            throw new NullPointerException("Config is not initialized yet!");
        }

        config = loaded;
    }

    @Override
    public void send(@NotNull Player player) {
        if (settingsHolder.get().guis().claimsGui() == Settings.Guis.ClaimsGui.FIRST_CLAIM) {
            Set<Claim> claims = claimManager.getPlayerClaims(player.getUniqueId());

            if (claims.isEmpty()) {
                text.send(player, messagesHolder.get().claims().dontHaveAny());
                return;
            }

            forms.get(ClaimMemberListForm.class)
                    .send(player, new GuiMetadata(claims.iterator().next()));
            return;
        }

        render(player);
    }

    private void render(@NotNull Player player) {
        scheduler.scheduleAsync(() -> {
            State state = formService.state(player, STATE_KEY, State::new);

            List<Claim> all = claimManager.getPlayerClaims(player.getUniqueId()).stream()
                    .sorted(Comparator.comparing((Claim claim) -> claim.main() != null)
                            .thenComparing(Claim::name, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            List<Claim> matching = all.stream()
                    .filter(claim -> matches(claim, state))
                    .toList();

            int limit = state.showAll ? -1 : config.maxEntries();
            boolean truncated = limit >= 0 && matching.size() > limit;
            List<Claim> shown = truncated ? matching.subList(0, limit) : matching;

            scheduler.schedule(() -> {
                SharedContext context = new SharedContext(
                        player, null, ClaimListForm.class, formService, forms, () -> render(player)
                ).extraButtons(config.extraButtons())
                        .backButton(config.backButton());

                SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                        .title(FormText.line(config.title(), player))
                        .content(FormText.block(
                                matching.isEmpty() ? config.emptyContent() : config.content(), player, Map.of(
                                        "filter", filterName(state),
                                        "query", state.query.isEmpty() ? config.noQuery() : state.query,
                                        "shown", String.valueOf(shown.size()),
                                        "total", String.valueOf(all.size())
                                )
                        ));

                for (String section : config.order()) {
                    switch (section) {
                        case "search" -> builder.button(
                                FormText.line(config.searchButton().text(), player),
                                context.image(config.searchButton().image(), null),
                                () -> openSearch(player, state)
                        );

                        case "claims" -> {
                            shown.forEach(claim -> addClaim(builder, context, player, claim));

                            if (truncated) {
                                builder.button(
                                        FormText.line(config.moreButton().text(), player, Map.of(
                                                "remaining", String.valueOf(matching.size() - shown.size())
                                        )),
                                        context.image(config.moreButton().image(), null),
                                        () -> {
                                            state.showAll = true;
                                            render(player);
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

    private void addClaim(
            @NotNull SimpleFormSpec.Builder builder, @NotNull SharedContext context,
            @NotNull Player player, @NotNull Claim claim
    ) {
        FormTemplate.Entry entry = claim.main() == null ? config.claimEntry() : config.subClaimEntry();

        builder.button(navigating(
                FormText.block(entry.text(), player, placeholders.claimInfo(claim)),
                context.image(entry.image(), null),
                context,
                () -> openActions(player, claim)
        ));
    }

    private void openActions(@NotNull Player player, @NotNull Claim claim) {
        FormTemplate.SubForm actions = config.claimActions();
        Map<String, String> data = placeholders.claimInfo(claim);

        SimpleFormSpec.Builder builder = SimpleFormSpec.builder()
                .title(FormText.line(actions.title().text(), player, Map.of(
                        "name", FormText.shorten(claim.name(), actions.title().maxLength())
                )))
                .content(FormText.block(actions.content(), player, data));

        for (Map.Entry<String, FormTemplate.Button> entry : ordered(actions.buttons(), "manage", "rename").entrySet()) {
            FormTemplate.Button button = entry.getValue();
            String label = FormText.line(button.text(), player, data);

            switch (entry.getKey()) {
                case "manage" -> builder.button(label, image(button.image(), formService), () -> {
                    formService.push(player, () -> openActions(player, claim));

                    forms.get(ClaimMemberListForm.class)
                            .send(player, new GuiMetadata(claim));
                });

                case "rename" -> builder.button(label, image(button.image(), formService), () -> rename(player, claim));

                case "back" ->
                        builder.button(label, image(button.image(), formService), () -> formService.back(player));

                default -> builder.button(label, image(button.image(), formService), () -> openActions(player, claim));
            }
        }

        formService.send(player, builder.build());
    }

    private void rename(@NotNull Player player, @NotNull Claim claim) {
        inputs.prompt(
                player, inputs.config().renameClaim(), Map.of("claim_name", claim.name()), claim.name(),
                newName -> {
                    claim.rename(newName);
                    storageHolder.get().claims().saveClaimName(claim);

                    openActions(player, claim);
                },
                () -> openActions(player, claim)
        );
    }

    private void openSearch(@NotNull Player player, @NotNull State state) {
        ClaimListFormConfig.Search search = config.search();

        List<String> filterKeys = new ArrayList<>(search.filter().options().keySet());
        List<String> searchKeys = new ArrayList<>(search.searchBy().options().keySet());

        CustomFormSpec spec = CustomFormSpec.builder()
                .title(FormText.line(search.title(), player))
                .input(
                        "query",
                        FormText.line(search.query().label(), player),
                        FormText.line(search.query().placeholder(), player),
                        state.query
                )
                .dropdown(
                        "search-by",
                        FormText.line(search.searchBy().label(), player),
                        labels(search.searchBy().options(), searchKeys, player),
                        Math.max(0, searchKeys.indexOf(state.searchBy))
                )
                .dropdown(
                        "filter",
                        FormText.line(search.filter().label(), player),
                        labels(search.filter().options(), filterKeys, player),
                        Math.max(0, filterKeys.indexOf(state.filter))
                )
                .onSubmit(values -> {
                    String query = values.input("query");
                    state.query = query == null ? "" : query.trim();

                    int searchBy = values.index("search-by", 0);
                    if (searchBy >= 0 && searchBy < searchKeys.size()) {
                        state.searchBy = searchKeys.get(searchBy);
                    }

                    int filter = values.index("filter", 0);
                    if (filter >= 0 && filter < filterKeys.size()) {
                        state.filter = filterKeys.get(filter);
                    }

                    render(player);
                })
                .onClose(() -> render(player))
                .build();

        formService.send(player, spec);
    }

    private boolean matches(@NotNull Claim claim, @NotNull State state) {
        switch (state.filter) {
            case "MAIN" -> {
                if (claim.main() != null) {
                    return false;
                }
            }

            case "SUB_CLAIMS" -> {
                if (claim.main() == null) {
                    return false;
                }
            }

            default -> {
            }
        }

        if (state.query.isEmpty()) {
            return true;
        }

        return switch (state.searchBy) {
            case "id" -> StringUtil.has(claim.claimId().toString(), state.query);

            case "main" -> claim.main() != null && StringUtil.has(claim.main().name(), state.query);

            case "member" -> StringUtil.listHas(
                    claim.members().stream().map(ClaimMember::lastKnownName).toList(), state.query
            );

            default -> StringUtil.has(claim.name(), state.query);
        };
    }

    @NotNull
    private String filterName(@NotNull State state) {
        return config.search().filter().options().getOrDefault(state.filter, state.filter);
    }

    @NotNull
    private List<String> labels(@NotNull Map<String, String> options, @NotNull List<String> keys, @NotNull Player player) {
        return keys.stream()
                .map(key -> FormText.line(options.get(key), player))
                .toList();
    }

    private static final class State {

        private String query = "";
        private String searchBy = "name";
        private String filter = "ALL";

        private boolean showAll;
    }
}