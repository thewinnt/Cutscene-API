package net.thewinnt.cutscenes.client.overlay;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.Profiler;
import net.thewinnt.cutscenes.client.Overlay;
import net.thewinnt.cutscenes.effect.configuration.TriangleStripConfiguration;
import net.thewinnt.cutscenes.mixin.GuiGraphicsAccessor;
import net.thewinnt.cutscenes.mixin.RenderPipelinesAccessor;
import net.thewinnt.cutscenes.util.DynamicVertex;
import net.thewinnt.cutscenes.util.TimeProvider;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.Arrays;

public class TriangleStripOverlay implements Overlay {
    public static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelinesAccessor.guiSnippet())
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
        .withCull(false)
        .withLocation(ResourceLocation.parse("cutscenes:pipeline/triangle_strip"))
        .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
        .build();
    private final TriangleStripConfiguration config;

    public TriangleStripOverlay(TriangleStripConfiguration config) {
        this.config = config;
    }

    @Override
    public void render(Minecraft minecraft, GuiGraphics graphics, int width, int height, Object config) {
        Profiler.get().push("cutscenes:triangle_strip");
        TimeProvider time = (TimeProvider) config;
        graphics.nextStratum();
        Tesselator tesselator = Tesselator.getInstance();
        double t = time.getProgress();
        Matrix3x2fStack stack = graphics.pose();
        Vertex[] vertices = Arrays.stream(this.config.vertices()).map(i -> new Vertex(
            i.x().get(t, width),
            i.y().get(t, height),
            i.color().toARGB(t)
        )).toArray(Vertex[]::new);
        stack.pushMatrix();
        stack.translate(100, 150);
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (Vertex i : vertices) {
            builder.addVertexWith2DPose(stack, i.x, i.y, 0).setColor(i.color);
        }
        stack.popMatrix();
        ((GuiGraphicsAccessor) graphics).guiRenderState().submitGuiElement(new GuiElementRenderState() {
            final TriangleStripConfiguration config = TriangleStripOverlay.this.config;

            @Override
            public void buildVertices(VertexConsumer consumer, float z) {
                double t = time.getProgress();
                Matrix3x2fStack stack = graphics.pose();
                Vertex[] vertices = Arrays.stream(this.config.vertices()).map(i -> new Vertex(
                    i.x().get(t, width),
                    i.y().get(t, height),
                    i.color().toARGB(t)
                )).toArray(Vertex[]::new);
                stack.pushMatrix();
                for (int i = 0; i < vertices.length - 2; i += 1) {
                    consumer.addVertexWith2DPose(stack, vertices[i].x, vertices[i].y, z).setColor(vertices[i].color);
                    consumer.addVertexWith2DPose(stack, vertices[i + 1].x, vertices[i + 1].y, z).setColor(vertices[i + 1].color);
                    consumer.addVertexWith2DPose(stack, vertices[i + 2].x, vertices[i + 2].y, z).setColor(vertices[i + 2].color);
                    consumer.addVertexWith2DPose(stack, vertices[i + 2].x, vertices[i + 2].y, z).setColor(vertices[i + 2].color);
                }
                stack.popMatrix();
            }

            @Override
            public RenderPipeline pipeline() {
                return RenderPipeline.builder(RenderPipelinesAccessor.guiSnippet())
                    .withLocation(ResourceLocation.parse("cutscenes:pipeline/triangle_strip"))
                    .withCull(false).build();
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.noTexture();
            }

            @Override
            public @Nullable ScreenRectangle scissorArea() {
                return new ScreenRectangle(0, 0, width, height).transformMaxBounds(graphics.pose());
            }

            @Override
            public @Nullable ScreenRectangle bounds() {
                return scissorArea();
            }
        });
        Profiler.get().pop();
    }

    private record Vertex(float x, float y, int color) {}
}
