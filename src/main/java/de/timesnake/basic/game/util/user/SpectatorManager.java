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
import de.timesnake.basic.game.util.server.GameServer;
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

import java.util.*;

public abstract class SpectatorManager implements UserInventoryClickListener, PacketPlayOutListener, Listener {

  // teleports the spectator to spawn if he goes lower than min height - value
  public static final Integer MAX_LOWER_THAN_MIN_HEIGHT = 10;
  private static final Integer LEAVE_TIME_MILLIS = 2000;

  public static final ExItemStack USER_INV = new ExItemStack(1, Material.PLAYER_HEAD,
      "§9Teleporter").setMoveable(false).setDropable(false).immutable()
      .onInteract(event -> ((SpectatorUser) event.getUser()).openGameUserInventory(), true);
  public static final ExItemStack GLOWING = new ExItemStack(2, Material.SPECTRAL_ARROW,
      "§6Glowing").setMoveable(false).setDropable(false).immutable()
      .onInteract(event -> {
        SpectatorUser user = ((SpectatorUser) event.getUser());
        user.setGlowingEnabled(!user.hasGlowingEnabled());
        if (user.hasGlowingEnabled()) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Enabled glowing", ExTextColor.PERSONAL));
          event.getClickedItem().enchant();
        } else {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Disabled glowing", ExTextColor.PERSONAL));
          event.getClickedItem().disenchant();
        }
        GameServer.getSpectatorManager().sendGlowUpdateToUser(user);
        user.updateInventory();
      }, true, true);
  public static final ExItemStack SPEED = new ExItemStack(3, Material.FEATHER, "§bSpeed")
      .setMoveable(false).setDropable(false).immutable()
      .onInteract(event -> {
        SpectatorUser user = ((SpectatorUser) event.getUser());
        user.setSpeedEnabled(!user.hasSpeedEnabled());
        if (user.hasSpeedEnabled()) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Enabled speed", ExTextColor.PERSONAL));
          event.getClickedItem().enchant();
        } else {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Disabled speed", ExTextColor.PERSONAL));
          event.getClickedItem().disenchant();
        }
        user.updateInventory();
      }, true, true);
  public static final ExItemStack FLYING = new ExItemStack(4, Material.RABBIT_FOOT, "§9Flying")
      .setMoveable(false).setDropable(false).immutable()
      .onInteract(event -> {
        SpectatorUser user = ((SpectatorUser) event.getUser());
        user.setFlyEnabled(!user.hasFlyEnabled());
        user.sendPluginMessage(Plugin.GAME,
            Component.text((user.getAllowFlight() ? "Enabled" : "Disabled") + " flying",
                ExTextColor.PERSONAL));
        if (user.getAllowFlight()) {
          event.getClickedItem().enchant();
        } else {
          event.getClickedItem().disenchant();
        }
        user.updateInventory();
      }, true, true);
  public static final ExItemStack LEAVE_ITEM = new ExItemStack(8, Material.ANVIL,
      "§6Leave (hold right)").setMoveable(false).setDropable(false).immutable()
      .onInteract(event -> {
        SpectatorUser user = ((SpectatorUser) event.getUser());
        if (event.getAction().isRightClick()) {
          if (!GameServer.getSpectatorManager().clickedLeaveUsers.containsKey(user)) {
            GameServer.getSpectatorManager().clickedLeaveUsers.put(user, new ItemHoldClick(LEAVE_TIME_MILLIS));
          } else {
            if (GameServer.getSpectatorManager().clickedLeaveUsers.get(user).click()) {
              user.sendActionBarText(Component.empty());
              user.switchToLobbyLast();
            } else {
              user.sendActionBarText(Component.text("Leaving...", ExTextColor.WARNING));
            }
          }
        }
      }, true);


  private final UserMap<User, ItemHoldClick> clickedLeaveUsers = new UserMap<>();
  private final HashMap<Integer, User> userHeadsById = new HashMap<>();
  private ExInventory gameUserInv;
  private Set<User> glowingUsers = new HashSet<>();

  public SpectatorManager() {
    this.gameUserInv = new ExInventory(9, Component.text("Players"));

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
    this.gameUserInv = new ExInventory(inGame, Component.text("Players"));
    this.userHeadsById.clear();
    int slot = 0;
    for (User user : Server.getInGameUsers()) {
      ExItemStack head = ExItemStack.getHead(user.getPlayer(), user.getTDChatName()).setLore("", "§7Click to " + "teleport");
      this.userHeadsById.put(head.getId(), user);
      this.gameUserInv.setItemStack(slot, head);
      Server.getInventoryEventManager().addClickListener(this, head);
      slot++;
    }
  }

  public ExInventory getGameUserInventory() {
    return gameUserInv;
  }

  public void sendGlowUpdateToUser(User user) {
    this.sendUpdatePackets(user);
  }

  public void updateGlowingPlayers() {
    this.glowingUsers = new HashSet<>(Server.getInGameUsers());
    Server.getUsers(u -> u.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR)).forEach(this::sendUpdatePackets);
  }

  private void sendUpdatePackets(User user) {
    for (User glowingUser : this.glowingUsers) {
      Packet<?> packet = new ClientboundSetEntityDataPacketBuilder(glowingUser.getMinecraftPlayer())
          .setFlagsFromEntity()
          .build();
      user.sendPacket(packet);
    }
  }

  @PacketHandler(type = ClientboundSetEntityDataPacket.class, modify = true)
  public Packet<?> onPacketPlayOut(Packet<?> packet, Player receiver) {
    if (!(packet instanceof ClientboundSetEntityDataPacket dataPacket)) {
      return packet;
    }

    if (!ClientboundSetEntityDataPacketBuilder.isPosePacket(dataPacket)) {
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
      return dataPacket;
    }

    if (!this.glowingUsers.contains(Server.getUser(player))) {
      return dataPacket;
    }

    SpectatorUser user = (SpectatorUser) Server.getUser(receiver);

    if (!(user.hasGlowingEnabled()) || !(user.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR))) {
      return dataPacket;
    }

    dataPacket = new ClientboundSetEntityDataPacketBuilder(((CraftPlayer) player).getHandle())
        .setDefaultFlags()
        .setFlags(ClientboundSetEntityDataPacketBuilder.getFlagsOfPacket(dataPacket))
        .setFlag(ClientboundSetEntityDataPacketBuilder.Type.GLOWING, true)
        .build();

    return dataPacket;
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
