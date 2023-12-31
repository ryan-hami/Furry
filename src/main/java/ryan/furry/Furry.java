package ryan.furry;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Vector3f;

public class Furry implements ModInitializer {
    private static final double LN_10 = Math.log(10);

    public static int furryState = 0;

    // number of columns of shells per quad
    private static int n = 10;

    // number of rows of shells per quad
    private static int m = 2;

    // number of layers
    private static int l = 6;

    // distance between layers
    private static double h = 1 / 50.0;

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("furryCycle").executes(context -> {
                furryState = (furryState + 1) % 3;
                return 1;
            }));

            dispatcher.register(ClientCommandManager.literal("furry_shell_columns").then(ClientCommandManager.argument("columns", IntegerArgumentType.integer(0)).executes(context -> {
                n = context.getArgument("columns", int.class);
                return 1;
            })));

            dispatcher.register(ClientCommandManager.literal("furry_shell_rows").then(ClientCommandManager.argument("rows", IntegerArgumentType.integer(0)).executes(context -> {
                m = context.getArgument("rows", int.class);
                return 1;
            })));

            dispatcher.register(ClientCommandManager.literal("furry_shell_layers").then(ClientCommandManager.argument("layers", IntegerArgumentType.integer(0)).executes(context -> {
                l = context.getArgument("layers", int.class);
                return 1;
            })));

            dispatcher.register(ClientCommandManager.literal("furry_shell_distance_between_layers").then(ClientCommandManager.argument("height", DoubleArgumentType.doubleArg(0)).executes(context -> {
                h = context.getArgument("height", double.class);
                return 1;
            })));
        });
    }

    public static void furry(XYZUV[] verticies, Vector3f transNorm, VertexConsumer vertexConsumer, int light,
                             int overlay, float red, float green, float blue, float alpha) {

        Muncher HUNGY = (x, y, z, u, v) -> vertexConsumer
                .vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, transNorm.x, transNorm.y, transNorm.z);

        XYZUV normal = new XYZUV(transNorm.x, transNorm.y, transNorm.z, 0, 0);

        XYZUV a = verticies[0];
        XYZUV b = verticies[1];
        XYZUV c = verticies[2];
        XYZUV d = verticies[3];

        switch (furryState) {
            case 0 -> shell(HUNGY, a, b, c, d, normal);
            case 1 -> {
                for (XYZUV[] v1 : Furry.julienne(verticies)) for (XYZUV[] v2 : Furry.julienne(v1))
                    dice(HUNGY, v2[0], v2[1], v2[2], v2[3], normal);
            }
            case 2 -> spike(HUNGY, a, b, c, d, normal);
        }
    }

    private static void nom(Muncher consumer, XYZUV vertex, XYZUV offsetAlongNormal) {
        consumer.eat(vertex.add(offsetAlongNormal));
    }

    private static XYZUV mid(XYZUV a, XYZUV b) {
        return a.add(b).mul(0.5);
    }

    private static XYZUV mid(XYZUV a, XYZUV b, XYZUV c, XYZUV d) {
        return a.add(b).add(c).add(d).mul(0.25);
    }

    /** parametric traversal from one point to another. */
    private static XYZUV p(XYZUV start, XYZUV delta, double t) {
        return start.add(delta.mul(t));
    }

    /** actual implementation of shell texturing (the whole point of the challenge) */
    public static void shell(Muncher HUNGY, XYZUV a, XYZUV b, XYZUV c, XYZUV d, XYZUV normal) {
        // https://www.desmos.com/3d/efd1f03ccd

        XYZUV mid = mid(a, b, c, d);

        double g = LN_10 / a.sqDistanceTo(mid);

        XYZUV dba = b.sub(a);
        XYZUV dcd = c.sub(d);

        double dn = 1.0 / n;
        double dm = 1.0 / m;
        for (int i = 0; i < n; ++i) {
            double t00 = i * dn;
            double t01 = t00 + dn;

            XYZUV x00 = p(a, dba, t00);
            XYZUV x01 = p(a, dba, t01);
            XYZUV x10 = p(d, dcd, t00);
            XYZUV x11 = p(d, dcd, t01);

            XYZUV dx10x00 = x10.sub(x00);
            XYZUV dx11x01 = x11.sub(x01);

            for (int j = 0; j < m; ++j) {
                double t10 = (double) j / m;
                double t11 = t10 + dm;

                XYZUV v00 = p(x00, dx10x00, t10);
                XYZUV v01 = p(x00, dx10x00, t11);
                XYZUV v10 = p(x01, dx11x01, t10);
                XYZUV v11 = p(x01, dx11x01, t11);

                XYZUV m = mid(v00, v01, v10, v11);

                XYZUV dmv00 = m.sub(v00);
                XYZUV dmv01 = m.sub(v01);
                XYZUV dmv10 = m.sub(v10);
                XYZUV dmv11 = m.sub(v11);

                double r = m.sqDistanceTo(mid);

                for (int k = 0; k < l; ++k) {
                    double height = (k + 1) * h * Math.exp(-r * g);
                    XYZUV on = normal.mul(height - h);
                    XYZUV op = normal.mul(height + h);

                    double t = (double) k / l / ((double) l / 2);

                    XYZUV s00 = p(v00, dmv00, t);
                    XYZUV s01 = p(v01, dmv01, t);
                    XYZUV s10 = p(v10, dmv10, t);
                    XYZUV s11 = p(v11, dmv11, t);

                    // vertices must be consumed in the order a b d c because of quad
                    nom(HUNGY, s00, op);
                    nom(HUNGY, s01, on);
                    nom(HUNGY, s11, op);
                    nom(HUNGY, s10, on);
                }
            }
        }
    }

    /** makes a spike */
    public static void spike(Muncher HUNGY, XYZUV a, XYZUV b, XYZUV c, XYZUV d, XYZUV normal) {
        XYZUV m = mid(a, b, c, d).add(normal.mul(0.25f));

        HUNGY.eat(a);
        HUNGY.eat(b);
        HUNGY.eat(m);
        HUNGY.eat(a);

        HUNGY.eat(b);
        HUNGY.eat(c);
        HUNGY.eat(m);
        HUNGY.eat(b);

        HUNGY.eat(c);
        HUNGY.eat(d);
        HUNGY.eat(m);
        HUNGY.eat(c);

        HUNGY.eat(d);
        HUNGY.eat(a);
        HUNGY.eat(m);
        HUNGY.eat(d);
    }

    /** Splits a quad into quadrants and draws shells */
    public static void dice(Muncher HUNGY, XYZUV vx0, XYZUV vx1, XYZUV vx2, XYZUV vx3, XYZUV normal) {
        XYZUV mid = mid(vx0, vx1, vx2, vx3);

        XYZUV p12 = mid(vx0, vx1);
        XYZUV p14 = mid(vx0, vx3);
        XYZUV p32 = mid(vx2, vx1);
        XYZUV p34 = mid(vx2, vx3);

        XYZUVConsumer WUFF = v -> normal.mul(v.distanceTo(mid) * 4 / 3);

        XYZUV sx0 = WUFF.gib(vx0);
        XYZUV s12 = WUFF.gib(p12);
        XYZUV szo = WUFF.gib(mid); // is always zeros
        XYZUV s14 = WUFF.gib(p14);
        XYZUV sx1 = WUFF.gib(vx1);
        XYZUV s32 = WUFF.gib(p32);
        XYZUV sx2 = WUFF.gib(vx2);
        XYZUV s34 = WUFF.gib(p34);
        XYZUV sx3 = WUFF.gib(vx3);

        nom(HUNGY, vx0, sx0);
        nom(HUNGY, p12, s12);
        nom(HUNGY, mid, szo);
        nom(HUNGY, p14, s14);

        nom(HUNGY, vx1, sx1);
        nom(HUNGY, p12, s12);
        nom(HUNGY, mid, szo);
        nom(HUNGY, p32, s32);

        nom(HUNGY, vx2, sx2);
        nom(HUNGY, p34, s34);
        nom(HUNGY, mid, szo);
        nom(HUNGY, p32, s32);

        nom(HUNGY, vx3, sx3);
        nom(HUNGY, p34, s34);
        nom(HUNGY, mid, szo);
        nom(HUNGY, p14, s14);
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

    public static XYZUV[] knife(XYZUV[] a, int i10, int i20, int i21, int i40, int i41) {
        return new XYZUV[]{
                a[i10],
                a[i20].add(a[i21]).mul(0.5),
                a[0].add(a[1]).add(a[2]).add(a[3]).mul(0.25),
                a[i40].add(a[i41]).mul(0.5)};
    }

    public static XYZUV[][] julienne(XYZUV[] corners) {
        return new XYZUV[][]{ brunoise(corners), mince(corners), cube(corners), batonnet(corners) };
    }

    public static class XYZUV {
        public float x, y, z, u, v;

        public XYZUV(float x, float y, float z, float u, float v) {
            this.x = x; this.y = y; this.z = z; this.u = u; this.v = v;
        }

        public XYZUV mul(double d) {
            return mul((float) d);
        }

        public XYZUV mul(float f) {
            return new XYZUV(x * f, y * f, z * f, u * f, v * f);
        }

        public XYZUV add(XYZUV vec) {
            return new XYZUV(x + vec.x, y + vec.y, z + vec.z, u + vec.u, v + vec.v);
        }

        public XYZUV sub(XYZUV vec) {
            return new XYZUV(x - vec.x, y - vec.y, z - vec.z, u - vec.u, v - vec.v);
        }

        public double distanceTo(XYZUV v) {
            float dx = v.x - x;
            float dy = v.y - y;
            float dz = v.z - z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public double sqDistanceTo(XYZUV v) {
            float dx = v.x - x;
            float dy = v.y - y;
            float dz = v.z - z;
            return dx * dx + dy * dy + dz * dz;
        }
    }

    public interface Muncher {
        void eat(float x, float y, float z, float u, float v);

        default void eat(XYZUV vec) {
            eat(vec.x, vec.y, vec.z, vec.u, vec.v);
        }
    }

    interface XYZUVConsumer { XYZUV gib(XYZUV v); }
}
