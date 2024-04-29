package net.thewinnt.cutscenes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.effect.CutsceneEffect;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition;
import net.thewinnt.cutscenes.util.ActionToggles;
import net.thewinnt.cutscenes.util.ActionToggles.Builder;
import net.thewinnt.cutscenes.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CutsceneType {
    public final int length;
    public final @Nullable Path path;
    public final @Nullable Path rotationProvider;
    public final Transition startTransition;
    public final Transition endTransition;
    public final boolean blockMovement;
    public final boolean blockCameraRotation;
    public final ActionToggles actionToggles;
    public final boolean hideHand;
    public final boolean hideBlockOutline;
    public final List<CutsceneEffect<?>> effects;

    public CutsceneType(PathLike path, Path rotationProvider, int length, Transition start, Transition end, boolean blockMovement, boolean blockCameraRotation, ActionToggles toggles, boolean hideHand, boolean hideBlockOutline, List<CutsceneEffect<?>> effects) {
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
        this.actionToggles = toggles;
        this.hideHand = hideHand;
        this.hideBlockOutline = hideBlockOutline;
        this.effects = effects;
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
        this.actionToggles = new Builder(true).build();
        this.hideHand = false;
        this.hideBlockOutline = false;
        this.effects = List.of();
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
        actionToggles.toNetwork(buf);
        buf.writeBoolean(hideHand);
        buf.writeBoolean(hideBlockOutline);
        buf.writeCollection(effects, (buf1, effect) -> effect.toNetwork(buf1));
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
        ActionToggles actionToggles = ActionToggles.fromNetwork(buf);
        boolean hideHand = buf.readBoolean();
        boolean hideBlockOutline = buf.readBoolean();
        List<CutsceneEffect<?>> effects = buf.readList(CutsceneEffect::fromNetwork);
        return new CutsceneType(path, rotationProvider, length, start, end, blockMovement, blockCameraRotation, actionToggles, hideHand, hideBlockOutline, effects);
    }

    public static CutsceneType fromJSON(JsonObject json) {
        int length = json.get("length").getAsInt();
        Path path = Path.fromJSON(JsonHelper.getNullableObject(json, "path"), null);
        Path rotation = Path.fromJSON(JsonHelper.getNullableObject(json, "rotation"), path);
        Transition start = Transition.fromJSON(JsonHelper.getNullableObject(json, "start_transition"), new SmoothEaseTransition(40, true, true));
        Transition end = Transition.fromJSON(JsonHelper.getNullableObject(json, "end_transition"), new SmoothEaseTransition(40, false, false));
        boolean blockMovement = GsonHelper.getAsBoolean(json, "block_movement", false) || path != null;
        boolean blockRotation = GsonHelper.getAsBoolean(json, "block_rotation", false) || rotation != null;
        ActionToggles toggles;
        if (json.has("disable_actions")) {
            toggles = ActionToggles.fromJson(json.get("disable_actions"));
        } else {
            toggles = new ActionToggles.Builder(true).build();
        }
        boolean defaultHideBlockOutline = toggles.disableBreakingBlocks() && toggles.disableBlockInteractions() && toggles.disablePickingBlocks();
        boolean defaultHideHand = defaultHideBlockOutline && toggles.disableAttacking() && toggles.disableUsingItems() && toggles.disableEntityInteractions();
        boolean hideHand = GsonHelper.getAsBoolean(json, "hide_hand", defaultHideHand);
        boolean hideBlockOutline = GsonHelper.getAsBoolean(json, "hide_block_outline", defaultHideBlockOutline);
        JsonArray effectsJson = GsonHelper.getAsJsonArray(json, "effects", new JsonArray());
        ArrayList<CutsceneEffect<?>> effects = new ArrayList<>();
        for (JsonElement i : effectsJson) {
            effects.add(CutsceneEffect.fromJSON(GsonHelper.convertToJsonObject(i, "effect")));
        }
        return new CutsceneType(path, rotation, length, start, end, blockMovement, blockRotation, toggles, hideHand, hideBlockOutline, effects);
    }
}
