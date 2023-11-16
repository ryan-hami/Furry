package ryan.furry.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ryan.furry.Plushie;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
@Mixin({EntityRenderer.class})
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "render", at = @At("HEAD"))
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {

        //if (entity instanceof LivingEntity) {
        //    Plushie<T> monster = new Plushie<>(entity, yaw, tickDelta, matrices, vertexConsumers, light,
        //            entity.getType().getUntranslatedName());

        //    monster.cakeup();
        //}
    }

    @Mixin({Dilation.class})
    public interface IDilationMixin {
        @Accessor float getRadiusX();
        @Accessor float getRadiusY();
        @Accessor float getRadiusZ();
    }

    @Mixin({ModelCuboidData.class})
    public interface IModelCuboidDataMixin {
        @Accessor String getName();
        @Accessor Vector3f getOffset();
        @Accessor Vector3f getDimensions();
        @Accessor Dilation getExtraSize();
        @Accessor boolean getMirror();
        @Accessor Vector2f getTextureUV();
        @Accessor Vector2f getTextureScale();
        @Accessor Set<Direction> getDirections();
    }

    @Mixin({ModelPartData.class})
    public interface IModelPartDataMixin {
        @Accessor List<IModelCuboidDataMixin> getCuboidData();
        @Accessor ModelTransform getRotationData();
        @Accessor Map<String, IModelPartDataMixin> getChildren();
    }

    @Mixin({ModelTransform.class})
    public interface IModelTransformMixin {
        @Accessor float getPivotX();
        @Accessor float getPivotY();
        @Accessor float getPivotZ();
        @Accessor float getPitch();
        @Accessor float getYaw();
        @Accessor float getRoll();
    }

    @Mixin({TextureDimensions.class})
    public interface ITextureDimensionsMixin {
        @Accessor int getWidth();
        @Accessor int getHeight();
    }

    @Mixin({TexturedModelData.class})
    public interface ITexturedModelDataMixin {
        @Accessor ModelData getData();
        @Accessor TextureDimensions getDimensions();
    }
}
