package net.thewinnt.cutscenes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.event.EndingReason;
import net.thewinnt.cutscenes.util.PlayerExt;

import static net.minecraft.commands.Commands.*;

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
                .then(argument("type", ResourceLocationArgument.id()).suggests(SUGGEST_CUTSCENES)
                .executes(arg -> {
                    CommandSourceStack source = arg.getSource();
                    ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                    ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                    return showCutscene(source, type, player, source.getPosition(), Vec3.ZERO, Vec3.ZERO, "command");
                })
                .then(literal("at_preview")
                    .executes(arg -> {
                        CommandSourceStack source = arg.getSource();
                        ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                        ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                        if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                            arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                        }
                        return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), Vec3.ZERO, new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathRoll), "command");
                    })
                    .then(argument("camera_rotation_xy", RotationArgument.rotation())
                        .then(argument("camera_rotation_z", DoubleArgumentType.doubleArg())
                        .executes(arg -> {
                            CommandSourceStack source = arg.getSource();
                            ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                            ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                            Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                            double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                            double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                            double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                            double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                            if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                                arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                            }
                            return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), new Vec3(xRot, yRot, zRot), new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathPitch), "command");
                        })
                        .then(argument("reason", StringArgumentType.string())
                            .executes(arg -> {
                                CommandSourceStack source = arg.getSource();
                                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                                Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                                double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                                double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                                double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                                double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                                if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                                    arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                                }
                                String reason = StringArgumentType.getString(arg, "reason");
                                return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), new Vec3(xRot, yRot, zRot), new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathPitch), reason);
                            }))))
                    .then(argument("reason", StringArgumentType.string())
                        .executes(arg -> {
                            CommandSourceStack source = arg.getSource();
                            ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                            ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                            Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                            double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                            double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                            double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                            double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                            if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                                arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                            }
                            String reason = StringArgumentType.getString(arg, "reason");
                            return showCutscene(source, type, player, new Vec3(CutsceneManager.getOffset()), new Vec3(xRot, yRot, zRot), new Vec3(CutsceneManager.previewPathYaw, CutsceneManager.previewPathPitch, CutsceneManager.previewPathPitch), reason);
                        })))
                .then(argument("reason", StringArgumentType.string())
                    .executes(arg -> {
                        CommandSourceStack source = arg.getSource();
                        ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                        ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                        String reason = StringArgumentType.getString(arg, "reason");
                        return showCutscene(source, type, player, source.getPosition(), Vec3.ZERO, Vec3.ZERO, reason);
                    }))
            .then(argument("start_pos", Vec3Argument.vec3())
                .executes(arg -> {
                    CommandSourceStack source = arg.getSource();
                    ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                    ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                    Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                    if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                        arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                    }
                    return showCutscene(source, type, player, pos, Vec3.ZERO, Vec3.ZERO, "command");
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
                        double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                        double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                        double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                        if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                            arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                        }
                        return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), Vec3.ZERO, "command");
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
                            double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                            double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                            double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                            Vec2 pathRotXY = RotationArgument.getRotation(arg, "path_rotation_xy").getRotation(source);
                            double pathRotZ = DoubleArgumentType.getDouble(arg, "path_rotation_z");
                            if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                                arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                            }
                            return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), new Vec3(pathRotXY.x, pathRotXY.y, pathRotZ), "command");
                        })
                        .then(argument("reason", StringArgumentType.string())
                            .executes(arg -> {
                                CommandSourceStack source = arg.getSource();
                                ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                                Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                                Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                                double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                                double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                                double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                                double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                                Vec2 pathRotXY = RotationArgument.getRotation(arg, "path_rotation_xy").getRotation(source);
                                double pathRotZ = DoubleArgumentType.getDouble(arg, "path_rotation_z");
                                if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                                    arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                                }
                                String reason = StringArgumentType.getString(arg, "reason");
                                return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), new Vec3(pathRotXY.x, pathRotXY.y, pathRotZ), reason);
                            }))))
                    .then(argument("reason", StringArgumentType.string())
                        .executes(arg -> {
                            CommandSourceStack source = arg.getSource();
                            ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                            ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                            Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                            Vec2 rot = RotationArgument.getRotation(arg, "camera_rotation_xy").getRotation(source);
                            double rotZ = DoubleArgumentType.getDouble(arg, "camera_rotation_z");
                            double xRot = rot.y < -180 || rot.y > 180 ? Double.NaN : rot.y;
                            double yRot = rot.x < -90 || rot.x > 90 ? Double.NaN : rot.x;
                            double zRot = rotZ < -180 || rotZ > 180 ? Double.NaN : rotZ;
                            if (CutsceneManager.getPreviewedCutscene() != null && !type.equals(CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene()))) {
                                arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                            }
                            String reason = StringArgumentType.getString(arg, "reason");
                            return showCutscene(source, type, player, pos, new Vec3(xRot, yRot, zRot), Vec3.ZERO, reason);
                        }))))
                .then(argument("reason", StringArgumentType.string())
                    .executes(arg -> {
                        CommandSourceStack source = arg.getSource();
                        ResourceLocation type = ResourceLocationArgument.getId(arg, "type");
                        ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                        Vec3 pos = Vec3Argument.getVec3(arg, "start_pos");
                        if (CutsceneManager.getPreviewedCutscene() != null && type != CutsceneManager.REGISTRY.inverse().get(CutsceneManager.getPreviewedCutscene())) {
                            arg.getSource().sendSuccess(() -> Component.translatable("commands.cutscene.warning.cutscene_mismatch").withStyle(ChatFormatting.GOLD), false);
                        }
                        String reason = StringArgumentType.getString(arg, "reason");
                        return showCutscene(source, type, player, pos, Vec3.ZERO, Vec3.ZERO, reason);
                    }))))
                ))

            .then(literal("stop")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();
                    CutsceneManager.stopCutscene(player, EndingReason.COMMAND);
                    source.sendSuccess(() -> Component.translatable("commands.cutscene.stopped", player.getDisplayName()), true);
                    return 1;
                })
            .then(argument("player", EntityArgument.player())
            .executes(arg -> {
                CommandSourceStack source = arg.getSource();
                ServerPlayer player = EntityArgument.getPlayer(arg, "player");
                CutsceneManager.stopCutscene(player, EndingReason.COMMAND);
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
            })))

            .then(literal("get")
                .then(argument("player", EntityArgument.player())
                    .then(literal("start_reason")
                        .executes(context -> {
                            CommandSourceStack stack = context.getSource();
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            PlayerExt ext = ((PlayerExt) player);
                            if (ext.csapi$isWatchingCutscene()) {
                                stack.sendSuccess(() -> Component.translatable("commands.cutscene.get.start_reason", player.getName(), ext.csapi$getStartReason()), false);
                                return 1;
                            } else {
                                throw PLAYER_NOT_IN_CUTSCENE.create(player.getName());
                            }
                        })))
            ));
    }

    private static int showCutscene(CommandSourceStack source, ResourceLocation id, ServerPlayer player, Vec3 pos, Vec3 camRot, Vec3 pathRot, String startingReason) throws CommandSyntaxException {
        if (!CutsceneManager.REGISTRY.containsKey(id)) {
            throw NO_CUTSCENE.create(id.toString());
        }
        CutsceneManager.startCutscene(id, pos, camRot, pathRot, player, startingReason);
        source.sendSuccess(() -> Component.translatable("commands.cutscene.showing", id.toString(), player.getDisplayName()), true);
        return 1;
    }
}
