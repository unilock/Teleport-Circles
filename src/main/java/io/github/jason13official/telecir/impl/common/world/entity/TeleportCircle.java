package io.github.jason13official.telecir.impl.common.world.entity;

import io.github.jason13official.telecir.Constants;
import io.github.jason13official.telecir.TeleCirServer;
import io.github.jason13official.telecir.impl.client.screen.LocationTeleportScreen;
import io.github.jason13official.telecir.impl.common.network.packet.ManagerSyncS2CPacket;
import io.github.jason13official.telecir.impl.common.registry.ModParticles;
import io.github.jason13official.telecir.impl.common.util.RotationHelper;
import io.github.jason13official.telecir.impl.server.data.CircleRecord;
import io.netty.util.Constant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

public class TeleportCircle extends AbstractTeleportCircle {

  public static final EntityDataAccessor<Boolean> ACTIVATED = SynchedEntityData.defineId(
      TeleportCircle.class, EntityDataSerializers.BOOLEAN);

  public TeleportCircle(EntityType<?> entityType, Level level) {
    super(entityType, level);
    this.blocksBuilding = true;
  }

  @Override
  protected void defineSynchedData(Builder builder) {
    builder.define(ACTIVATED, false);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag compound) {
    boolean savedActivationState = compound.getBoolean("activated");
    this.activated(savedActivationState);

    Constants.debug("Restored activation state for circle {} to {}", this.getStringUUID(), savedActivationState);

    // Update the circle record if the server is initialized and the activation state changed
    if (!this.level().isClientSide() && TeleCirServer.getInstance() != null && this.hasCustomName()) {
      TeleCirServer.getManager().setMapping(this.getUUID(),
          new CircleRecord(this.getName().getString(), this.level().dimension(), this.position(), this.activated()));
      Constants.debug("Updated CircleRecord activation state to {} for circle {}", this.activated(), this.getStringUUID());
    }
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag compound) {
    compound.putBoolean("activated", this.activated());
  }

  /// getter
  public final boolean activated() {
    return this.entityData.get(ACTIVATED);
  }

  /// setter
  public final void activated(boolean value) {
    this.entityData.set(ACTIVATED, value);
  }

  @Override
  public void kill() {

    if (!this.level().isClientSide()) {
      TeleCirServer.getManager().dereference(this.getUUID());
    }

    super.kill();

    if (this.level() instanceof ServerLevel serverLevel) {
      serverLevel.getServer().getAllLevels().forEach(level -> {
        level.players().forEach(serverPlayer -> {
          ManagerSyncS2CPacket.createAndSend(serverPlayer, level);
        });
      });
    }
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {

    if (!this.level().isClientSide) {

      if (!this.activated()) {
        this.activated(true);

        String name = TeleCirServer.getManager().generateUniqueName();

        // also sets the mapping on the logical side
        this.setCustomName(Component.literal(name));
      }

      Constants.debug("syncing all circles to all players");
      if (this.level() instanceof ServerLevel serverLevel) {
        serverLevel.getServer().getAllLevels().forEach(level -> {
          level.players().forEach(serverPlayer -> {
            ManagerSyncS2CPacket.createAndSend(serverPlayer, level);
          });
        });
      }

      Constants.debug("interacted with teleport circle on logical side successfully");
      return InteractionResult.PASS;
    } else {

      if (this.activated()) {
        this.openLocationTeleportScreen();
      }

      return InteractionResult.SUCCESS;
    }
  }

  @Override
  public void setCustomName(@Nullable Component name) {
    super.setCustomName(name);

    if (name == null || this.level().isClientSide()) {
      return;
    }

    Constants.debug("Circle loaded from memory " + this.getStringUUID());
    Constants.debug("Logical side of mod already initialized? " + String.valueOf(TeleCirServer.getInstance() != null));

    if (TeleCirServer.getInstance() == null) {
      TeleCirServer.PRELOAD.put(this.getUUID(),
          new CircleRecord(name.getString(), this.level().dimension(), this.position(),
              this.activated()));
      Constants.debug("Added circle to preload map");
    } else {
      TeleCirServer.getManager().setMapping(this.getUUID(),
          new CircleRecord(name.getString(), this.level().dimension(), this.position(),
              this.activated()));
    }
  }

  @Override
  public void tick() {
    super.tick();

    if (activated()) {
      this.setYRot((this.getYRot() + 1.0f) % 360.0f);
    }

    if (!(this.level() instanceof ServerLevel level)) {
      return;
    }

    final Vec3 offset = this.position().add(0, this.getBbHeight() / 2.0, 0);
    final double halfWidth = 1.0D;
    final double height = 2.0D;

    // Create the original bounding box
    final AABB originalBox = new AABB(offset.x - halfWidth, offset.y, offset.z - halfWidth,
        offset.x + halfWidth, offset.y + height, offset.z + halfWidth);

    // Rotate the bounding box around the center point
    // Using the entity's Y rotation converted to radians
    final double yRotationRadians = Math.toRadians(this.getYRot());
    final Vec3 rotationCenter = new Vec3(offset.x, offset.y, offset.z); // Bottom-center of the box

    // get rotated vertices for rendering
    final Vec3[] rotatedVertices = RotationHelper.rotateAABBCorners(originalBox.minX, originalBox.minY, originalBox.minZ, originalBox.maxX, originalBox.maxY,
        originalBox.maxZ, rotationCenter, 0, yRotationRadians, 0);

    // Render rotated bounding box vertices
//    for (Vec3 pos : rotatedVertices) {
//      level.sendParticles(ModParticles.CIRCLE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.01);
//    }

    // only render half of the vertices
    if (activated() && level.getGameTime() % 2 == 0) {
      for (int i = 0; i <= rotatedVertices.length; i++) {
        switch (i) {
          case 0, 1, 4, 5 -> level.sendParticles(ModParticles.CIRCLE, rotatedVertices[i].x,
              rotatedVertices[i].y + 0.0625D, rotatedVertices[i].z, 1, 0, 0, 0, 0.01);
        }
      }
    }
  }

  @Environment(EnvType.CLIENT)
  private void openLocationTeleportScreen() {
      Minecraft.getInstance()
              .setScreen(new LocationTeleportScreen(this.getUUID(), this.getName().getString()));
  }
}
