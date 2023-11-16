package ryan.furry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Vector3f;

public class Furry implements ModInitializer {
    @Override
    public void onInitialize() {
    }

    public static void dice(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light, int overlay,
                            float red, float green, float blue, float alpha) {

        // TODO: animate scalar wrt time or ticks
        Vector3f sa = new Vector3f(transNorm).mul(1 / 16f);
        Vector3f sb = new Vector3f(transNorm).mul(1 / 12f);
        Vector3f sc = new Vector3f(transNorm).mul(1 / 32f);
        Vector3f sd = new Vector3f(transNorm).mul(1 / 48f);

        XYZUV vx0 = verticies[0];
        XYZUV vx1 = verticies[1];
        XYZUV vx2 = verticies[2];
        XYZUV vx3 = verticies[3];

        XYZUV mid = vx0.add(vx1).add(vx2).add(vx3).scale(1 / 4f);

        XYZUV p12 = vx0.add(vx1).scale(1 / 2f);
        XYZUV p14 = vx0.add(vx3).scale(1 / 2f);
        XYZUV p32 = vx2.add(vx1).scale(1 / 2f);
        XYZUV p34 = vx2.add(vx3).scale(1 / 2f);

        // TODO: abstract for n rows and m columns of shells (or just do 2 minutes of research / rewatch video and make a real implementation)
        Muncher HUNGY = (x, y, z, u, v) -> vertexConsumer
                .vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, transNorm.x, transNorm.y, transNorm.z);

        HUNGY.eat(vx0.add(sa));
        HUNGY.eat(p12.add(sa));
        HUNGY.eat(mid.add(sa));
        HUNGY.eat(p14.add(sa));

        HUNGY.eat(vx1.add(sb));
        HUNGY.eat(p12.add(sb));
        HUNGY.eat(mid.add(sb));
        HUNGY.eat(p32.add(sb));

        HUNGY.eat(vx2.add(sc));
        HUNGY.eat(p34.add(sc));
        HUNGY.eat(mid.add(sc));
        HUNGY.eat(p32.add(sc));

        HUNGY.eat(vx3.add(sd));
        HUNGY.eat(p34.add(sd));
        HUNGY.eat(mid.add(sd));
        HUNGY.eat(p14.add(sd));
    }

    public static class XYZUV {
        public float x, y, z, u, v;

        public XYZUV(float x, float y, float z, float u, float v) {
            this.x = x; this.y = y; this.z = z; this.u = u; this.v = v;
        }

        public XYZUV(float[] f) {
            this.x = f[0]; this.y = f[1]; this.z = f[2]; this.u = f[3]; this.v = f[4];
        }

        public XYZUV scale(float f) {
            return new XYZUV(x * f, y * f, z * f, u * f, v * f);
        }

        public XYZUV add(Vector3f vec) {
            return new XYZUV(x + vec.x, y + vec.y, z + vec.z, u, v);
        }

        public XYZUV add(XYZUV vec) {
            return new XYZUV(x + vec.x, y + vec.y, z + vec.z, u + vec.u, v + vec.v);
        }
    }

    public interface Muncher {
        void eat(float x, float y, float z, float u, float v);

        default void eat(XYZUV vec) {
            eat(vec.x, vec.y, vec.z, vec.u, vec.v);
        }
    }
}
