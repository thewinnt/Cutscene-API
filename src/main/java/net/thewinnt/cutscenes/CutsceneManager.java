package net.thewinnt.cutscenes;

import javax.annotation.Nonnull;

import org.joml.Vector3f;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.path.BezierCurve;
import net.thewinnt.cutscenes.path.CatmullRomSpline;
import net.thewinnt.cutscenes.path.ConstantPoint;
import net.thewinnt.cutscenes.path.EasingFunction;
import net.thewinnt.cutscenes.path.LineSegment;
import net.thewinnt.cutscenes.path.LookAtPoint;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike.SegmentSerializer;
import net.thewinnt.cutscenes.path.PathTransition;
import net.thewinnt.cutscenes.path.point.PointProvider.PointSerializer;
import net.thewinnt.cutscenes.transition.NoopTransition;
import net.thewinnt.cutscenes.transition.SmoothEaseTransition;
import net.thewinnt.cutscenes.transition.Transition.TransitionSerializer;
import net.thewinnt.cutscenes.path.point.StaticPointProvider;
import net.thewinnt.cutscenes.path.point.WaypointProvider;
import net.thewinnt.cutscenes.path.point.WorldPointProvider;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class CutsceneManager {
    /** The cutscene registry, where all cutscenes are stored. Only read from this, please */
    public static final BiMap<ResourceLocation, CutsceneType> REGISTRY = HashBiMap.create();

    /** The segment type registry, where the segment types are stored */
    private static final BiMap<ResourceLocation, SegmentSerializer<?>> SEGMENT_TYPE_REGISTRY = HashBiMap.create();

    /** The point type registry, where the point types are stored */
    private static final BiMap<ResourceLocation, PointSerializer<?>> POINT_TYPE_REGISTRY = HashBiMap.create();

    /** The transition type registry, where the transition types are stored */
    private static final BiMap<ResourceLocation, TransitionSerializer<?>> TRANSITION_TYPE_REGISTRY = HashBiMap.create();

    /** The currently previewed cutscene */
    private static CutsceneType previewedCutscene;

    /** The start position of the preview */
    private static Vec3 previewOffset;

    /** The x rotation of the preview path */
    public static float previewPathYaw;

    /** The y rotation of the preview path */
    public static float previewPathPitch;

    /** The z rotation of the preview path */
    public static float previewPathRoll;

    // BUILT-IN CUTSCENES //
    // They're not used anywhere and are here to show how you can make some yourself with code
    // You can also use datapacks to create cutscenes, it has all the same functionality, except it's better

    public static final ResourceLocation ASCEND_ID = new ResourceLocation("cutscenes:tests/ascend");
    public static final ResourceLocation COOL_PATH_ID = new ResourceLocation("cutscenes:tests/cool_path");
    public static final ResourceLocation MULTI_TYPE_ID = new ResourceLocation("cutscenes:tests/multi_type");
    public static final ResourceLocation HORIZONTAL_LINE_ID = new ResourceLocation("cutscenes:tests/horizontal_line");
    public static final ResourceLocation CATMULL_ROM_TEST_ID = new ResourceLocation("cutscenes:tests/catmull_rom_test");

    /** Ascends you 25 blocks up with a little twist */
    public static final CutsceneType ASCEND = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 12.5, 10), null, new Vec3(0, 25, 0))),
        new Path(new ConstantPoint(Vec3.ZERO)),
        100
    );

    /** A cool path made of continuous Bezier curves, also features rotation changes */
    public static final CutsceneType COOL_PATH = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), null, null, new Vec3(0, 10, 0)))
                .continueBezier(new Vec3(-50, 1, 0), new Vec3(-50, 10, 25)) // adds a new Bezier curve with arguments: (see below)
                .continueBezier(new Vec3(-25, 50, 0), new Vec3(-25, 30, 10), 10) // start = prev.end; control_a = prev.control_b.lerp(prev.end, 2); control_b and end are specified by user
                .continueBezier(new Vec3(-25, 0, 10), new Vec3(0, 0, 0)), // if previous is not Bezier or doesn't have control_b, args are: start = prev.end; control_b; null; end
        new Path(new LineSegment(new Vec3(-30, 30, 0), new Vec3(20, -20, 0), EasingFunction.LINEAR, EasingFunction.IN_CUBIC, EasingFunction.LINEAR, true)),
        500
    );

    /** A path that combines some Bezier curve configurations */
    public static final CutsceneType MULTI_TYPE = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 0, 0), new Vec3(0, 10, 0), new Vec3(10, 10, 0)))
                .continueBezier(null, new Vec3(10, 10, 10)) // +/-
                .continueBezier(new Vec3(15, 30, 15), new Vec3(20, 20, 20)) // -/+
                .continueBezier(null, new Vec3(30, 20, 30)) // +/-
                .continueBezier(null, new Vec3(30, 30, 30)), // -/-
        new Path(new ConstantPoint(Vec3.ZERO)),
        500
    );

    /** A horizontal line going 12 blocks towards +X */
    public static final CutsceneType HORIZONTAL_LINE = new CutsceneType(
        new Path(new LineSegment(new Vec3(-6, 0, 0), new Vec3(6, 0, 0))),
        new Path(new ConstantPoint(Vec3.ZERO)), 
        200
    );

    /** A path consisting of a Catmull-Rom spline with a lot of random points */
    public static final CutsceneType CATMULL_ROM_TEST = new CutsceneType(
        new Path(new CatmullRomSpline(
            new Vec3(0, 0, 0),
            new Vec3(0, 10, 0),
            new Vec3(5, 4, 9),
            new Vec3(-6, 19.3, -8.37),
            new Vec3(-9.71, -8.98, -7.5), // starting from here, the points were generated using a script
            new Vec3(-17.89, -11.57, -17.17),
            new Vec3(0.1, -10.19, 6.9),
            new Vec3(12.87, -21.55, -9.95),
            new Vec3(-17.36, 23.98, 14.78),
            new Vec3(-16.87, -23.58, -23.87),
            new Vec3(-6.15, 14.8, -3.45),
            new Vec3(-16.72, 16.56, -16.24),
            new Vec3(-1.63, -17.64, 16.57),
            new Vec3(-3.98, 4.25, 11.01),
            new Vec3(-19.31, -16.89, -8.79),
            new Vec3(-12.12, 18.33, -1.0),
            new Vec3(-4.78, 16.29, -8.53),
            new Vec3(-8.76, 19.35, 21.01),
            new Vec3(0.8, 8.73, 10.65)
        )),
        new Path(new ConstantPoint(Vec3.ZERO)),
        200
    );

    // SEGMENT TYPES //
    // Segment serializers are used to identify and read segment types. The writing is performed on instances of
    // segments obtained from these serializers.

    public static final SegmentSerializer<LineSegment> LINE = SegmentSerializer.of(LineSegment::fromNetwork, LineSegment::fromJSON);
    public static final SegmentSerializer<BezierCurve> BEZIER = SegmentSerializer.of(BezierCurve::fromNetwork, BezierCurve::fromJSON);
    public static final SegmentSerializer<CatmullRomSpline> CATMULL_ROM = SegmentSerializer.of(CatmullRomSpline::fromNetwork, CatmullRomSpline::fromJSON);
    public static final SegmentSerializer<Path> PATH = SegmentSerializer.of(Path::fromNetwork, Path::fromJSON);
    public static final SegmentSerializer<ConstantPoint> CONSTANT = SegmentSerializer.of(ConstantPoint::fromNetwork, ConstantPoint::fromJSON);
    public static final SegmentSerializer<LookAtPoint> LOOK_AT_POINT = SegmentSerializer.of(LookAtPoint::fromNetwork, LookAtPoint::fromJSON);
    public static final SegmentSerializer<PathTransition> PATH_TRANSITION = SegmentSerializer.of(PathTransition::fromNetwork, PathTransition::fromJSON);

    // POINT TYPES //
    // Point serializers are used to identify and read point types. A point type gets a Level in and returns
    // a point based on that.

    public static final PointSerializer<StaticPointProvider> STATIC = PointSerializer.of(StaticPointProvider::fromNetwork, StaticPointProvider::fromJSON);
    public static final PointSerializer<WaypointProvider> WAYPOINT = PointSerializer.of(WaypointProvider::fromNetwork, WaypointProvider::fromJSON);
    public static final PointSerializer<WorldPointProvider> WORLD = PointSerializer.of(WorldPointProvider::fromNetwork, WorldPointProvider::fromJSON);

    // TRANSITION TYPES //
    // Transitions make you enter and leave a cutscene with beauty, instead of simply snapping into it.

    public static final TransitionSerializer<NoopTransition> NO_OP = TransitionSerializer.of(NoopTransition::fromNetwork, NoopTransition::fromJSON);
    public static final TransitionSerializer<SmoothEaseTransition> SMOOTH_EASE = TransitionSerializer.of(SmoothEaseTransition::fromNetwork, SmoothEaseTransition::fromJSON);

    /** 
     * Registers a cutscene
     * @param id The ID of the cutscene that will be used in commands
     * @param type The actual cutscene type you want to register
     * @return Your cutscene for storing
     */
    public static CutsceneType registerCutscene(ResourceLocation id, @Nonnull CutsceneType type) {
        REGISTRY.put(id, type);
        return type;
    }

    /**
     * Registers a segment serializer
     * @param id The ID of the segment type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerSegmentType(ResourceLocation id, SegmentSerializer<?> type) {
        SEGMENT_TYPE_REGISTRY.put(id, type);
    }

    /**
     * Registers a point serializer
     * @param id The ID of the point type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerPointType(ResourceLocation id, PointSerializer<?> type) {
        POINT_TYPE_REGISTRY.put(id, type);
    }

    /**
     * Registers a transition serializer
     * @param id The ID of the transition type that will be used in datapacks
     * @param type The serializer to register
     */
    public static void registerTransitionType(ResourceLocation id, TransitionSerializer<?> type) {
        TRANSITION_TYPE_REGISTRY.put(id, type);
    }

    /** Returns the ID of the specified serializer, or {@code null} if it's not registered */
    public static ResourceLocation getSegmentTypeId(SegmentSerializer<?> type) {
        return SEGMENT_TYPE_REGISTRY.inverse().get(type);
    }

    /** Returns the segment serializer with this ID, or {@code null} if it doesn't exist */
    public static SegmentSerializer<?> getSegmentType(ResourceLocation id) {
        return SEGMENT_TYPE_REGISTRY.get(id);
    }

    /** Returns the ID of the specified point type, or {@code null} if it's not registered */
    public static ResourceLocation getPointTypeId(PointSerializer<?> type) {
        return POINT_TYPE_REGISTRY.inverse().get(type);
    }

    /** Returns the point serializer with this ID, or {@code null} if it doesn't exist */
    public static PointSerializer<?> getPointType(ResourceLocation id) {
        return POINT_TYPE_REGISTRY.get(id);
    }

    /** Sets the currently previewed cutscene and tells the clients */
    public static void setPreviewedCutscene(CutsceneType type, Vec3 offset, float pathYaw, float pathPitch, float pathRoll) {
        previewedCutscene = type;
        previewOffset = offset;
        previewPathYaw = pathYaw;
        previewPathPitch = pathPitch;
        previewPathRoll = pathRoll;
        CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new PreviewCutscenePacket(REGISTRY.inverse().get(type), offset, pathYaw, pathPitch, pathRoll));
    }

    // self-explanatory
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

    /** Sends the cutscene registry to client */
    @SubscribeEvent
    public static void sendRegistry(OnDatapackSyncEvent event) {
        if (event != null && event.getPlayer() != null) {
            CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> event.getPlayer()), new UpdateCutscenesPacket(REGISTRY));
        } else {
            CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateCutscenesPacket(REGISTRY));
        }
    }

    /** 
     * Starts a cutscene for a player
     * @param id The ID of the cutscene to start
     * @param startPos The starting position for the cutscene
     * @param camRot The initial camera rotation of the player as a vector of (yaw, pitch, roll)
     * @param pathRot The path rotation as a vector of (yaw, pitch, roll)
     * @param player The player to play the cutscene to
     */
    public static void startCutscene(ResourceLocation id, Vec3 startPos, Vec3 camRot, Vec3 pathRot, ServerPlayer player) {
        ((ServerPlayerExt)player).setCutsceneTicks(REGISTRY.get(id).length);
        CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new StartCutscenePacket(id, startPos, (float)camRot.x, (float)camRot.y, (float)camRot.z, (float)pathRot.x, (float)pathRot.y, (float)pathRot.z));
    }
}

