package net.thewinnt.cutscenes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.thewinnt.cutscenes.CutsceneManager;
import net.thewinnt.cutscenes.CutsceneType;
import net.thewinnt.cutscenes.util.PlayerExt;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static net.thewinnt.cutscenes.command.CutsceneCommand.SUGGEST_CUTSCENES;

public class ExecuteCommands {
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var node = dispatcher.getRoot().getChild("execute");
        addConditionals(node.getChild("if"), node, true);
        addConditionals(node.getChild("unless"), node, false);
    }

    private static void addConditionals(CommandNode<CommandSourceStack> nodeToAdd, CommandNode<CommandSourceStack> rootNode, boolean isIf) {
        nodeToAdd.addChild(Commands.literal("cutscene")
            .then(Commands.literal("watching")
                .then(addConditional(
                    rootNode,
                    Commands.argument("player", EntityArgument.player()),
                    isIf,
                    context -> getPlayer(context).csapi$isWatchingCutscene()
                ))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(addConditional(
                        rootNode,
                        Commands.argument("cutscene", IdentifierArgument.id())
                            .suggests(SUGGEST_CUTSCENES),
                        isIf,
                        context -> {
                            CutsceneType type = getPlayer(context).csapi$getRunningCutscene();
                            return Objects.equals(CutsceneManager.REGISTRY.inverse().get(type), IdentifierArgument.getId(context, "cutscene"));
                        }
                    ))))
            .then(Commands.literal("start_reason")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.literal("contains")
                        .then(addConditional(
                            rootNode,
                                Commands.argument("substring", StringArgumentType.string()),
                                isIf,
                                context -> getPlayer(context).csapi$getStartReason().contains(StringArgumentType.getString(context, "substring"))
                        ))
                    )
                    .then(Commands.literal("equals")
                        .then(addConditional(
                            rootNode,
                            Commands.argument("string", StringArgumentType.string()),
                            isIf,
                            context -> getPlayer(context).csapi$getStartReason().equals(StringArgumentType.getString(context, "string"))
                        ))
                    )
            ))
            .then(Commands.literal("end_reason")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(addConditional(
                        rootNode,
                        Commands.argument("reason", EndingReasonArgument.endingReason()),
                        isIf,
                        context -> getPlayer(context).csapi$getEndReason() == EndingReasonArgument.getEndingReason(context, "reason")
                    ))
            ))
            .build());
    }

    private static Collection<CommandSourceStack> expect(CommandContext<CommandSourceStack> context, boolean actual, boolean expected) {
        return expected == actual ? Collections.singleton(context.getSource()) : Collections.emptyList();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addConditional(
        CommandNode<CommandSourceStack> commandNode, ArgumentBuilder<CommandSourceStack, ?> builder, boolean value, CommandPredicate test
    ) {
        return builder.fork(commandNode, commandContext -> expect(commandContext, value, test.test(commandContext))).executes(commandContext -> {
            if (value == test.test(commandContext)) {
                commandContext.getSource().sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), false);
                LOGGER.info("Test passed!");
                return 1;
            } else {
                throw ERROR_CONDITIONAL_FAILED.create();
            }
        });
    }

    private static PlayerExt getPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return ((PlayerExt) EntityArgument.getPlayer(context, "player"));
    }

    @FunctionalInterface
    interface CommandPredicate {
        boolean test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
    }
}
