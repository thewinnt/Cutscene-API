package net.thewinnt.cutscenes;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition;

public class CutsceneType {
    public final int length;
    public final Path path;
    public final Path rotationProvider;
    public final Transition startTransition;
    public final Transition endTransition;

    public CutsceneType(PathLike path, Path rotationProvider, int length, Transition start, Transition end) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else {
            this.path = new Path(path);
        }
        this.rotationProvider = rotationProvider;
        this.length = length;
        this.startTransition = start;
        this.endTransition = end;
    }
    
    public CutsceneType(PathLike path, Path rotationProvider, int length) {
        if (path instanceof Path pth) {
            this.path = pth;
        } else {
            this.path = new Path(path);
        }
        this.rotationProvider = rotationProvider;
        this.length = length;
        this.startTransition = new SmoothEaseTransition(40, true, true);
        this.endTransition = new SmoothEaseTransition(40, false, false);
    }

    public Vec3 getPathPoint(double point, Level level, Vec3 cutsceneStart) {
        return path.getPoint(point, level, cutsceneStart);
    }

    public Vec3 getRotationAt(double point, Level level, Vec3 cutsceneStart) {
        return rotationProvider.getPoint(point, level, cutsceneStart);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeInt(length);
        path.toNetwork(buf);
        rotationProvider.toNetwork(buf);
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(startTransition.getSerializer()));
        startTransition.toNetwork(buf);
        buf.writeResourceLocation(CutsceneManager.getTransitionTypeId(endTransition.getSerializer()));
        endTransition.toNetwork(buf);
    }

    public static CutsceneType fromNetwork(FriendlyByteBuf buf) {
        int length = buf.readInt();
        Path path = Path.fromNetwork(buf, null);
        Path rotationProvider = Path.fromNetwork(buf, path);
        Transition start = Transition.fromNetwork(buf);
        Transition end = Transition.fromNetwork(buf);
        return new CutsceneType(path, rotationProvider, length, start, end);
    }

    public static CutsceneType fromJSON(JsonObject json) {
        int length = json.get("length").getAsInt();
        Path path = Path.fromJSON(json.getAsJsonObject("path"), null);
        Path rotation = Path.fromJSON(json.getAsJsonObject("rotation"), path);
        Transition start, end;
        if (json.has("start_transition")) {
            start = Transition.fromJSON(json.getAsJsonObject("start_transition"));
        } else {
            start = new SmoothEaseTransition(40, true, true);
        }
        if (json.has("end_transition")) {
            end = Transition.fromJSON(json.getAsJsonObject("end_transition"));
        } else {
            end = new SmoothEaseTransition(40, false, false);
        }
        return new CutsceneType(path, rotation, length, start, end);
    }
}
