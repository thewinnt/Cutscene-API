package net.thewinnt.cutscenes;

import javax.annotation.Nonnull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.math.Vector3f;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.thewinnt.cutscenes.math.*;
import net.thewinnt.cutscenes.networking.CutsceneNetworkHandler;
import net.thewinnt.cutscenes.networking.packets.PreviewCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.networking.packets.UpdateCutscenesPacket;
import net.thewinnt.cutscenes.path.Path;
import net.thewinnt.cutscenes.path.PathLike.SegmentSerializer;

@Mod.EventBusSubscriber(bus = Bus.FORGE)
public class CutsceneManager {
    public static final BiMap<ResourceLocation, CutsceneType> REGISTRY = HashBiMap.create(5);
    private static final BiMap<ResourceLocation, SegmentSerializer<?>> SEGMENT_TYPE_REGISTRY = HashBiMap.create(7);
    private static CutsceneType previewedCutscene;
    private static Vec3 previewOffset;
    public static float previewPathYaw;
    public static float previewPathPitch;
    public static float previewPathRoll;
    public static boolean isPreviewRelative;

    public static final ResourceLocation ASCEND_ID = new ResourceLocation("cutscenes:tests/ascend");
    public static final ResourceLocation COOL_PATH_ID = new ResourceLocation("cutscenes:tests/cool_path");
    public static final ResourceLocation MULTI_TYPE_ID = new ResourceLocation("cutscenes:tests/multi_type");
    public static final ResourceLocation HORIZONTAL_LINE_ID = new ResourceLocation("cutscenes:tests/horizontal_line");
    public static final ResourceLocation CATMULL_ROM_TEST_ID = new ResourceLocation("cutscenes:tests/catmull_rom_test");

    public static final CutsceneType ASCEND = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 12.5, 10), null, new Vec3(0, 25, 0))),
        new Path(new ConstantPoint(Vec3.ZERO)),
        100
    );
    public static final CutsceneType COOL_PATH = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), null, null, new Vec3(0, 10, 0)))
                .continueBezier(new Vec3(-50, 1, 0), new Vec3(-50, 10, 25))
                .continueBezier(new Vec3(-25, 50, 0), new Vec3(-25, 30, 10), 10)
                .continueBezier(new Vec3(-25, 0, 10), new Vec3(0, 0, 0)),
        new Path(new LineSegment(new Vec3(-30, 30, 0), new Vec3(20, -20, 0), EasingFunction.LINEAR, EasingFunction.IN_CUBIC, EasingFunction.LINEAR, true)),
        500
    );
    public static final CutsceneType MULTI_TYPE = new CutsceneType(
        new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 0, 0), new Vec3(0, 10, 0), new Vec3(10, 10, 0)))
                .continueBezier(null, new Vec3(10, 10, 10)) // +/-
                .continueBezier(new Vec3(15, 30, 15), new Vec3(20, 20, 20)) // -/+
                .continueBezier(null, new Vec3(30, 20, 30)) // +/-
                .continueBezier(null, new Vec3(30, 30, 30)), // -/-
        new Path(new ConstantPoint(Vec3.ZERO)),
        500
    );

    public static final CutsceneType HORIZONTAL_LINE = new CutsceneType(
        new Path(new LineSegment(new Vec3(-6, 0, 0), new Vec3(6, 0, 0))),
        new Path(new ConstantPoint(Vec3.ZERO)), 
        200
    );

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

    public static final SegmentSerializer<LineSegment> LINE = SegmentSerializer.of(LineSegment::fromNetwork, LineSegment::fromJSON);
    public static final SegmentSerializer<BezierCurve> BEZIER = SegmentSerializer.of(BezierCurve::fromNetwork, BezierCurve::fromJSON);
    public static final SegmentSerializer<CatmullRomSpline> CATMULL_ROM = SegmentSerializer.of(CatmullRomSpline::fromNetwork, CatmullRomSpline::fromJSON);
    public static final SegmentSerializer<Path> PATH = SegmentSerializer.of(Path::fromNetwork, Path::fromJSON);
    public static final SegmentSerializer<ConstantPoint> CONSTANT = SegmentSerializer.of(ConstantPoint::fromNetwork, ConstantPoint::fromJSON);
    public static final SegmentSerializer<LookAtPoint> LOOK_AT_POINT = SegmentSerializer.of(LookAtPoint::fromNetwork, LookAtPoint::fromJSON);
    public static final SegmentSerializer<Transition> TRANSITION = SegmentSerializer.of(Transition::fromNetwork, Transition::fromJSON);

    public static CutsceneType registerCutscene(ResourceLocation id, @Nonnull CutsceneType type) {
        REGISTRY.put(id, type);
        return type;
    }

    public static void registerSegmentType(ResourceLocation id, SegmentSerializer<?> type) {
        SEGMENT_TYPE_REGISTRY.put(id, type);
    }

    public static ResourceLocation getSegmentTypeId(SegmentSerializer<?> type) {
        return SEGMENT_TYPE_REGISTRY.inverse().get(type);
    }

    public static SegmentSerializer<?> getSegmentType(ResourceLocation id) {
        return SEGMENT_TYPE_REGISTRY.get(id);
    }

    public static void setPreviewedCutscene(CutsceneType type, Vec3 offset, float pathYaw, float pathPitch, float pathRoll) {
        previewedCutscene = type;
        previewOffset = offset;
        previewPathYaw = pathYaw;
        previewPathPitch = pathPitch;
        previewPathRoll = pathRoll;
        CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new PreviewCutscenePacket(REGISTRY.inverse().get(type), offset, pathYaw, pathPitch, pathRoll));
    }

    public static CutsceneType getPreviewedCutscene() {
        return previewedCutscene;
    }

    public static void registerBuiltInCutscenes() {
        REGISTRY.put(ASCEND_ID, ASCEND);
        REGISTRY.put(COOL_PATH_ID, COOL_PATH);
        REGISTRY.put(MULTI_TYPE_ID, MULTI_TYPE);
        REGISTRY.put(HORIZONTAL_LINE_ID, HORIZONTAL_LINE);
        REGISTRY.put(CATMULL_ROM_TEST_ID, CATMULL_ROM_TEST);
    }

    public static Vector3f getOffset() {
        if (previewOffset != null) {
            return new Vector3f(previewOffset);
        } else {
            return new Vector3f(0, 100, 0);
        }
    }

    @SubscribeEvent
    public static void sendRegistry(OnDatapackSyncEvent event) {
        if (event != null && event.getPlayer() != null) {
            CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> event.getPlayer()), new UpdateCutscenesPacket(REGISTRY));
        } else {
            CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateCutscenesPacket(REGISTRY));
        }
    }

    public static void startCutscene(ResourceLocation id, Vec3 startPos, Vec3 camRot, Vec3 pathRot, ServerPlayer player) {
        CutsceneNetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new StartCutscenePacket(id, startPos, (float)camRot.x, (float)camRot.y, (float)camRot.z, (float)pathRot.x, (float)pathRot.y, (float)pathRot.z));
    }
}

