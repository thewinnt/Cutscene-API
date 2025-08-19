package net.thewinnt.cutscenes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ExecuteCommand;

import java.util.Collection;
import java.util.Collections;

public class ExecuteCommands {
    private static final SimpleCommandExceptionType ERROR_CONDITIONAL_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.execute.conditional.fail"));
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var node = dispatcher.getRoot().getChild("execute").getChild("if");
        node.addChild(Commands.literal("cutscene")
                .then(Commands.literal("watching"))
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
                return 1;
            } else {
                throw ERROR_CONDITIONAL_FAILED.create();
            }
        });
    }

    @FunctionalInterface
    interface CommandPredicate {
        boolean test(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
    }
}
