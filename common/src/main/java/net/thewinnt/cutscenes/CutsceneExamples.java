package net.thewinnt.cutscenes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.easing.types.SimpleEasing;
import net.thewinnt.cutscenes.path.BezierCurve;
import net.thewinnt.cutscenes.path.CatmullRomSpline;
import net.thewinnt.cutscenes.path.ConstantPoint;
import net.thewinnt.cutscenes.path.LineSegment;
import net.thewinnt.cutscenes.path.Path;

/** Stores some examples of cutscenes made with code. I'm not sure how you can use this */
public class CutsceneExamples {

    // BUILT-IN CUTSCENES //
    // They're not used anywhere and are here to show how you can make some yourself with code
    // You can also use datapacks to create cutscenes, it has all the same functionality, except it's better

    public static final ResourceLocation ASCEND_ID = ResourceLocation.parse("cutscenes:tests/ascend");
    public static final ResourceLocation COOL_PATH_ID = ResourceLocation.parse("cutscenes:tests/cool_path");
    public static final ResourceLocation MULTI_TYPE_ID = ResourceLocation.parse("cutscenes:tests/multi_type");
    public static final ResourceLocation HORIZONTAL_LINE_ID = ResourceLocation.parse("cutscenes:tests/horizontal_line");
    public static final ResourceLocation CATMULL_ROM_TEST_ID = ResourceLocation.parse("cutscenes:tests/catmull_rom_test");
    /**
     * Ascends you 25 blocks up with a little twist
     */
    public static final CutsceneType ASCEND = new CutsceneType(
            new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 12.5, 10), null, new Vec3(0, 25, 0))),
            new Path(new ConstantPoint(Vec3.ZERO)),
            100
    );
    /**
     * A cool path made of continuous Bézier curves, also features rotation changes
     */
    public static final CutsceneType COOL_PATH = new CutsceneType(
            new Path(new BezierCurve(new Vec3(0, 0, 0), null, null, new Vec3(0, 10, 0)))
                    .continueBezier(new Vec3(-50, 1, 0), new Vec3(-50, 10, 25)) // adds a new Bezier curve with arguments: (see below)
                    .continueBezier(new Vec3(-25, 50, 0), new Vec3(-25, 30, 10), 10) // start = prev.end; control_a = prev.control_b.lerp(prev.end, 2); control_b and end are specified by user
                    .continueBezier(new Vec3(-25, 0, 10), new Vec3(0, 0, 0)), // if previous is not Bezier or doesn't have control_b, args are: start = prev.end; control_b; null; end
            new Path(new LineSegment(new Vec3(-30, 30, 0), new Vec3(20, -20, 0), SimpleEasing.LINEAR, SimpleEasing.IN_CUBIC, SimpleEasing.LINEAR, true)),
            500
    );
    /**
     * A path that combines some Bézier curve configurations
     */
    public static final CutsceneType MULTI_TYPE = new CutsceneType(
            new Path(new BezierCurve(new Vec3(0, 0, 0), new Vec3(10, 0, 0), new Vec3(0, 10, 0), new Vec3(10, 10, 0)))
                    .continueBezier(null, new Vec3(10, 10, 10)) // +/-
                    .continueBezier(new Vec3(15, 30, 15), new Vec3(20, 20, 20)) // -/+
                    .continueBezier(null, new Vec3(30, 20, 30)) // +/-
                    .continueBezier(null, new Vec3(30, 30, 30)), // -/-
            new Path(new ConstantPoint(Vec3.ZERO)),
            500
    );
    /**
     * A horizontal line going 12 blocks towards +X
     */
    public static final CutsceneType HORIZONTAL_LINE = new CutsceneType(
            new Path(new LineSegment(new Vec3(-6, 0, 0), new Vec3(6, 0, 0))),
            new Path(new ConstantPoint(Vec3.ZERO)),
            200
    );
    /**
     * A path consisting of a Catmull-Rom spline with a lot of random points
     */
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
}
