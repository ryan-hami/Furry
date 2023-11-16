package ryan.furry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.util.Identifier;
import ryan.furry.mixin.EntityRendererMixin;
import ryan.furry.mixin.EntityRendererMixin.ITexturedModelDataMixin;

import java.util.*;

public class Furry implements ModInitializer {
	public static final RenderLayer BEACON = RenderLayer.getEntityCutoutNoCull(new Identifier("textures/entity/beacon_beam.png"));
	public static final Map<String, EntityModelLayer> LOC_TO_LAYER;
	public static final Map<EntityModelLayer, ITexturedModelDataMixin> LAYER_TO_MODEL;

	@Override
	public void onInitialize() {
	}

	static {
		LOC_TO_LAYER = new HashMap<>();
		EntityModelLayers.getLayers().forEach(var -> LOC_TO_LAYER.put(var.getId().getPath() + var.getName(), var));

		LAYER_TO_MODEL = new HashMap<>();
		EntityModels.getModels().forEach((key, value) -> LAYER_TO_MODEL.put(key, (ITexturedModelDataMixin) value));

		LOC_TO_LAYER.forEach((key, value) -> System.out.println(key));
	}

	@FunctionalInterface public interface XYZUV { void eat(float x, float y, float z, float u, float v); }
	@FunctionalInterface public interface ModelMuncher { void eat(String name, EntityRendererMixin.IModelPartDataMixin modelData); }
}
