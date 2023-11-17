package ryan.furry;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Vector3f;

public class Furry implements ModInitializer {
    public static boolean furryState = true;

    // number of rows of shells per quad
    private static final int n = 10;

    // number of columns of shells per quad
    private static final int m = 2;

    // number of layers
    private static final int l = 6;

    // distance between layers
    private static final double h = 1 / 50.0;

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("toggleFurry").executes(this::toggle))));
    }

    private int toggle(CommandContext<FabricClientCommandSource> context) {
        furryState = !furryState;
        return 1;
    }

    /** actual implementation of shell texturing (the whole point of the challenge) */
    public static void shell(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light,
                             int overlay, float red, float green, float blue, float alpha) {

        Muncher HUNGY = (x, y, z, u, v) -> vertexConsumer
                .vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, transNorm.x, transNorm.y, transNorm.z);

        XYZUV a = verticies[0];
        XYZUV b = verticies[1];
        XYZUV d = verticies[2];
        XYZUV c = verticies[3];

        XYZUV dba = b.sub(a);
        XYZUV ddc = d.sub(c);

        // https://www.desmos.com/3d/17f0200211
        double dn = 1.0 / n;
        double dm = 1.0 / m;
        for (int i = 0; i < n; ++i) {
            double t00 = i * dn;
            double t01 = t00 + dn;

            XYZUV x00 = a.add(dba.scale((float) t00));
            XYZUV x01 = a.add(dba.scale((float) t01));
            XYZUV x10 = c.add(ddc.scale((float) t00));
            XYZUV x11 = c.add(ddc.scale((float) t01));

            XYZUV dx10x00 = x10.sub(x00);
            XYZUV dx11x01 = x11.sub(x01);

            for (int j = 0; j < m; ++j) {
                double t10 = (double) j / m;
                double t11 = t10 + dm;

                XYZUV v00 = x00.add(dx10x00.scale((float) t10));
                XYZUV v01 = x00.add(dx10x00.scale((float) t11));
                XYZUV v10 = x01.add(dx11x01.scale((float) t10));
                XYZUV v11 = x01.add(dx11x01.scale((float) t11));

                XYZUV m = v00.add(v01).add(v10).add(v11).scale((float) (1 / 4.0));

                for (int k = 0; k < l; ++k) {
                    Vector3f o = new Vector3f(transNorm).mul((float) (h * (k + 1)));
                    double t = (double) k / l / ((double) l / 2);

                    XYZUV s00 = v00.add(m.sub(v00).scale((float) t));
                    XYZUV s01 = v01.add(m.sub(v01).scale((float) t));
                    XYZUV s10 = v10.add(m.sub(v10).scale((float) t));
                    XYZUV s11 = v11.add(m.sub(v11).scale((float) t));

                    // vertices must be consumed in the order a b d c because of quad
                    HUNGY.eat(s00.add(o));
                    HUNGY.eat(s01.add(o));
                    HUNGY.eat(s11.add(o));
                    HUNGY.eat(s10.add(o));
                }
            }
        }
    }

    /** Splits a quad into quadrants and draws shells */
    public static void dice(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light,
                            int overlay, float red, float green, float blue, float alpha) {
        Muncher HUNGY = (x, y, z, u, v) -> vertexConsumer
                .vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, transNorm.x, transNorm.y, transNorm.z);

        XYZUV vx0 = verticies[0];
        XYZUV vx1 = verticies[1];
        XYZUV vx2 = verticies[2];
        XYZUV vx3 = verticies[3];

        XYZUV mid = vx0.add(vx1).add(vx2).add(vx3).scale(1 / 4f);

        XYZUV p12 = vx0.add(vx1).scale(1 / 2f);
        XYZUV p14 = vx0.add(vx3).scale(1 / 2f);
        XYZUV p32 = vx2.add(vx1).scale(1 / 2f);
        XYZUV p34 = vx2.add(vx3).scale(1 / 2f);

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

        public XYZUV sub(XYZUV vec) {
            return new XYZUV(x - vec.x, y - vec.y, z - vec.z, u - vec.u, v - vec.v);
        }

        public float distanceTo(XYZUV v) {
            float dx = v.x - x;
            float dy = v.y - y;
            float dz = v.z - z;
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
