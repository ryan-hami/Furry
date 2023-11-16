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
        for (ModelPart.Quad quad : this.sides) {
            Vector3f transNorm = normal.transform(new Vector3f(quad.direction));
            float[][] vrts = new float[4][5];
            for (int index = 0; index < quad.vertices.length; ++index) {
                ModelPart.Vertex vertex = quad.vertices[index];
                float i = vertex.pos.x() / 16.0f;
                float j = vertex.pos.y() / 16.0f;
                float k = vertex.pos.z() / 16.0f;
                Vector4f rotVert = position.transform(new Vector4f(i, j, k, 1.0f));
                vertexConsumer.vertex(rotVert.x(), rotVert.y(), rotVert.z(), red, green, blue, alpha, vertex.u, vertex.v, overlay, light, transNorm.x, transNorm.y, transNorm.z);
                vrts[index] = new float[]{rotVert.x, rotVert.y, rotVert.z, vertex.u, vertex.v};
            }

            Furry.dice(vrts, transNorm, vertexConsumer, light, overlay, red, green, blue, alpha);

            /*// max number of layers
            int max = 16;

            // space between layers
            float d = 1f / (max * 8);

            int rows = 2;
            int cols = 2;

            int total = rows * cols;

            float dx = sums[0] / total;
            float dy = sums[1] / total;
            float dz = sums[2] / total;
            float du = sums[3] / total;
            float dv = sums[4] / total;

            XYZUV p12 = new XYZUV(vrts[0][0] + vrts[1][0], vrts[0][1] + vrts[1][1], vrts[0][2] + vrts[1][2], vrts[0][3] + vrts[1][3], vrts[0][4] + vrts[1][4]);
            XYZUV p14 = new XYZUV(vrts[0][0] + vrts[3][0], vrts[0][1] + vrts[3][1], vrts[0][2] + vrts[3][2], vrts[0][3] + vrts[3][3], vrts[0][4] + vrts[3][4]);
            XYZUV p32 = new XYZUV(vrts[2][0] + vrts[1][0], vrts[2][1] + vrts[1][1], vrts[2][2] + vrts[1][2], vrts[2][3] + vrts[1][3], vrts[2][4] + vrts[1][4]);
            XYZUV p34 = new XYZUV(vrts[2][0] + vrts[3][0], vrts[2][1] + vrts[3][1], vrts[2][2] + vrts[3][2], vrts[2][3] + vrts[3][3], vrts[2][4] + vrts[3][4]);

            float delta = 1f / total;
            //p12.scale(delta);
            //p14.scale(delta);
            //p32.scale(delta);
            //p34.scale(delta);

            for (int row = 0; row < rows; ++row) {
                for (int col = 0; col < cols; ++col) {
                    int index = row * cols + col;
                    double progress = (float) (index + 1) / total;
                    int layers = (int) (max * Math.exp(-Math.pow((double) 2 * index / total - 1, 2)));

                    for (int layer = 0; layer < layers; ++layer) {
                        float h = d * layer;
                        Vector3f s = new Vector3f(transNorm).mul(h);
                        for (int c = 1; c < 5; ++c) {
                            //vertexConsumer.vertex(x * c + s.x, y * c + s.y, z * c + s.z, red, green, blue, alpha, u * c, v * c, overlay, light, transNorm.x, transNorm.y, transNorm.z);
                        }
                    }
                }
            }*/
        }
    }
}
