package io.github.jason13official.telecir.impl.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.jason13official.telecir.ConstantsClient;
import io.github.jason13official.telecir.TeleCir;
import io.github.jason13official.telecir.impl.client.model.CircleModel;
import io.github.jason13official.telecir.impl.common.world.entity.TeleportCircle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CircleRenderer extends EntityRenderer<TeleportCircle> {

  // unused as the model defines the Function<ResourceLocation, RenderType> to apply
//  private static final RenderType DEBUG_RENDER_TYPE = RenderType.entityCutout(
//      Constants.CIRCLE_DEBUG_TEXTURE);
//  private static final RenderType ACTIVE_RENDER_TYPE = RenderType.entityCutout(
//      Constants.CIRCLE_ACTIVE_TEXTURE);
//  private static final RenderType INACTIVE_RENDER_TYPE = RenderType.entityCutout(
//      Constants.CIRCLE_INACTIVE_TEXTURE);

  private final CircleModel model;

  public CircleRenderer(Context context) {
    super(context);
    this.model = new CircleModel(context.bakeLayer(ConstantsClient.CIRCLE_MODEL_LAYER));
  }

  @Override
  public ResourceLocation getTextureLocation(TeleportCircle circle) {
    return TeleCir.DEBUG ? ConstantsClient.CIRCLE_DEBUG_TEXTURE
        : circle.activated() ? ConstantsClient.CIRCLE_ACTIVE_TEXTURE : ConstantsClient.CIRCLE_INACTIVE_TEXTURE;
  }

  @Override
  public void render(TeleportCircle circle, float entityYaw, float partialTick, PoseStack poseStack,
      MultiBufferSource bufferSource, int packedLight) {

    poseStack.pushPose();

    // get the circle out of the ground
    // poseStack.translate(0.0F, 0.0625F, 0.0F);
    poseStack.translate(0.0F, 0.0125F, 0.0F);

    // hack/fix copied from BoatRenderer...
    poseStack.scale(-1.0F, -1.0F, 1.0F);

    // rotation
    if (circle.activated()) {
      poseStack.rotateAround(Axis.YP.rotationDegrees(circle.getYRot()), 0, 0, 0);
    }

    // apply the RenderType define by the model, using the correct texture location
    VertexConsumer vertexConsumer = bufferSource.getBuffer(
        this.model.renderType(getTextureLocation(circle)));

    // actually render
    this.model.renderToBuffer(poseStack, vertexConsumer, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY);

    poseStack.popPose();

    // BoatRenderer also calls to super, but this might be unnecessary?
    // super.render(circle, entityYaw, partialTick, poseStack, bufferSource, packedLight);
  }
}
