package ryan.furry;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import ryan.furry.mixin.EntityRendererMixin.*;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"FieldMayBeFinal", "unused", "FieldCanBeLocal"})
public class Plushie<T extends Entity> {
    private T entity;
    private float yaw;
    private float tickDelta;
    private MatrixStack matrices;
    private VertexConsumerProvider vertexConsumers;
    private int light;

    private VertexConsumer vertexConsumer;
    private Matrix4f positionMatrix;
    private Matrix3f normalMatrix;
    private String path;

    private final Furry.XYZUV NAWM =
            (x, y, z, u, v) -> vertex(vertexConsumer, positionMatrix, normalMatrix, x, y, z, u, v);
    private final Furry.ModelMuncher HUNGY =
            (k, v) -> traverseChildren(k, v, v.getCuboidData(), (IModelTransformMixin) v.getRotationData());

    public Plushie(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                   int light, String path) {
        this.vertexConsumers = vertexConsumers;
        this.tickDelta = tickDelta;
        this.matrices = matrices;
        this.entity = entity;
        this.light = light;
        this.path = path;
        this.yaw = yaw;
    }

    public void cakeup() {
        EntityModelLayer entityModelLayer = Furry.LOC_TO_LAYER.get(path + "main");
        ITexturedModelDataMixin texturedModelData = Furry.LAYER_TO_MODEL.get(entityModelLayer);
        IModelPartDataMixin mainModelData = (IModelPartDataMixin) texturedModelData.getData().getRoot();
        ITextureDimensionsMixin dimensions = (ITextureDimensionsMixin) texturedModelData.getDimensions();

        float lerpedYaw = entity.getYaw(tickDelta);
        float lerpedPitch = entity.getPitch(tickDelta);
        vertexConsumer = vertexConsumers.getBuffer(Furry.BEACON);

        matrices.push();
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(lerpedYaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lerpedPitch));

        MatrixStack.Entry entry = matrices.peek();
        positionMatrix = entry.getPositionMatrix();
        normalMatrix = entry.getNormalMatrix();

        //zUnitQuad();
        HUNGY.eat("main", mainModelData);
        matrices.pop();
    }

    private void traverseChildren(String name, IModelPartDataMixin modelData, List<IModelCuboidDataMixin> cuboids, IModelTransformMixin transform) {
        Map<String, IModelPartDataMixin> children = modelData.getChildren();
        if (children.size() > 1) children.forEach(HUNGY::eat);


    }

    private void zUnitQuad() {
        NAWM.eat(0, 0, 0, 0, 0);
        NAWM.eat(0, 0, 1, 0, 1);
        NAWM.eat(0, 1, 1, 1, 1);
        NAWM.eat(0, 1, 0, 1, 0);
    }

    private void vertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Matrix3f normalMatrix,
                                float x, float y, float z, float u, float v) {
        vertexConsumer
                .vertex(positionMatrix, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
                .next();
    }
}
