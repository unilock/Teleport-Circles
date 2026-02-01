package io.github.jason13official.telecir;

import io.github.jason13official.telecir.impl.client.model.CircleModel;
import io.github.jason13official.telecir.impl.client.particle.TeleportCircleParticle;
import io.github.jason13official.telecir.impl.client.renderer.CircleRenderer;
import io.github.jason13official.telecir.impl.common.network.packet.ManagerSyncS2CPacket;
import io.github.jason13official.telecir.impl.common.registry.ModEntities;
import io.github.jason13official.telecir.impl.common.registry.ModParticles;
import io.github.jason13official.telecir.impl.server.data.CircleRecord;
import java.util.LinkedHashMap;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class TeleCirClient implements ClientModInitializer {

  public static LinkedHashMap<UUID, CircleRecord> synchronizedRecords = new LinkedHashMap<>();

  @Override
  public void onInitializeClient() {
    long start = System.currentTimeMillis();
    Constants.debug("Began client initialization.");

    EntityRendererRegistry.register(ModEntities.CIRCLE, CircleRenderer::new);

    EntityModelLayerRegistry.registerModelLayer(ConstantsClient.CIRCLE_MODEL_LAYER,
        CircleModel::createBodyLayer);

    ParticleFactoryRegistry.getInstance()
        .register(ModParticles.CIRCLE, TeleportCircleParticle.SmallProvider::new);

    ClientPlayNetworking.registerGlobalReceiver(ManagerSyncS2CPacket.TYPE,
        ManagerSyncS2CPacket::handleOnClient);

    Constants.debug("Ended client initialization.");
    long total = System.currentTimeMillis() - start;
    Constants.debug("Client initialized in {}ms", total);
  }
}