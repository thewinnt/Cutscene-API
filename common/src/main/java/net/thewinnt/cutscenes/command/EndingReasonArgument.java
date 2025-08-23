package net.thewinnt.cutscenes.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.util.StringRepresentable;
import net.thewinnt.cutscenes.event.EndingReason;

import java.util.Locale;

public class EndingReasonArgument extends StringRepresentableArgument<EndingReason> {
    public static final Codec<EndingReason> CODEC = StringRepresentable.fromEnumWithMapping(
        EndingReason::values,
        string -> string.toLowerCase(Locale.ROOT)
    );

    private EndingReasonArgument() {
        super(CODEC, EndingReason::values);
    }

    public static EndingReasonArgument endingReason() {
        return new EndingReasonArgument();
    }

    public static EndingReason getEndingReason(CommandContext<CommandSourceStack> context, String argument) {
        return context.getArgument(argument, EndingReason.class);
    }

    @Override
    protected String convertId(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
