package net.thewinnt.cutscenes.neoforge;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thewinnt.cutscenes.command.EndingReasonArgument;

public class CutsceneAPIArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, "cutscenes");

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<EndingReasonArgument>> ENDING_REASON = REGISTRY.register(
        "ending_reason",
        () -> ArgumentTypeInfos.registerByClass(EndingReasonArgument.class, SingletonArgumentInfo.contextFree(EndingReasonArgument::endingReason))
    );
}
