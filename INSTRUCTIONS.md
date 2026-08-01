1. Create the permission in the `com.hibiscusmc.hmcclaims.claim.permission.Permission` class. Every permission should have a `Key`, a `Display Name` and a `Description`. These are default values that will be used only for the default config files, so if the description of a permission is too long, consider using a `\n` to insert a new line.
2. Register the permission in the `com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry` class. Every permission should be registered inside of the `static { }` block. This registry only can be accessed statically, you should never try to create an instance of this class.
3. Creating the listener if it doesn't exists yet. Every listener should implement the  `org.bukkit.event.Listener` class and should be inside of the `com.hibiscusmc.hmcclaims.listener.permission` package. After creating the class, you should register the listener on the `com.hibiscusmc.hmcclaims.module.ListenerModule` class in order to inject the dependencies properly. Just remove the last `;`, and add a new `to(YourClassListener.class)`;
4. Injecting the needed dependencies. Usually you would just need 3, `ConfigHolder<Messages>` to import messages from config files, `ClaimManager` to fetch claims and TextUtil to send formatted messages to players. In order to do so, you should use the `@Inject` annotation from `team.unnamed.inject.Inject`. Here's an example:
```
@Inject
private ConfigHolder<Messages> messagesHolder;

@Inject
private ClaimManager claimManager;

@Inject
private TextUtil text;
```

5. Using the permission. Most of the time you can just check if a player has a permission using the direct method from the Claim class like this:
`claim.hasPermission(Player#getUniqueId, Permission.PERMISSION_TO_CHECK)`.

6. Example of putting everything together:
```
@EventHandler(priority = EventPriority.LOWEST)
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    Block block = event.getBlock();

    Claim claim = claimManager.getClaimAt(block.getLocation())
            .orElse(null);

    if (claim == null) {
        return;
    }

    if (claim.hasPermission(player.getUniqueId(), Permission.BREAK_BLOCK)) {
        return;
    }

    text.send(player, messagesHolder.get().claims().permissions().breakBlock());
    event.setCancelled(true);
}
```