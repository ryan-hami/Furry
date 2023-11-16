package ryan.furry.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ryan.furry.Furry;
import ryan.furry.Furry.XYZUV;

@Mixin({ModelPart.Cuboid.class})
public class CuboidMixin {
    @Shadow @Final private ModelPart.Quad[] sides;

    /**
     * @author ryan
     * @reason as a random mod, this will be a major optimization whilst having no effect on any human to ever live.
     */
    @Overwrite
    public void renderCuboid(MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f position = entry.getPositionMatrix();
        Matrix3f normal = entry.getNormalMatrix();
        for (ModelPart.Quad quad : sides) {
            Vector3f transNorm = normal.transform(new Vector3f(quad.direction));
            XYZUV[] vrts = new XYZUV[4];
            for (int index = 0; index < quad.vertices.length; ++index) {
                ModelPart.Vertex vertex = quad.vertices[index];
                Vector4f rotVert = position.transform(new Vector4f(vertex.pos.x() / 16.0f, vertex.pos.y() / 16.0f, vertex.pos.z() / 16.0f, 1.0f));
                vertexConsumer.vertex(rotVert.x(), rotVert.y(), rotVert.z(), red, green, blue, alpha, vertex.u, vertex.v, overlay, light, transNorm.x, transNorm.y, transNorm.z);
                vrts[index] = new XYZUV(rotVert.x, rotVert.y, rotVert.z, vertex.u, vertex.v);
            }

            Furry.dice(vrts, transNorm, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }
}
