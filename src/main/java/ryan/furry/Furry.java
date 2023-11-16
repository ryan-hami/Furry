package ryan.furry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import ryan.furry.mixin.EntityRendererMixin.ITexturedModelDataMixin;
import ryan.furry.mixin.EntityRendererMixin.IModelPartDataMixin;

import java.util.*;

public class Furry implements ModInitializer {
    public static final RenderLayer BEACON = RenderLayer.getEntityCutoutNoCull(new Identifier("textures/entity/beacon_beam.png"));
    public static final Map<String, EntityModelLayer> LOC_TO_LAYER;
    public static final Map<EntityModelLayer, ITexturedModelDataMixin> LAYER_TO_MODEL;

    @Override
    public void onInitialize() {
    }

    public static Direction fromVector(Vector3f unitVector) {
        if (unitVector.y == 1)  return Direction.UP;
        if (unitVector.y == -1) return Direction.DOWN;
        if (unitVector.x == 1)  return Direction.EAST;
        if (unitVector.x == -1) return Direction.WEST;
        if (unitVector.z == 1)  return Direction.SOUTH;
        if (unitVector.z == -1) return Direction.NORTH;
        return null;
    }

    public static int compareXYZUV(float[] a1, float[] a2) {
        int result = Float.compare(a1[0], a2[0]);
        if (result == 0) result = Float.compare(a1[1], a2[1]);
        return result == 0 ? Float.compare(a1[2], a2[2]) : result;
    }

    public static List<float[]> sort(List<float[]> arrs) {
        arrs.sort(Furry::compareXYZUV);
        return Arrays.asList(arrs.get(0), arrs.get(1), arrs.get(3), arrs.get(2));
    }

    static {
        LOC_TO_LAYER = new HashMap<>();
        EntityModelLayers.getLayers().forEach(var -> LOC_TO_LAYER.put(var.getId().getPath() + var.getName(), var));

        LAYER_TO_MODEL = new HashMap<>();
        EntityModels.getModels().forEach((key, value) -> LAYER_TO_MODEL.put(key, (ITexturedModelDataMixin) value));
    }

    @FunctionalInterface public interface XYZUV { void eat(float x, float y, float z, float u, float v); }
    @FunctionalInterface public interface ModelMuncher { void eat(String name, IModelPartDataMixin modelData); }
}
