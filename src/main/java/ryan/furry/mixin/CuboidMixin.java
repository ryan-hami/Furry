package ryan.furry.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({ModelPart.Cuboid.class})
public abstract class CuboidMixin {
    @Shadow @Final private ModelPart.Quad[] sides;

    /**
     * @author ryan
     * @reason as a standalone mod, this will be a major optimization whilst having no effect on any human to ever live.
     */
    @Overwrite
    public void renderCuboid(MatrixStack.Entry entry, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f position = entry.getPositionMatrix();
        Matrix3f normal = entry.getNormalMatrix();
        for (ModelPart.Quad quad : this.sides) {
            Vector3f transNorm = normal.transform(new Vector3f(quad.direction));
            float[][] verticies = new float[4][5];
            for (int index = 0; index < quad.vertices.length; ++index) {
                ModelPart.Vertex vertex = quad.vertices[index];
                float i = vertex.pos.x() / 16.0f;
                float j = vertex.pos.y() / 16.0f;
                float k = vertex.pos.z() / 16.0f;
                Vector4f rotVert = position.transform(new Vector4f(i, j, k, 1.0f));
                vertices.vertex(rotVert.x(), rotVert.y(), rotVert.z(), red, green, blue, alpha, vertex.u, vertex.v, overlay, light, transNorm.x, transNorm.y, transNorm.z);
                verticies[index] = new float[]{rotVert.x, rotVert.y, rotVert.z, vertex.u, vertex.v};
            }

            // TODO: animate scalar wrt time or ticks
            Vector3f sa = new Vector3f(transNorm).mul(1 / 16f);
            Vector3f sb = new Vector3f(transNorm).mul(1 / 12f);
            Vector3f sc = new Vector3f(transNorm).mul(1 / 32f);
            Vector3f sd = new Vector3f(transNorm).mul(1 / 48f);

            // TODO: abstract for n rows and m columns of shells (or just do 2 minutes of research / rewatch video and make a real implementation)
            float[] p1 = verticies[0], p2 = verticies[1], p3 = verticies[2], p4 = verticies[3];
            Vector3f mid = new Vector3f((p1[0] + p2[0] + p3[0] + p4[0]) / 4, (p1[1] + p2[1] + p3[1] + p4[1]) / 4, (p1[2] + p2[2] + p3[2] + p4[2]) / 4);
            Vector3f b12 = new Vector3f((p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2, (p1[2] + p2[2]) / 2);
            Vector3f b14 = new Vector3f((p1[0] + p4[0]) / 2, (p1[1] + p4[1]) / 2, (p1[2] + p4[2]) / 2);
            Vector3f b32 = new Vector3f((p3[0] + p2[0]) / 2, (p3[1] + p2[1]) / 2, (p3[2] + p2[2]) / 2);
            Vector3f b34 = new Vector3f((p4[0] + p3[0]) / 2, (p4[1] + p3[1]) / 2, (p4[2] + p3[2]) / 2);

            Vector2f uvm = new Vector2f((p1[3] + p2[3] + p3[3] + p4[3]) / 4, (p1[4] + p2[4] + p3[4] + p4[4]) / 4);
            Vector2f t12 = new Vector2f((p1[3] + p2[3]) / 2, (p1[4] + p2[4]) / 2);
            Vector2f t14 = new Vector2f((p1[3] + p4[3]) / 2, (p1[4] + p4[4]) / 2);
            Vector2f t32 = new Vector2f((p3[3] + p2[3]) / 2, (p3[4] + p2[4]) / 2);
            Vector2f t34 = new Vector2f((p4[3] + p3[3]) / 2, (p4[4] + p3[4]) / 2);

            vertices.vertex(p1[0] + sa.x, p1[1] + sa.y, p1[2] + sa.z, red, green, blue, alpha, p1[3], p1[4], overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b12.x + sa.x, b12.y + sa.y, b12.z + sa.z, red, green, blue, alpha, t12.x, t12.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(mid.x + sa.x, mid.y + sa.y, mid.z + sa.z, red, green, blue, alpha, uvm.x, uvm.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b14.x + sa.x, b14.y + sa.y, b14.z + sa.z, red, green, blue, alpha, t14.x, t14.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);

            vertices.vertex(p2[0] + sb.x, p2[1] + sb.y, p2[2] + sb.z, red, green, blue, alpha, p2[3], p2[4], overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b12.x + sb.x, b12.y + sb.y, b12.z + sb.z, red, green, blue, alpha, t12.x, t12.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(mid.x + sb.x, mid.y + sb.y, mid.z + sb.z, red, green, blue, alpha, uvm.x, uvm.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b32.x + sb.x, b32.y + sb.y, b32.z + sb.z, red, green, blue, alpha, t32.x, t32.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);

            vertices.vertex(p3[0] + sc.x, p3[1] + sc.y, p3[2] + sc.z, red, green, blue, alpha, p3[3], p3[4], overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b34.x + sc.x, b34.y + sc.y, b34.z + sc.z, red, green, blue, alpha, t34.x, t34.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(mid.x + sc.x, mid.y + sc.y, mid.z + sc.z, red, green, blue, alpha, uvm.x, uvm.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b32.x + sc.x, b32.y + sc.y, b32.z + sc.z, red, green, blue, alpha, t32.x, t32.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);

            vertices.vertex(p4[0] + sd.x, p4[1] + sd.y, p4[2] + sd.z, red, green, blue, alpha, p4[3], p4[4], overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b34.x + sd.x, b34.y + sd.y, b34.z + sd.z, red, green, blue, alpha, t34.x, t34.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(mid.x + sd.x, mid.y + sd.y, mid.z + sd.z, red, green, blue, alpha, uvm.x, uvm.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
            vertices.vertex(b14.x + sd.x, b14.y + sd.y, b14.z + sd.z, red, green, blue, alpha, t14.x, t14.y, overlay, light, transNorm.x, transNorm.y, transNorm.z);
        }
    }
}
