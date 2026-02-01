package io.github.jason13official.telecir;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ConstantsClient {

  public static final ModelLayerLocation CIRCLE_MODEL_LAYER = new ModelLayerLocation(
      TeleCir.identifier("circle"), "main");

  public static final ResourceLocation CIRCLE_DEBUG_TEXTURE = TeleCir.identifier(
      "textures/entity/debug.png");

  public static final ResourceLocation CIRCLE_INACTIVE_TEXTURE = TeleCir.identifier(
      "textures/entity/small_circle.png");

  public static final ResourceLocation CIRCLE_ACTIVE_TEXTURE = TeleCir.identifier(
      "textures/entity/circle.png");
}
