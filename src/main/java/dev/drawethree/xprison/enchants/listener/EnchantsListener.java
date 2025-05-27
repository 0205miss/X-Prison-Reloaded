package dev.drawethree.xprison.enchants.listener;

import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.gui.EnchantGUI;
import dev.drawethree.xprison.enchants.gui.PickaxeGUI;
import dev.drawethree.xprison.utils.Constants;
import dev.drawethree.xprison.utils.compat.MinecraftVersion;
import dev.drawethree.xprison.utils.inventory.InventoryUtils;
import me.lucko.helper.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.*;
import java.util.stream.Collectors;

public class EnchantsListener {

	private final XPrisonEnchants plugin;
	private final List<BlockBreakEvent> ignoredEvents = new ArrayList<>();
	private final Set<Integer> craftSlots = new HashSet<Integer>() {{
		add(0);
		add(10);
		add(13);
		add(16);
		add(18);
	}};
	public EnchantsListener(XPrisonEnchants plugin) {
		this.plugin = plugin;
	}

	public void register() {

		this.subscribeToPlayerDeathEvent();
		this.subscribeToPlayerRespawnEvent();
		this.subscribeToInventoryClickEvent();
		this.subscribeToPlayerJoinEvent();
		this.subscribeToPlayerDropItemEvent();
		this.subscribeToPlayerInteractEvent();
		this.subscribeToPlayerItemHeldEvent();
		this.subscribeToBlockBreakEvent();
	}

	public List<BlockBreakEvent> getIgnoredEvents() {
		return ignoredEvents;
	}

