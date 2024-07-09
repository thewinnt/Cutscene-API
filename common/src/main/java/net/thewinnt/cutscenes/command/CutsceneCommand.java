package net.thewinnt.cutscenes.command;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.thewinnt.cutscenes.CutsceneAPI;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.networking.packets.StartCutscenePacket;
import net.thewinnt.cutscenes.util.ServerPlayerExt;

public class CutsceneCommand {
    public static final DynamicCommandExceptionType PLAYER_ALREADY_IN_CUTSCENE = new DynamicCommandExceptionType(obj -> Component.translatable("commands.cutscene.error.player_already_in_cutscene", obj));
    public static final DynamicCommandExceptionType PLAYER_NOT_IN_CUTSCENE = new DynamicCommandExceptionType(obj -> Component.translatable("commands.cutscene.error.player_not_in_cutscene", obj));
    public static final SimpleCommandExceptionType MISSING_RUNNER = new SimpleCommandExceptionType(Component.translatable("commands.cutscene.error.no_runner"));
    public static final SimpleCommandExceptionType NO_PREVIEW = new SimpleCommandExceptionType(Component.translatable("commands.cutscene.error.no_preview"));
    public static final DynamicCommandExceptionType NO_CUTSCENE = new DynamicCommandExceptionType(obj -> Component.translatable("commands.cutscene.error.no_such_cutscene", obj));
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_CUTSCENES = (stack, builder) -> SharedSuggestionProvider.suggestResource(CutsceneManager.REGISTRY.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("cutscene")
            .requires((s) -> s.hasPermission(2))
            .then(literal("start")
            .then(argument("player", EntityArgument.player())
            .then(argument("type", ResourceLocationArgument.id())
                .suggests(SUGGEST_CUTSCENES)
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                return showCutscene(source, type, player, source.getPosition(), Vec3.ZERO, Vec3.ZERO);
            })
            .then(literal("at_preview")
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                    arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                }
                return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), Vec3.ZERO, new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll));
            })
            .then(argument("camera_rotation_xy", RotationArgument.rotation())
            .then(argument("camera_rotation_z", DoubleArgumentType.doubleArg())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                double xRot = rot.x < -180 || rot.x > 180 ? Double.NaN : rot.x;
                double yRot = rot.y < -90 || rot.y > 90 ? Double.NaN : rot.y;
                double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                    arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                }
                return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), new Vec3(xRot, yRot, zRot), new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathPitch));
            }))))
            .then(argument("start_pos", Vec3Argument.vec3())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                    arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                }
                return showCutscene(source, type, player, pos, Vec3.ZERO, Vec3.ZERO);
            })
            .then(argument("camera_rotation_xy", RotationArgument.rotation())
            .then(argument("camera_rotation_z", DoubleArgumentType.doubleArg())
                .executes(arg -> {
                    CommandSourceStack source = arg.getSource();
                    ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                    ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                    Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                    Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                    double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                    double xRot = rot.x < -180 || rot.x > 180 ? Double.NaN : rot.x;
                    double yRot = rot.y < -90 || rot.y > 90 ? Double.NaN : rot.y;
                    double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                    if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                        arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                    }
                    return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), Vec3.ZERO);
                })
                .then(argument("path_rotation_xy", RotationArgument.rotation())
                .then(argument("path_rotation_z", DoubleArgumentType.doubleArg())
                    .executes(arg -> {
                        CommandSourceStack source = arg.getSource();
                        ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                        ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                        Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                        Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                        double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                        double xRot = rot.x < -180 || rot.x > 180 ? Double.NaN : rot.x;
                        double yRot = rot.y < -90 || rot.y > 90 ? Double.NaN : rot.y;
                        double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                        Vec2 pathRotXY = RotationArgument.getRotation(arg, "path_rotation_xy").getRotation(source);
                        double pathRotZ = DoubleArgumentType.getDouble(arg, "path_rotation_z");
                        if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                            arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                        }
                        return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), new Vec3(pathRotXY.x, pathRotXY.y, pathRotZ));
                    })))))))))

            .then(literal("stop")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();
                    CutsceneManager.stopCutscene(player);
                    source.sendSuccess(() -> Component.translatable("commands.cutscene.stopped", player.getDisplayName()), true);
                    return 1;
                })
            .then(argument("player", EntityArgument.player())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                CutsceneManager.stopCutscene(player);
                source.sendSuccess(() -> Component.translatable("commands.cutscene.stopped", player.getDisplayName()), true);
                return 1;
            })))

            .then(literal("preview")
            .then(literal("set")
            .then(argument("cutscene", ResourceLocationArgument.id())
                .suggests(SUGGEST_CUTSCENES)
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation id = ResourceLocationArgument.getId(arg, "cutscene");
                CutsceneType type = CutsceneManager.REGISTRY.get(id);
                if (type == null) {
                    throw NO_CUTSCENE.create(id.toString());
                }
                source.sendSuccess(() -> Component.translatable("commands.cutscene.preview.from_block", id.toString()), true);
                CutsceneManager.setPreviewedCutscene(type, source.getPosition(), 0, 0, 0);
                return 1;
            })
            .then(argument("start_pos", Vec3Argument.vec3())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation id = ResourceLocationArgument.getId(arg, "cutscene");
                CutsceneType type = CutsceneManager.REGISTRY.get(id);
                if (type == null) {
                    throw NO_CUTSCENE.create(id.toString());
                }
                Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                source.sendSuccess(() -> Component.translatable("commands.cutscene.preview.from_block", id.toString()), true);
                CutsceneManager.setPreviewedCutscene(type, pos, 0, 0, 0);
                return 1;
            })
            .then(argument("path_rotation_xy", RotationArgument.rotation())
            .then(argument("path_rotation_z", DoubleArgumentType.doubleArg())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ResourceLocation id = ResourceLocationArgument.getId(arg, "cutscene");
                CutsceneType type = CutsceneManager.REGISTRY.get(id);
                if (type == null) {
                    throw NO_CUTSCENE.create(id.toString());
                }
                Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                Vec2 rotXY = RotationArgument.getRotation(arg, "path_rotation_xy").getRotation(source);
                double rotZ = DoubleArgumentType.getDouble(arg, "path_rotation_z");
                source.sendSuccess(() -> Component.translatable("commands.cutscene.preview.from_block", id.toString()), true);
                CutsceneManager.setPreviewedCutscene(type, pos, rotXY.x, rotXY.y, (float)rotZ);
                return 1;
            }))))))
            .then(literal("hide")
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                CutsceneManager.setPreviewedCutscene(null, Vec3.ZERO, 0, 0, 0);
                source.sendSuccess(() -> Component.translatable("commands.cutscene.preview.hide"), true);
                return 1;
            }))));

            
    }

    private static int showCutscene(CommandSourceStack source, ResourceLocation id, ServerPlayer player, Vec3 pos, Vec3 camRot, Vec3 pathRot) throws CommandSyntaxException {
        if (!CutsceneManager.REGISTRY.containsKey(id)) {
            throw NO_CUTSCENE.create(id.toString());
        }
        CutsceneType type = CutsceneManager.REGISTRY.get(id);
        CutsceneAPI.platform().sendPacketToPlayer(new StartCutscenePacket(id, pos, (float)camRot.x, (float)camRot.y, (float)camRot.z, (float)pathRot.x, (float)pathRot.y, (float)pathRot.z), player);
        ((ServerPlayerExt)player).csapi$setCutsceneTicks(type.length);
        ((ServerPlayerExt)player).csapi$setRunningCutscene(type);
        source.sendSuccess(() -> Component.translatable("commands.cutscene.showing", id.toString(), player.getDisplayName()), true);
        return 1;
    }
}
