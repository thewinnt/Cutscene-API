package net.thewinnt.cutscenes.entity;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.client.ClientCutsceneManager;

public class CutsceneCameraEntity extends LocalPlayer {
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final ClientPacketListener NETWORK_HANDLER = new ClientPacketListener(
        MINECRAFT,
        MINECRAFT.screen,
        MINECRAFT.getConnection().getConnection(),
        MINECRAFT.getCurrentServer(),
        new GameProfile(UUID.randomUUID(), "CutsceneAPI$Camera"),
        MINECRAFT.getTelemetryManager().createWorldSessionManager(false, null, null)
    ) {
        public void send(Packet<?> pPacket) {}
    };

    private final CutsceneType cutscene;
    private final long startTick;
    private final Vec3 startPos;
    private final float camStartYaw;
    private final float camStartPitch;
    private final float pathYaw;
    private final float pathPitch;
    private final float pathRoll;

    public CutsceneCameraEntity(int id, CutsceneType cutscene, Vec3 startPos, float camStartYaw, float camStartPitch, float pathYaw, float pathPitch, float pathRoll) {
        super(MINECRAFT, MINECRAFT.level, NETWORK_HANDLER, MINECRAFT.player.getStats(), MINECRAFT.player.getRecipeBook(), false, false);
        this.setId(id);
        super.setPose(Pose.SWIMMING);
        LocalPlayer mcplayer = MINECRAFT.player;
        this.moveTo(mcplayer.getX(), mcplayer.getY(), mcplayer.getZ(), mcplayer.getYRot(), mcplayer.getXRot());
        this.xBob = getXRot();
        this.yBob = getYRot();
        this.xBobO = xBob;
        this.yBobO = yBob;
        this.getAbilities().flying = true;
        this.input = new Input();
        this.cutscene = cutscene;
        this.startTick = MINECRAFT.level.getGameTime();
        this.startPos = startPos;
        this.noPhysics = true;
        this.camStartYaw = camStartYaw;
        this.camStartPitch = camStartPitch;
        this.pathYaw = pathYaw;
        this.pathPitch = pathPitch;
        this.pathRoll = pathRoll;
    }

    public void spawn() {
        if (clientLevel != null) {
            clientLevel.putNonPlayerEntity(getId(), this);
        }
    }

    public void despawn() {
        if (clientLevel != null && clientLevel.getEntity(getId()) != null) {
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
        double progress = (clientLevel.getGameTime() - startTick) / (double)cutscene.length;
        if (progress <= 1) {
            Vec3 position = cutscene.getPathPoint(progress, clientLevel, startPos);
            float yRot = (float)Math.toRadians(pathYaw);
            float zRot = (float)Math.toRadians(pathPitch);
            float xRot = (float)Math.toRadians(pathRoll);
            this.setPos(position.yRot(yRot).zRot(zRot).xRot(xRot).add(startPos));
            Vec3 rotation = cutscene.getRotationAt(progress, clientLevel, startPos);
            this.setXRot((float)(rotation.y + camStartPitch));
            this.setYRot((float)(rotation.x + camStartYaw));
            // TODO transitions
        } else {
            ClientCutsceneManager.stopCutsceneImmediate();
        }
        super.aiStep();
        getAbilities().flying = true;
        setOnGround(false);
    }

    public Vec3 getProperPosition(float partialTick) {
        double progress = (clientLevel.getGameTime() - startTick + partialTick) / (double)cutscene.length;
        Vec3 position = cutscene.getPathPoint(progress, clientLevel, startPos);
        float yRot = (float)Math.toRadians(pathYaw);
        float zRot = (float)Math.toRadians(pathPitch);
        float xRot = (float)Math.toRadians(pathRoll);
        return position.yRot(yRot).zRot(zRot).xRot(xRot).add(startPos);
    }

    @Override
    public boolean isSpectator() {
        return true;
    }

    @Override
    public float getViewXRot(float pPartialTick) {
        double progress = (clientLevel.getGameTime() - startTick + pPartialTick) / (double)cutscene.length;
        return (float)cutscene.getRotationAt(progress, clientLevel, startPos).y + camStartPitch;
    }

    @Override
    public float getViewYRot(float pPartialTick) {
        double progress = (clientLevel.getGameTime() - startTick + pPartialTick) / (double)cutscene.length;
        return (float)cutscene.getRotationAt(progress, clientLevel, startPos).x + camStartYaw;
    }
}
