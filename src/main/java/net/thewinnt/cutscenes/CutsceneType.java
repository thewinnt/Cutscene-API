package net.thewinnt.cutscenes;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition;
import net.thewinnt.cutscenes.util.JsonHelper;

import javax.annotation.Nullable;

public class CutsceneType {
    public final int length;
    public final @Nullable Path path;
    public final @Nullable Path rotationProvider;
    public final Transition startTransition;
    public final Transition endTransition;
    public final boolean blockMovement;
    public final boolean blockCameraRotation;

    public CutsceneType(PathLike path, Path rotationProvider, int length, Transition start, Transition end, boolean blockMovement, boolean blockCameraRotation) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else if (path != null) {
            this.path = new Path(path);
        } else {
            this.path = null;
        }
        this.rotationProvider = rotationProvider;
        this.length = length;
        this.startTransition = start;
        this.endTransition = end;
        this.blockMovement = path != null || blockMovement; // if there's a path, you can't block movement
        this.blockCameraRotation = rotationProvider != null || blockCameraRotation; // same for rotation
    }
    
    public CutsceneType(PathLike path, Path rotationProvider, int length) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else if (path != null) {
            this.path = new Path(path);
        } else {
            this.path = null;
        }
        this.rotationProvider = rotationProvider;
        this.length = length;
        this.startTransition = new SmoothEaseTransition(40, true, true);
        this.endTransition = new SmoothEaseTransition(40, false, false);
        this.blockMovement = true;
        this.blockCameraRotation = true;
    }

    @Nullable
    public Vec3 getPathPoint(double point, Level level, Vec3 cutsceneStart) {
        if (path == null) return null;
        return path.getPoint(point, level, cutsceneStart);
    }

    @Nullable
    public Vec3 getRotationAt(double point, Level level, Vec3 cutsceneStart) {
        if (rotationProvider == null) return null;
        return rotationProvider.getPoint(point, level, cutsceneStart);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(length);
        buf.writeBoolean(path == null);
        if (path != null) path.toNetwork(buf);
        buf.writeBoolean(rotationProvider == null);
        if (rotationProvider != null) rotationProvider.toNetwork(buf);
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(startTransition.getSerializer()));
        startTransition.toNetwork(buf);
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(endTransition.getSerializer()));
        endTransition.toNetwork(buf);
        buf.writeBoolean(blockMovement);
        buf.writeBoolean(blockCameraRotation);
    }

    public static CutsceneType fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readInt();
        Path path, rotationProvider;
        if (!buf.readBoolean()) {
            path = Path.fromNetwork(buf, null);
        } else {
            path = null;
        }
        if (!buf.readBoolean()) {
            rotationProvider = Path.fromNetwork(buf, path);
        } else {
            rotationProvider = null;
        }
        Transition start = Transition.fromNetwork(buf);
        Transition end = Transition.fromNetwork(buf);
        boolean blockMovement = buf.readBoolean();
        boolean blockCameraRotation = buf.readBoolean();
        return new CutsceneType(path, rotationProvider, length, start, end, blockMovement, blockCameraRotation);
    }

    public static CutsceneType fromJSON(JsonObject json) {
        int length = json.get("length").getAsInt();
        Path path = Path.fromJSON(JsonHelper.getNullableObject(json, "path"), null);
        Path rotation = Path.fromJSON(JsonHelper.getNullableObject(json, "rotation"), path);
        Transition start = Transition.fromJSON(JsonHelper.getNullableObject(json, "start_transition"), new SmoothEaseTransition(40, true, true));
        Transition end = Transition.fromJSON(JsonHelper.getNullableObject(json, "end_transition"), new SmoothEaseTransition(40, false, false));
        boolean blockMovement = GsonHelper.getAsBoolean(json, "block_movement", false) || path != null;
        boolean blockRotation = GsonHelper.getAsBoolean(json, "block_rotation", false) || rotation != null;
        return new CutsceneType(path, rotation, length, start, end, blockMovement, blockRotation);
    }
}
