package com.hibiscusmc.hmcclaims.claim;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.form.DialogsFormConfig;
import com.hibiscusmc.hmcclaims.dialog.type.ConfirmationDialog;
import com.hibiscusmc.hmcclaims.form.FormService;
import com.hibiscusmc.hmcclaims.form.FormText;
import com.hibiscusmc.hmcclaims.form.spec.ModalFormSpec;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.marker.MarkType;
import com.hibiscusmc.hmcclaims.selection.Selection;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigHolder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Expands claims by a number of blocks towards a direction, after the owner confirms it.
 */
@Singleton
public class ClaimExpander {

    @Inject
    private ClaimManager claimManager;

    @Inject
    private UserManager userManager;

    @Inject
    private SelectionManager selectionManager;

    @Inject
    private BlockMarker marker;

    @Inject
    private FormService formService;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private ConfigHolder<DialogsFormConfig> dialogsFormHolder;

    @Inject
    private TextUtil text;

    private record Problem(String message, Map<String, String> placeholders) {
    }

    private record Plan(ClaimRegion region, long price, Problem problem) {
    }

    /**
     * Validates the expansion and asks the player to confirm it.
     *
     * @param player The owner of the claim.
     * @param claim  The claim to expand.
     * @param face   The cardinal direction to expand towards.
     * @param amount The number of blocks to add.
     */
    public void prompt(@NotNull Player player, @NotNull Claim claim, @NotNull BlockFace face, int amount) {
        Messages messages = messagesHolder.get();
        DialogsFormConfig forms = dialogsFormHolder.get();

        Plan plan = plan(player, claim, face, amount);
        if (plan.problem() != null) {
            text.send(player, plan.problem().message(), plan.problem().placeholders());
            return;
        }

        ClaimRegion region = plan.region();
        User user = userManager.getUser(player.getUniqueId()).orElse(null);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("claim_name", claim.name());
        placeholders.put("amount", amount + "");
        placeholders.put("direction", direction(face));
        placeholders.put("width", (region.maxX() - region.minX() + 1) + "");
        placeholders.put("length", (region.maxZ() - region.minZ() + 1) + "");
        placeholders.put("price", plan.price() + "");
        placeholders.put("remaining_blocks", (userManager.getRemainingBlocks(user) - plan.price()) + "");

        Runnable cancelled = () -> text.send(player, messages.claims().expanding().cancelled());

        if (formService.isBedrock(player)) {
            DialogsFormConfig.Confirmation config = forms.expandClaim();

            formService.send(player, new ModalFormSpec(
                    FormText.line(config.title(), player, placeholders),
                    FormText.block(config.content(), player, placeholders),
                    FormText.line(config.confirm(), player),
                    FormText.line(config.cancel(), player),
                    () -> confirm(player, claim, face, amount),
                    cancelled,
                    null
            ));

            return;
        }

        Messages.Dialogs.ExpandClaim dialog = messages.dialogs().expandClaim();

        new ConfirmationDialog()
                .create(dialog.title(), dialog.buttons(), placeholders, dialog.body())
                .onSubmit(() -> confirm(player, claim, face, amount))
                .onCancel(cancelled)
                .show(player);
    }

    private void confirm(Player player, Claim claim, BlockFace face, int amount) {
        Messages.Claims.Expanding messages = messagesHolder.get().claims().expanding();

        boolean registered = claimManager.getClaim(claim.claimId()).orElse(null) == claim;
        if (!registered || !claim.owner().equals(player.getUniqueId())) {
            text.send(player, messages.unavailable());
            return;
        }

        Plan plan = plan(player, claim, face, amount);
        if (plan.problem() != null) {
            text.send(player, plan.problem().message(), plan.problem().placeholders());
            return;
        }

        if (!claimManager.resizeClaim(player, claim, plan.region())) {
            return;
        }

        Selection resizing = selectionManager.currentResize(player);
        if (resizing != null && resizing.resizingClaim() == claim) {
            selectionManager.destroySelection(player);
        }

        userManager.calculateUsedBlocks(player.getUniqueId());

        boolean main = claim.main() == null;

        if (main) {
            text.send(player, messages.expanded(), Map.of(
                    "name", claim.name(),
                    "amount", amount + "",
                    "direction", direction(face),
                    "price", plan.price() + ""
            ));
        } else {
            text.send(player, messages.subExpanded(), Map.of(
                    "name", claim.name(),
                    "amount", amount + "",
                    "direction", direction(face)
            ));
        }

        marker.mark(player, claim.region().getLCornerBlocks(), main ? MarkType.CREATE : MarkType.CREATE_SUB, TimeUnit.SECONDS.toMillis(10));
    }

    private Plan plan(Player player, Claim claim, BlockFace face, int amount) {
        Messages.Claims messages = messagesHolder.get().claims();

        ClaimRegion region = expandedRegion(claim, face, amount);
        if (region == null) {
            return failed(messages.expanding().outsideWorldBorder());
        }

        boolean sub = claim.main() != null;

        if (sub && !isInside(region, claim.main().region())) {
            return failed(messages.resizing().withinMainClaim());
        }

        if (claimManager.isOverlapping(region, player.getUniqueId(), sub, claim)) {
            return failed(messages.selecting().claimOverlaps());
        }

        if (sub) {
            return new Plan(region, 0, null);
        }

        User user = userManager.getUser(player.getUniqueId()).orElse(null);
        if (user == null) {
            return failed(messages.expanding().unavailable());
        }

        long price = region.getSurfaceArea() - claim.region().getSurfaceArea();
        long current = userManager.getRemainingBlocks(user);

        if (price > current) {
            return new Plan(region, price, new Problem(messages.selecting().notEnoughClaimBlocks(), Map.of(
                    "required_blocks", price + "",
                    "current_blocks", current + ""
            )));
        }

        return new Plan(region, price, null);
    }

    @Nullable
    private ClaimRegion expandedRegion(Claim claim, BlockFace face, int amount) {
        ClaimRegion current = claim.region();

        World world = current.bukkitWorld();
        if (world == null) {
            return null;
        }

        long minX = current.minX();
        long maxX = current.maxX();
        long minZ = current.minZ();
        long maxZ = current.maxZ();

        switch (face) {
            case NORTH -> minZ -= amount;
            case SOUTH -> maxZ += amount;
            case EAST -> maxX += amount;
            case WEST -> minX -= amount;
            default -> {
                return null;
            }
        }

        if (!fitsInt(minX) || !fitsInt(maxX) || !fitsInt(minZ) || !fitsInt(maxZ)) {
            return null;
        }

        if (!world.getWorldBorder().isInside(new Location(world, minX + 0.5, 0, minZ + 0.5))
                || !world.getWorldBorder().isInside(new Location(world, maxX + 0.5, 0, maxZ + 0.5))) {
            return null;
        }

        ClaimRegion copy = new ClaimRegion(current.worldName(), current.minX(), current.maxX(), current.minZ(), current.maxZ());

        return copy.resize(face, amount) ? copy : null;
    }

    private boolean isInside(ClaimRegion inner, ClaimRegion outer) {
        return inner.minX() >= outer.minX() && inner.maxX() <= outer.maxX()
                && inner.minZ() >= outer.minZ() && inner.maxZ() <= outer.maxZ();
    }

    private boolean fitsInt(long value) {
        return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
    }

    private Plan failed(String message) {
        return new Plan(null, 0, new Problem(message, Map.of()));
    }

    private String direction(BlockFace face) {
        String name = face.name().toLowerCase(Locale.ROOT);

        return messagesHolder.get().claims().expanding().directions().getOrDefault(name, name);
    }
}
