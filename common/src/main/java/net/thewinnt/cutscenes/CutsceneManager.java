package net.thewinnt.cutscenes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StopCutscenePacket;
import net.thewinnt.cutscenes.path.BezierCurve;
import net.thewinnt.cutscenes.path.CalculatedPoint;
import net.thewinnt.cutscenes.path.CatmullRomSpline;
import net.thewinnt.cutscenes.path.ConstantPoint;
import net.thewinnt.cutscenes.path.LineSegment;
import net.thewinnt.cutscenes.path.LookAtPoint;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike.SegmentType;
import net.thewinnt.cutscenes.path.PathTransition;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.path.point.WaypointProvider;
import net.thewinnt.cutscenes.path.point.WorldPointProvider;
import net.thewinnt.cutscenes.platform.PlatformAbstractions;
import net.thewinnt.cutscenes.transition.FadeToColorTransition;
import net.thewinnt.cutscenes.transition.NoopTransition;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition.TransitionSerializer;
import net.thewinnt.cutscenes.util.PlayerExt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class CutsceneManager {
    /** The cutscene registry, where all cutscenes are stored. Only read from this, please */
    public static final BiMap<Identifier, CutsceneType> REGISTRY = HashBiMap.create();

    /** The currently previewed cutscene */
    private static CutsceneType previewedCutscene;

    /** The start position of the preview */
    public static Vec3 previewOffset;

    /** The x rotation of the preview path */
    public static float previewPathYaw;

    /** The y rotation of the preview path */
    public static float previewPathPitch;

    /** The z rotation of the preview path */
    public static float previewPathRoll;

    // SEGMENT TYPES //
    // Segment serializers are used to identify and read segment types. The writing is performed on instances of
    // segments obtained from these serializers.

    /** A line, consisting of 2 point, interpolated between each other with some easings. */
    public static final SegmentType<LineSegment> LINE = SegmentType.of(LineSegment::fromNetwork, LineSegment::fromJSON);
    /** A cubic or quadratic Bézier curve, depending on the points supplied. */
    public static final SegmentType<BezierCurve> BEZIER = SegmentType.of(BezierCurve::fromNetwork, BezierCurve::fromJSON);
    /** A Catmull-Rom spline, made of 2 or more points. */
    public static final SegmentType<CatmullRomSpline> CATMULL_ROM = SegmentType.of(CatmullRomSpline::fromNetwork, CatmullRomSpline::fromJSON);
    /** A segment made of other segments. */
    public static final SegmentType<Path> PATH = SegmentType.of(Path::fromNetwork, Path::fromJSON);
    /** A segment always returning a single point. */
    public static final SegmentType<ConstantPoint> CONSTANT = SegmentType.of(ConstantPoint::fromNetwork, ConstantPoint::fromJSON);
    /** A segment returning a look direction so that the player is looking at the specified point. */
    public static final SegmentType<LookAtPoint> LOOK_AT_POINT = SegmentType.usingRotationPath(LookAtPoint::fromNetwork, LookAtPoint::fromJSON);
    /** A transition between two segments - the one before and the one after this. */
    public static final SegmentType<PathTransition> PATH_TRANSITION = SegmentType.of(PathTransition::fromNetwork, PathTransition::fromJSON);
    /** A segment getting its coordinates from easings. */
    public static final SegmentType<CalculatedPoint> CALCULATED_POINT = SegmentType.of(CalculatedPoint::fromNetwork, CalculatedPoint::fromJSON);

    // POINT TYPES //
    // Point serializers are used to identify and read point types. A point type gets a Level in and returns
    // a point based on that.

    /** A simple point. Can be inlined as an array of 3 points. */
    public static final PointSerializer<StaticPointProvider> STATIC = PointSerializer.of(StaticPointProvider::fromNetwork, StaticPointProvider::fromJSON);
    /** A point that finds a waypoint entity with a specified name, and returns its position (maybe offset too). */
    public static final PointSerializer<WaypointProvider> WAYPOINT = PointSerializer.of(WaypointProvider::fromNetwork, WaypointProvider::fromJSON);
    /** A point type that is specified in world coordinates */
    public static final PointSerializer<WorldPointProvider> WORLD = PointSerializer.of(WorldPointProvider::fromNetwork, WorldPointProvider::fromJSON);

    // TRANSITION TYPES //
    // Transitions make you enter and leave a cutscene with beauty, instead of simply snapping into it.

    /** Does nothing. */
    public static final TransitionSerializer<NoopTransition> NO_OP = TransitionSerializer.of(NoopTransition::fromNetwork, NoopTransition::fromJSON, NoopTransition.CODEC);
    /** Smoothly transitions your camera from the starting point to the current point you should be at. */
    public static final TransitionSerializer<SmoothEaseTransition> SMOOTH_EASE = TransitionSerializer.of(SmoothEaseTransition::fromNetwork, SmoothEaseTransition::fromJSON, SmoothEaseTransition.CODEC);
    /** Fades the screen to a color (or several colors that may change too) */
    public static final TransitionSerializer<FadeToColorTransition> FADE = TransitionSerializer.of(FadeToColorTransition::fromNetwork, FadeToColorTransition::fromJSON, FadeToColorTransition.CODEC);

    // UTILITY CONSTANTS //
    // Some constants of variable usefulness.

    /** Pass this as a camera rotation to {@link #startCutscene(Identifier, Vec3, Vec3, Vec3, ServerPlayer)} and it will make the start camera rotation equal to the current rotation. */
    public static final Vec3 KEEP_ROTATION = new Vec3(Double.NaN, Double.NaN, Double.NaN);

    /** 
     * Registers a cutscene type
     * @param id The ID of the cutscene type that will be used in commands
     * @param type The actual cutscene type you want to register
     * @return Your cutscene type for storing
     */
    public static CutsceneType registerCutscene(Identifier id, @NotNull CutsceneType type) {
        REGISTRY.put(id, type);
        return type;
    }

    /**
     * Registers a segment serializer
     * @param id The ID of the segment type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerSegmentType(Identifier id, SegmentType<?> type) {
        Registry.register(CutsceneAPI.SEGMENT_TYPES, id, type);
    }

    /**
     * Registers a point serializer
     * @param id The ID of the point type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerPointType(Identifier id, PointSerializer<?> type) {
        Registry.register(CutsceneAPI.POINT_TYPES, id, type);
    }

    /**
     * Registers a transition serializer
     * @param id The ID of the transition type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerTransitionType(Identifier id, TransitionSerializer<?> type) {
        Registry.register(CutsceneAPI.TRANSITION_TYPES, id, type);
    }

    /** Returns the ID of the specified serializer, or {@code null} if it's not registered */
    public static Identifier getSegmentTypeId(SegmentType<?> type) {
        return CutsceneAPI.SEGMENT_TYPES.getKey(type);
    }

    /** Returns the segment serializer with this ID, or {@code null} if it doesn't exist */
    public static SegmentType<?> getSegmentType(Identifier id) {
        return CutsceneAPI.SEGMENT_TYPES.getValue(id);
    }

    /** Returns the ID of the specified point type, or {@code null} if it's not registered */
    public static Identifier getPointTypeId(PointSerializer<?> type) {
        return CutsceneAPI.POINT_TYPES.getKey(type);
    }

    /** Returns the point serializer with this ID, or {@code null} if it doesn't exist */
    @Nullable
    public static PointSerializer<?> getPointType(Identifier id) {
        return CutsceneAPI.POINT_TYPES.getValue(id);
    }

    /** Returns the ID of the specified transition type, or {@code null} if it's not registered */
    @Nullable
    public static Identifier getTransitionTypeId(TransitionSerializer<?> type) {
        return CutsceneAPI.TRANSITION_TYPES.getKey(type);
    }

    /** Returns the transition serializer with this ID, or {@code null} if it doesn't exist */
    @Nullable
    public static TransitionSerializer<?> getTransitionType(Identifier id) {
        return CutsceneAPI.TRANSITION_TYPES.getValue(id);
    }

    /** Sets the currently previewed cutscene and tells the clients */
    public static void setPreviewedCutscene(CutsceneType type, Vec3 offset, float pathYaw, float pathPitch, float pathRoll) {
        previewedCutscene = type;
        previewOffset = offset;
        previewPathYaw = pathYaw;
        previewPathPitch = pathPitch;
        previewPathRoll = pathRoll;
        PlatformAbstractions platform = CutsceneAPI.platform();
        platform.sendPacketToPlayers(new PreviewCutscenePacket(REGISTRY.inverse().get(type), offset, pathYaw, pathPitch, pathRoll), platform.getServer().getPlayerList().getPlayers());
    }

    /** Self-explanatory */
    public static CutsceneType getPreviewedCutscene() {
        return previewedCutscene;
    }

    /** Returns the starting position of the current cutscene preview, or [0, 100, 0] if there isn't one */
    public static Vector3f getOffset() {
        if (previewOffset != null) {
            return previewOffset.toVector3f();
        } else {
            return new Vector3f(0, 100, 0);
        }
    }

    /**
     * Starts a cutscene for a player
     * @param id The ID of the cutscene to start
     * @param startPos The starting position for the cutscene
     * @param camRot The initial camera rotation of the player as a vector of (yaw, pitch, roll)
     * @param pathRot The path rotation as a vector of (yaw, pitch, roll)
     * @param player The player to play the cutscene to
     * @see CutsceneManager#KEEP_ROTATION
     */
    public static void startCutscene(Identifier id, Vec3 startPos, Vec3 camRot, Vec3 pathRot, ServerPlayer player, String startingReason) {
        CutsceneType type = REGISTRY.get(id);
        PlayerExt ext = (PlayerExt) player;
        ext.csapi$finishCutscene(EndingReason.INTERRUPT);
        if (type.length.manager().isServerSynched()) {
            double lengthUnits = type.length.length() + type.startTransition.getOffCutsceneTime() + type.endTransition.getOffCutsceneTime();
            ext.csapi$setCutsceneTicks((int)(lengthUnits * type.length.manager().ticksPerUnit()));
        } else {
            ext.csapi$setCutsceneTicks(Integer.MAX_VALUE);
        }
        ext.csapi$setRunningCutscene(type, startingReason);
        player.setCamera(null);
        CutsceneAPI.platform().sendPacketToPlayer(new StartCutscenePacket(id, startPos, (float)camRot.x, (float)camRot.y, (float)camRot.z, (float)pathRot.x, (float)pathRot.y, (float)pathRot.z), player);
    }

    /**
     * Starts a cutscene for a player
     * @param id The ID of the cutscene to start
     * @param startPos The starting position for the cutscene
     * @param camRot The initial camera rotation of the player as a vector of (yaw, pitch, roll)
     * @param pathRot The path rotation as a vector of (yaw, pitch, roll)
     * @param player The player to play the cutscene to
     * @see CutsceneManager#KEEP_ROTATION
     */
    public static void startCutscene(Identifier id, Vec3 startPos, Vec3 camRot, Vec3 pathRot, ServerPlayer player) {
        startCutscene(id, startPos, camRot, pathRot, player, "unspecified");
    }

    /**
     * Starts a cutscene for a player from their position with no preset rotation and starting reason {@code unspecified}
     * @param id The ID of the cutscene to start
     * @param player The player to play the cutscene to
     * @see CutsceneManager#startCutscene(Identifier, Vec3, Vec3, Vec3, ServerPlayer, String)
     */
    public static void startCutscene(Identifier id, ServerPlayer player) {
        startCutscene(id, player.position(), Vec3.ZERO, Vec3.ZERO, player);
    }

    /**
     * Starts a cutscene for a player from their position with no preset rotation and starting reason {@code unspecified}
     * @param id The ID of the cutscene to start
     * @param player The player to play the cutscene to
     * @param camRot The initial camera rotation of the player as a vector of (yaw, pitch, roll)
     * @see CutsceneManager#startCutscene(Identifier, Vec3, Vec3, Vec3, ServerPlayer, String)
     */
    public static void startCutscene(Identifier id, ServerPlayer player, Vec3 camRot) {
        startCutscene(id, player.position(), camRot, Vec3.ZERO, player);
    }

    /**
     * Stops a cutscene for a player, regardless of whether they were actually watching it or not.
     * @param player The player to stop the cutscene for
     * @param reason The reason why the cutscene is stopped
     */
    public static void stopCutscene(ServerPlayer player, EndingReason reason) {
        ((PlayerExt)player).csapi$finishCutscene(reason);
        CutsceneAPI.platform().sendPacketToPlayer(new StopCutscenePacket(reason), player);
    }

    /**
     * Stops a cutscene for a player, regardless of whether they were actually watching it or not.
     * Calls {@link #stopCutscene(ServerPlayer, EndingReason)} with {@link EndingReason#INTERRUPT}.
     * @param player The player to stop the cutscene for
     */
    public static void stopCutscene(ServerPlayer player) {
        stopCutscene(player, EndingReason.INTERRUPT);
    }
}