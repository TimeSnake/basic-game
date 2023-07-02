/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.game.util.user;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.timesnake.basic.bukkit.core.main.BasicBukkit;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.EntityDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserAttemptPickupItemEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageEvent;
import de.timesnake.basic.bukkit.util.user.event.UserMoveEvent;
import de.timesnake.basic.bukkit.util.user.inventory.*;
import de.timesnake.basic.bukkit.util.user.scoreboard.ItemHoldClick;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.extension.util.player.UserMap;
import de.timesnake.library.packets.core.packet.out.entity.ClientboundSetEntityDataPacketBuilder;
import de.timesnake.library.packets.util.listener.PacketHandler;
import de.timesnake.library.packets.util.listener.PacketPlayOutListener;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class SpectatorManager implements UserInventoryClickListener,
    UserInventoryInteractListener, PacketPlayOutListener, Listener {

  // teleports the spectator to spawn if he goes lower than min height - value
  public static final Integer MAX_LOWER_THAN_MIN_HEIGHT = 10;

  public static final ExItemStack USER_INV = new ExItemStack(1, Material.PLAYER_HEAD,
      "§9Teleporter").setMoveable(false).setDropable(false).immutable();
  public static final ExItemStack GLOWING = new ExItemStack(2, Material.SPECTRAL_ARROW,
      "§6Glowing").setMoveable(false).setDropable(false).immutable();
  public static final ExItemStack SPEED = new ExItemStack(3, Material.FEATHER, "§bSpeed")
      .setMoveable(false).setDropable(false).immutable();
  public static final ExItemStack FLYING = new ExItemStack(4, Material.RABBIT_FOOT, "§9Flying")
      .setMoveable(false).setDropable(false).immutable();
  public static final ExItemStack LEAVE_ITEM = new ExItemStack(8, Material.ANVIL,
      "§6Leave (hold right)").setMoveable(false).setDropable(false).immutable();

  private static final Integer LEAVE_TIME = 2000;

  private final UserMap<User, ItemHoldClick> clickedLeaveUsers = new UserMap<>();
  private final HashMap<Integer, User> userHeadsById = new HashMap<>();
  private final Set<User> clickCooldownUsers = new HashSet<>();

  private ExInventory gameUserInv;
  private Set<User> glowingUsers = new HashSet<>();

  public SpectatorManager() {
    this.gameUserInv = new ExInventory(9, Component.text("Players"));

    Server.getInventoryEventManager()
        .addInteractListener(this, USER_INV, GLOWING, SPEED, FLYING, LEAVE_ITEM);
    Server.getPacketManager().addListener(this);
    Server.registerListener(this, BasicBukkit.getPlugin());
  }

  public void updateSpectatorTools() {
    this.updateTeleportInventory();
    this.updateGlowingPlayers();
  }

  public void clearTools() {
    this.glowingUsers.clear();
  }

  private void updateTeleportInventory() {
    Server.getInventoryEventManager().removeClickListener(this);
    int inGame = Server.getInGameUsers().size();
    this.gameUserInv = new ExInventory(inGame == 0 ? 9 : (inGame + 8) / 9 * 9,
        Component.text("Players"));
    this.userHeadsById.clear();
    int slot = 0;
    for (User user : Server.getInGameUsers()) {
      ExItemStack head = ExItemStack.getHead(user.getPlayer(), user.getChatName())
          .setLore("", "§7Click to " +
              "teleport");
      this.userHeadsById.put(head.getId(), user);
      this.gameUserInv.setItemStack(slot, head);
      Server.getInventoryEventManager().addClickListener(this, head);
      slot++;
    }
  }

  public ExInventory getGameUserInventory() {
    return gameUserInv;
  }

  public void sendGlowUpdate() {
    this.sendUpdatePackets();
  }

  public void updateGlowingPlayers() {
    this.glowingUsers = new HashSet<>(Server.getInGameUsers());
    this.sendUpdatePackets();
  }

  private void sendUpdatePackets() {
    Collection<User> receivers = Server.getSpectatorUsers();
    receivers.addAll(Server.getOutGameUsers());

    for (User glowingUser : this.glowingUsers) {
      Packet<?> packet = new ClientboundSetEntityDataPacketBuilder(glowingUser.getMinecraftPlayer()).build();
      for (User receiver : receivers) {
        receiver.sendPacket(packet);
      }
    }
  }

  @PacketHandler(type = ClientboundSetEntityDataPacket.class, modify = true)
  public Packet<?> onPacketPlayOut(Packet<?> packet, Player receiver) {
    if (!(packet instanceof ClientboundSetEntityDataPacket dataPacket)) {
      return packet;
    }

    int entityId = dataPacket.id();

    Player player = null;
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.getEntityId() == entityId) {
        player = p;
        break;
      }
    }

    if (player == null) {
      return packet;
    }

    if (!this.glowingUsers.contains(Server.getUser(player))) {
      return packet;
    }

    SpectatorUser user = (SpectatorUser) Server.getUser(receiver);

    if (!(user.hasGlowingEnabled())
        || !(user.getStatus().equals(Status.User.OUT_GAME) || user.getStatus()
        .equals(Status.User.SPECTATOR))) {
      return packet;
    }

    packet = new ClientboundSetEntityDataPacketBuilder(((CraftPlayer) player).getHandle())
        .update()
        .setFlags(ClientboundSetEntityDataPacketBuilder.getFlagsOfPacket(dataPacket))
        .setFlag(ClientboundSetEntityDataPacketBuilder.Type.GLOWING, true)
        .build();

    return packet;
  }

  @Override
  public void onUserInventoryInteract(UserInventoryInteractEvent e) {
    ExItemStack clickedItem = e.getClickedItem();
    SpectatorUser user = (SpectatorUser) e.getUser();

    if (this.clickCooldownUsers.contains(user)) {
      return;
    }

    this.clickCooldownUsers.add(user);
    Server.runTaskLaterSynchrony(() -> this.clickCooldownUsers.remove(user), 10,
        BasicBukkit.getPlugin());

    if (clickedItem.equals(USER_INV)) {
      user.openGameUserInventory();
    } else if (clickedItem.equals(LEAVE_ITEM)) {

      if (e.getAction().isRightClick()) {
        if (!this.clickedLeaveUsers.containsKey(user)) {
          this.clickedLeaveUsers.put(user, new ItemHoldClick(LEAVE_TIME));
        } else {
          if (this.clickedLeaveUsers.get(user).click()) {
            user.sendActionBarText(Component.empty());
            user.switchToLobbyLast();
          } else {
            user.sendActionBarText(Component.text("Leaving...", ExTextColor.WARNING));
          }
        }
      }
    } else if (clickedItem.equals(GLOWING)) {
      user.setGlowingEnabled(!user.hasGlowingEnabled());
      if (user.hasGlowingEnabled()) {
        user.sendPluginMessage(Plugin.GAME,
            Component.text("Enabled glowing", ExTextColor.PERSONAL));
        clickedItem.enchant();
      } else {
        user.sendPluginMessage(Plugin.GAME,
            Component.text("Disabled glowing", ExTextColor.PERSONAL));
        clickedItem.disenchant();
      }
    } else if (clickedItem.equals(SPEED)) {
      user.setSpeedEnabled(!user.hasSpeedEnabled());
      if (user.hasSpeedEnabled()) {
        user.sendPluginMessage(Plugin.GAME,
            Component.text("Enabled speed", ExTextColor.PERSONAL));
        clickedItem.enchant();
      } else {
        user.sendPluginMessage(Plugin.GAME,
            Component.text("Disabled speed", ExTextColor.PERSONAL));
        clickedItem.disenchant();
      }
    } else if (clickedItem.equals(FLYING)) {
      user.setFlyEnabled(!user.hasFlyEnabled());
      user.sendPluginMessage(Plugin.GAME,
          Component.text((user.getAllowFlight() ? "Enabled" : "Disabled") + " flying",
              ExTextColor.PERSONAL));
      if (user.getAllowFlight()) {
        clickedItem.enchant();
      } else {
        clickedItem.disenchant();
      }
    }

    user.setItem(clickedItem);
    user.updateInventory();
  }

  @Override
  public void onUserInventoryClick(UserInventoryClickEvent e) {
    User user = e.getUser();
    ExItemStack clickedItem = e.getClickedItem();
    if (!this.userHeadsById.containsKey(clickedItem.getId())) {
      e.setCancelled(true);
      return;
    }

    User clickedUser = this.userHeadsById.get(clickedItem.getId());
    user.teleport(clickedUser);
    user.asSender(Plugin.GAME)
        .sendPluginMessage(Component.text("Teleported to player ", ExTextColor.PERSONAL)
            .append(clickedUser.getChatNameComponent()));

    e.setCancelled(true);
  }

  @EventHandler
  public void onUserDamage(UserDamageEvent e) {
    User user = e.getUser();
    if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus()
        .equals(Status.User.OUT_GAME)) {
      e.setCancelled(true);
      if (user.getLocation().getY()
          < user.getWorld().getMinHeight() - MAX_LOWER_THAN_MIN_HEIGHT) {
        ((SpectatorUser) user).teleportToSpectatorSpawn();
      }
    }
  }

  @EventHandler
  public void onUserMove(UserMoveEvent e) {
    User user = e.getUser();
    if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus()
        .equals(Status.User.OUT_GAME)) {
      if (user.getLocation().getY()
          < user.getWorld().getMinHeight() - MAX_LOWER_THAN_MIN_HEIGHT) {
        ((SpectatorUser) user).teleportToSpectatorSpawn();
      }
    }
  }

  @EventHandler
  public void onPlayerPickupExperience(PlayerPickupExperienceEvent e) {
    User user = Server.getUser(e.getPlayer());
    if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus()
        .equals(Status.User.OUT_GAME)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onUserPickupItem(UserAttemptPickupItemEvent e) {
    User user = e.getUser();
    if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus()
        .equals(Status.User.OUT_GAME)) {
      e.setFlyAtPlayer(false);
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDamageByUser(EntityDamageByUserEvent e) {
    User user = e.getUser();
    if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus()
        .equals(Status.User.OUT_GAME)) {
      e.setCancelDamage(true);
      e.setCancelled(true);
    }
  }

  public abstract @Nullable Sideboard getSpectatorSideboard();

  public abstract @Nullable Chat getSpectatorChat();

  public abstract ExLocation getSpectatorSpawn();

  public abstract boolean loadTools();

  public GameMode getReJoinGameMode() {
    return GameMode.ADVENTURE;
  }
}
