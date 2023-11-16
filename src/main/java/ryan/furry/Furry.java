package ryan.furry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Vector3f;

public class Furry implements ModInitializer {
    private static final float minScalar = 1 / 32f;
    private static final float maxScalar = 1 / 8f;
    private static final float s1;
    private static final float s2;
    private static final float s3;
    private static final float s4;

    @Override
    public void onInitialize() {
    }

    /** Splits a quad into quadrants and draws a shell */
    public static void dice(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light, int overlay,
                            float red, float green, float blue, float alpha) {

        // TODO: animate scalar wrt time or ticks
        Vector3f sa = new Vector3f(transNorm).mul(s1);
        Vector3f sb = new Vector3f(transNorm).mul(s2);
        Vector3f sc = new Vector3f(transNorm).mul(s3);
        Vector3f sd = new Vector3f(transNorm).mul(s4);

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

    /** Isolates the *top-right(left) corner of the quad */
    public static XYZUV[] brunoise(XYZUV[] corners) {
        return new XYZUV[]{
                corners[0],
                corners[0].add(corners[1]).scale(1 / 2f),
                corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(1 / 4f),
                corners[0].add(corners[3]).scale(1 / 2f)};
    }

    /** Isolates the *top-left(right) corner of the quad */
    public static XYZUV[] mince(XYZUV[] corners) {
        return new XYZUV[]{
                corners[1],
                corners[0].add(corners[1]).scale(1 / 2f),
                corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(1 / 4f),
                corners[2].add(corners[1]).scale(1 / 2f)};
    }

    /** Isolates the *bottom-left(right) corner of the quad */
    public static XYZUV[] cube(XYZUV[] corners) {
        return new XYZUV[]{
                corners[2],
                corners[2].add(corners[3]).scale(1 / 2f),
                corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(1 / 4f),
                corners[2].add(corners[1]).scale(1 / 2f)};
    }

    /** Isolates the *bottom-right(left) corner of the quad */
    public static XYZUV[] batonnet(XYZUV[] corners) {
        return new XYZUV[]{
                corners[3],
                corners[2].add(corners[3]).scale(1 / 2f),
                corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(1 / 4f),
                corners[0].add(corners[3]).scale(1 / 2f)};
    }

    static {
        s1 = (float) ((maxScalar - minScalar) * Math.random() + minScalar);
        s2 = (float) ((maxScalar - minScalar) * Math.random() + minScalar);
        s3 = (float) ((maxScalar - minScalar) * Math.random() + minScalar);
        s4 = (float) ((maxScalar - minScalar) * Math.random() + minScalar);
    }

    public static class XYZUV {
        public float x, y, z, u, v;

        public XYZUV(float x, float y, float z, float u, float v) {
            this.x = x; this.y = y; this.z = z; this.u = u; this.v = v;
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
