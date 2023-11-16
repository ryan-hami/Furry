package ryan.furry.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin({ModelPart.class})
public abstract class ModelPartMixin {
    @Shadow public abstract void rotate(MatrixStack matrices);
    @Shadow public boolean hidden;
    @Shadow @Final private Map<String, ModelPart> children;
    @Shadow @Final private List<ModelPart.Cuboid> cuboids;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At("RETURN"))
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha, CallbackInfo ci) {
        matrices.push();
        rotate(matrices);
        if (!hidden) {
            MatrixStack.Entry entry = matrices.peek();
            for (ModelPart.Cuboid cuboid : cuboids) {
                Matrix4f position = entry.getPositionMatrix();
                Matrix3f normal = entry.getNormalMatrix();
                for (ModelPart.Quad quad : ((CuboidAccessor) cuboid).getSides()) {
                    Vector3f vector3f = normal.transform(new Vector3f(quad.direction));

                    for (int q = 0; q < 1; ++q) {
                        for (int n = 0; n < 4; ++n) {
                            float dh = (float) (n + 1) / 16;
                            Vector3f sn = new Vector3f(vector3f).mul(dh);

                            List<float[]> arrs = new ArrayList<>();
                            for (int i = 0; i < 4; ++i) {
                                ModelPart.Vertex vertex = quad.vertices[i];
                                // [-0.5, 0.5]
                                float x = vertex.pos.x / 16.0f;
                                float y = vertex.pos.y / 16.0f;
                                float z = vertex.pos.z / 16.0f;

                                Vector4f scaledPos = position.transform(new Vector4f(x, y, z, 1.0f));
                                arrs.add(new float[]{scaledPos.x, scaledPos.y, scaledPos.z, vertex.u, vertex.v});
                            }
                            arrs.sort((a1, a2) -> {
                                int result = Float.compare(a1[0], a2[0]);
                                if (result == 0) result = Float.compare(a1[1], a2[1]);
                                return result == 0 ? Float.compare(a1[2], a2[2]) : result;
                            }); // a b d c
                            arrs = Arrays.asList(arrs.get(0), arrs.get(1), arrs.get(3), arrs.get(2));

                            for (float[] p : arrs) {
                                vertices.vertex(p[0] + sn.x, p[1] + sn.y, p[2] + sn.z, red, green, blue, alpha, p[3], p[4], overlay, light, vector3f.x, vector3f.y, vector3f.z);
                            }
                        }
                    }
                }
            }
        }
        for (ModelPart modelPart : children.values()) {
            modelPart.render(matrices, vertices, light, overlay, red, green, blue, alpha);
        }
        matrices.pop();
    }

    @Mixin({ModelPart.Cuboid.class})
    public interface CuboidAccessor {
        @Accessor ModelPart.Quad[] getSides();
    }
}

