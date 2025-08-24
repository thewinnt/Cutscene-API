package net.thewinnt.cutscenes.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WaypointEntity extends Entity {
    public static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(WaypointEntity.class, EntityDataSerializers.STRING);

    public WaypointEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.firstTick = false;
    }

    @Override
    public void tick() {}

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(NAME, "undefined"); // we need the client to know 
    }

    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {
        this.entityData.set(NAME, nbt.getStringOr("Name", "undefined"));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {
        nbt.putString("Name", this.entityData.get(NAME));
    }
    
    public String getWaypointName() {
        return this.entityData.get(NAME);
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }
}