	private void subscribeToBlockBreakEvent() {
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(e -> !e.isCancelled() && !ignoredEvents.contains(e))
				.filter(e -> e.getPlayer().getItemInHand() != null && this.plugin.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getItemMeta()))
				.filter(e -> this.plugin.getCore().getMines().getApi().getMineAtLocation(e.getBlock().getLocation()) != null)
				.handler(e -> this.plugin.getEnchantsManager().handleBlockBreak(e, e.getPlayer().getItemInHand())).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerItemHeldEvent() {
		// Switching pickaxes
		Events.subscribe(PlayerItemHeldEvent.class, EventPriority.HIGHEST)
				.handler(e -> {

					ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
					ItemStack previousItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());

					// Old item
					if (previousItem != null && this.plugin.getCore().isPickaxeSupported(previousItem)) {
						this.plugin.getEnchantsManager().handlePickaxeUnequip(e.getPlayer(), previousItem);
					}

					// New item
					if (newItem != null && this.plugin.getCore().isPickaxeSupported(newItem)) {
						this.plugin.getEnchantsManager().handlePickaxeEquip(e.getPlayer(), newItem);
					}

				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerInteractEvent() {
		Events.subscribe(PlayerInteractEvent.class)
				.filter(e -> e.getItem() != null && this.plugin.getCore().isPickaxeSupported(e.getItem()))
				.filter(e -> (this.plugin.getEnchantsConfig().getOpenEnchantMenuActions().contains(e.getAction())))
				.handler(e -> {

					e.setCancelled(true);

					ItemStack pickAxe = e.getItem();
					int pickaxeSlot = InventoryUtils.getInventorySlot(e.getPlayer(), pickAxe);
					this.plugin.getCore().debug("Pickaxe slot is: " + pickaxeSlot, this.plugin);

					new PickaxeGUI(this.plugin, e.getPlayer(), pickAxe, pickaxeSlot).open();
				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerDropItemEvent() {
		// Dropping pickaxe
		Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST)
				.handler(e -> {
					if (this.plugin.getCore().isPickaxeSupported(e.getItemDrop().getItemStack())) {
						this.plugin.getEnchantsManager().handlePickaxeUnequip(e.getPlayer(), e.getItemDrop().getItemStack());
						Player p = e.getPlayer();
						ItemStack item = e.getItemDrop().getItemStack().clone();
						e.getItemDrop().remove();
						p.getInventory().addItem(item);
					}
				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerJoinEvent() {
		//First join pickaxe
		Events.subscribe(PlayerJoinEvent.class)
				.filter(e -> !e.getPlayer().hasPlayedBefore() && this.plugin.getEnchantsConfig().isFirstJoinPickaxeEnabled())
				.handler(e -> {
					ItemStack firstJoinPickaxe = this.plugin.getEnchantsManager().createFirstJoinPickaxe(e.getPlayer());
					e.getPlayer().getInventory().addItem(firstJoinPickaxe);
				}).bindWith(this.plugin.getCore());
	}

	private void subscribeToPlayerRespawnEvent() {
		Events.subscribe(PlayerRespawnEvent.class, EventPriority.LOWEST)
				.handler(e -> this.plugin.getRespawnManager().handleRespawn(e.getPlayer())).bindWith(this.plugin.getCore());
	}

	private void subscribeToInventoryClickEvent() {
		//Grindstone disenchanting - disable
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_14)) {
			Events.subscribe(InventoryClickEvent.class)
					.filter(e -> e.getInventory() instanceof GrindstoneInventory)
					.handler(e -> {
						ItemStack item1 = e.getInventory().getItem(0);
						ItemStack item2 = e.getInventory().getItem(1);
						if (e.getSlot() == 2 && (this.plugin.getEnchantsManager().hasEnchants(item1) || this.plugin.getEnchantsManager().hasEnchants(item2))) {
							e.setCancelled(true);
						}
					}).bindWith(this.plugin.getCore());
		}

		Events.subscribe(InventoryClickEvent.class, EventPriority.MONITOR)
				.filter(e -> e.getWhoClicked() instanceof Player)
				.filter(e -> !e.isCancelled())
				.handler(e -> {
					if(e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						ItemStack item = e.getCurrentItem();
                        assert item != null;
                        if (this.plugin.getCore().isPickaxeSupported(item)) {
							this.plugin.getEnchantsManager().handlePickaxeUnequip((Player) e.getWhoClicked(), item);
							e.setCancelled(true);
						}
					}else if(e.getView().getTopInventory().getType() == InventoryType.CHEST && e.getView().getBottomInventory().getType() == InventoryType.PLAYER) {
						ItemStack item = e.getCurrentItem();
						assert item != null;
						if (this.plugin.getCore().isPickaxeSupported(item)) {
							e.setCancelled(true);
						}
					}
				}).bindWith(this.plugin.getCore());

	}

	private void subscribeToPlayerDeathEvent() {
		Events.subscribe(PlayerDeathEvent.class, EventPriority.LOWEST)
				.handler(e -> {

					if (!this.plugin.getEnchantsConfig().isKeepPickaxesOnDeath()) {
						return;
					}

					List<ItemStack> pickaxes = e.getDrops().stream().filter(itemStack -> this.plugin.getCore().isPickaxeSupported(itemStack) &&
							this.plugin.getEnchantsManager().hasEnchants(itemStack)).collect(Collectors.toList());
					e.getDrops().removeAll(pickaxes);

					this.plugin.getRespawnManager().addRespawnItems(e.getEntity(), pickaxes);

					if (pickaxes.size() > 0) {
						this.plugin.getCore().debug("Removed " + e.getEntity().getName() + "'s pickaxes from drops (" + pickaxes.size() + "). Will be given back on respawn.", this.plugin);
					} else {
						this.plugin.getCore().debug("No Pickaxes found for player " + e.getEntity().getName() + " (PlayerDeathEvent)", this.plugin);
					}

				}).bindWith(this.plugin.getCore());
	}

	private Optional<IWrappedFlag<WrappedState>> getWGFlag() {
		return this.plugin.getCore().getWorldGuardWrapper().getFlag(Constants.ENCHANTS_WG_FLAG_NAME, WrappedState.class);
	}
}
