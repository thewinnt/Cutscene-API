package net.thewinnt.cutscenes.entity;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneInstance;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;
import net.thewinnt.cutscenes.transition.Transition;

public class CutsceneCameraEntity extends LocalPlayer {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final ClientPacketListener NETWORK_HANDLER = new ClientPacketListener(
        MINECRAFT,
        MINECRAFT.getConnection().getConnection(),
        new CommonListenerCookie(
            new GameProfile(UUID.randomUUID(), "CutsceneAPI$Camera"),
            MINECRAFT.getTelemetryManager().createWorldSessionManager(false, null, null),
            RegistryAccess.Frozen.EMPTY,
            FeatureFlagSet.of(),
            null,
            MINECRAFT.getCurrentServer(),
            MINECRAFT.screen
        )
    ) {
        public void send(Packet<?> pPacket) {}
    };

    private final CutsceneInstance cutscene;
    private final Vec3 startPos;
    private final Vec3 startRot;
    private final Vec3 pathRot;
    private final float camStartYaw;
    private final float camStartPitch;
    private final float pathYaw;
    private final float pathPitch;
    private final float pathRoll;
    private byte cutscenePhase = 0;

    public CutsceneCameraEntity(int id, CutsceneInstance cutscene, Vec3 startPos, float camStartYaw, float camStartPitch, float pathYaw, float pathPitch, float pathRoll) {
        super(MINECRAFT, MINECRAFT.level, NETWORK_HANDLER, MINECRAFT.player.getStats(), MINECRAFT.player.getRecipeBook(), false, false);
        this.setId(id);
        super.setPose(Pose.SWIMMING);
        LocalPlayer mcplayer = MINECRAFT.player;
        this.cutscene = cutscene;
        Vec3 startReal = cutscene.cutscene.getPathPoint(0, MINECRAFT.level, startPos);
        if (startReal != null) {
            this.moveTo(startReal.x, startReal.y, startReal.z, mcplayer.getYRot(), mcplayer.getXRot());
        }
        this.xBob = getXRot();
        this.yBob = getYRot();
        this.xBobO = xBob;
        this.yBobO = yBob;
        this.getAbilities().flying = true;
        this.input = new Input();
        this.startPos = startPos;
        this.noPhysics = true;
        this.camStartYaw = camStartYaw;
        this.camStartPitch = camStartPitch;
        this.startRot = new Vec3(camStartYaw, camStartPitch, 0);
        this.pathYaw = (float)Math.toRadians(pathYaw);
        this.pathPitch = (float)Math.toRadians(pathPitch);
        this.pathRoll = (float)Math.toRadians(pathRoll);
        this.pathRot = new Vec3(this.pathRoll, this.pathYaw, this.pathPitch);
    }

    public void spawn() {
        clientLevel.addEntity(this);
    }

