package ryan.furry;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Vector3f;

public class Furry implements ModInitializer {
    @Override
    public void onInitialize() {
    }

    /** Splits a quad into quadrants and draws shells */
    public static void dice(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light,
                            int overlay, float red, float green, float blue, float alpha) {

        XYZUV vx0 = verticies[0];
        XYZUV vx1 = verticies[1];
        XYZUV vx2 = verticies[2];
        XYZUV vx3 = verticies[3];

        XYZUV mid = vx0.add(vx1).add(vx2).add(vx3).scale(1 / 4f);

        XYZUV p12 = vx0.add(vx1).scale(1 / 2f);
        XYZUV p14 = vx0.add(vx3).scale(1 / 2f);
        XYZUV p32 = vx2.add(vx1).scale(1 / 2f);
        XYZUV p34 = vx2.add(vx3).scale(1 / 2f);

        Muncher HUNGY = (x, y, z, u, v) -> vertexConsumer
                .vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, transNorm.x, transNorm.y, transNorm.z);

        XYZUVConsumer WUFF = (v) -> new Vector3f(transNorm).mul(v.distanceTo(mid));

        Vector3f sx0 = WUFF.ask(vx0);
        Vector3f s12 = WUFF.ask(p12);
        Vector3f szo = WUFF.ask(mid);
        Vector3f s14 = WUFF.ask(p14);
        Vector3f sx1 = WUFF.ask(vx1);
        Vector3f s32 = WUFF.ask(p32);
        Vector3f sx2 = WUFF.ask(vx2);
        Vector3f s34 = WUFF.ask(p34);
        Vector3f sx3 = WUFF.ask(p34);

        HUNGY.eat(vx0.add(sx0));
        HUNGY.eat(p12.add(s12));
        HUNGY.eat(mid.add(szo));
        HUNGY.eat(p14.add(s14));

        HUNGY.eat(vx1.add(sx1));
        HUNGY.eat(p12.add(s12));
        HUNGY.eat(mid.add(szo));
        HUNGY.eat(p32.add(s32));

        HUNGY.eat(vx2.add(sx2));
        HUNGY.eat(p34.add(s34));
        HUNGY.eat(mid.add(szo));
        HUNGY.eat(p32.add(s32));

        HUNGY.eat(vx3.add(sx3));
        HUNGY.eat(p34.add(s34));
        HUNGY.eat(mid.add(szo));
        HUNGY.eat(p14.add(s14));
    }

    /** Isolates the *top-right(left) corner of the quad */
    public static XYZUV[] brunoise(XYZUV[] corners) {
        return knife(corners, 0, 0, 1, 0, 3);
    }

    /** Isolates the *top-left(right) corner of the quad */
    public static XYZUV[] mince(XYZUV[] corners) {
        return knife(corners, 1, 0, 1, 2, 1);
    }

    /** Isolates the *bottom-left(right) corner of the quad */
    public static XYZUV[] cube(XYZUV[] corners) {
        return knife(corners, 2, 2, 3, 2, 1);
    }

    /** Isolates the *bottom-right(left) corner of the quad */
    public static XYZUV[] batonnet(XYZUV[] corners) {
        return knife(corners, 3, 2, 3, 0, 3);
    }

    public static XYZUV[] knife(XYZUV[] corners, int i10, int i20, int i21, int i40, int i41) {
        return new XYZUV[]{
                corners[i10],
                corners[i20].add(corners[i21]).scale(1 / 2f),
                corners[0].add(corners[1]).add(corners[2]).add(corners[3]).scale(1 / 4f),
                corners[i40].add(corners[i41]).scale(1 / 2f)};
    }

    public static XYZUV[][] julienne(XYZUV[] corners) {
        return new XYZUV[][]{brunoise(corners), mince(corners), cube(corners), batonnet(corners)};
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

        public float distanceTo(XYZUV target) {
            float dx = target.x - x;
            float dy = target.y - y;
            float dz = target.z - z;
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 4 / 3;
        }
    }

    public interface Muncher {
        void eat(float x, float y, float z, float u, float v);

        default void eat(XYZUV vec) {
            eat(vec.x, vec.y, vec.z, vec.u, vec.v);
        }
    }

    interface XYZUVConsumer { Vector3f ask(XYZUV v); }
}