    public void despawn() {
        if (clientLevel.getEntity(getId()) != null) {
            clientLevel.removeEntity(getId(), RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {}

    @Override
    public MobEffectInstance getEffect(MobEffect pEffect) {
        return MINECRAFT.player.getEffect(pEffect);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return false;
    }

    @Override
    public void setPose(Pose pPose) {
        super.setPose(Pose.SWIMMING);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean updateIsUnderwater() {
        this.wasUnderwater = this.isEyeInFluid(FluidTags.WATER);
        return this.wasUnderwater;
    }

    @Override
    protected void doWaterSplashEffect() {}

    @Override
    public void aiStep() {
        this.setPos(getProperPosition(0));
        this.setXRot(getViewXRot(0));
        this.setYRot(getViewYRot(0));
        super.aiStep();
        getAbilities().flying = true;
        setOnGround(false);
    }

    public Vec3 getProperPosition(float partialTick) {
        Vec3 output = getPositionAndTick(partialTick);
        BlockPos pos = BlockPos.containing(output.x, output.y, output.z);
        Level level = level();
        minecraft.smartCull = !level.getBlockState(pos).isSolidRender(level, pos);
        return output;
    }

    private Vec3 getPositionAndTick(float partialTick) {
        if (cutscene.isTimeForStart()) {
            Transition transition = cutscene.cutscene.startTransition;
            double progress = cutscene.getTime() / transition.getLength();
            return transition.getPos(progress, clientLevel, startPos, pathRot, minecraft.player.getEyePosition(partialTick), cutscene.cutscene);
        } else if (cutscene.isTimeForEnd()) {
            Transition transition = cutscene.cutscene.endTransition;
            double progress = cutscene.getEndProress();
            return transition.getPos(progress, clientLevel, startPos, pathRot, minecraft.player.getEyePosition(partialTick), cutscene.cutscene);
        }
        if (!cutscene.cutscene.blockMovement) return minecraft.player.getPosition(partialTick);
        if (cutscene.cutscene.path == null) return startPos;

        double progress = (cutscene.getTime() - cutscene.cutscene.startTransition.getOffCutsceneTime()) / (double)cutscene.cutscene.length;
        Vec3 position = cutscene.cutscene.getPathPoint(progress, clientLevel, startPos);
        return position.yRot(pathYaw).zRot(pathPitch).xRot(pathRoll).add(startPos);
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    /** Minecraft's X rotation = CutsceneAPI's Y rotation = pitch */
    @Override
    public float getViewXRot(float partialTick) {
        if (cutscene.isTimeForStart()) {
            double progress = cutscene.getTime() / cutscene.cutscene.startTransition.getLength();
            return (float)cutscene.cutscene.startTransition.getRot(progress, clientLevel, startPos, startRot, getPlayerCamRot(), cutscene.cutscene).y;
        } else if (cutscene.isTimeForEnd()) {
            double progress = cutscene.getEndProress();
            return (float)cutscene.cutscene.endTransition.getRot(progress, clientLevel, startPos, startRot, getPlayerCamRot(), cutscene.cutscene).y;
        }

        if (!cutscene.cutscene.blockCameraRotation) return minecraft.player.getViewXRot(partialTick);
        if (cutscene.cutscene.rotationProvider == null) return camStartPitch;

        double progress = (cutscene.getTime() - cutscene.cutscene.startTransition.getOffCutsceneTime()) / (double)cutscene.cutscene.length;
        return (float)cutscene.cutscene.getRotationAt(progress, clientLevel, startPos).y + camStartPitch;
    }

    /** Minecraft's Y rotation = CutsceneAPI's X rotation = yaw */
    @Override
    public float getViewYRot(float partialTick) {
        if (cutscene.isTimeForStart()) {
            double progress = cutscene.getTime() / cutscene.cutscene.startTransition.getLength();
            return (float)cutscene.cutscene.startTransition.getRot(progress, clientLevel, startPos, startRot, getPlayerCamRot(), cutscene.cutscene).x;
        } else if (cutscene.isTimeForEnd()) {
            double progress = cutscene.getEndProress();
            return (float)cutscene.cutscene.endTransition.getRot(progress, clientLevel, startPos, startRot, getPlayerCamRot(), cutscene.cutscene).x;
        }

        if (!cutscene.cutscene.blockCameraRotation) return minecraft.player.getViewYRot(partialTick);
        if (cutscene.cutscene.rotationProvider == null) return camStartYaw;

        double progress = (cutscene.getTime() - cutscene.cutscene.startTransition.getOffCutsceneTime()) / (double)cutscene.cutscene.length;
        return (float)cutscene.cutscene.getRotationAt(progress, clientLevel, startPos).x + camStartYaw;
    }

    public Vec3 getPlayerCamRot() {
        return new Vec3(minecraft.player.getYHeadRot(), minecraft.player.getXRot(), 0);
    }

    @Override
    public float getEyeHeight(Pose pPose) {
        return 0;
    }

    @Override
    public double getEyeY() {
        return getY();
    }

    @Override
    public float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return 0;
    }

    @Override
    public boolean hasEffect(MobEffect pEffect) {
        return minecraft.player.hasEffect(pEffect);
    }
}
