package net.thewinnt.cutscenes.event;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum EndingReason implements StringRepresentable {
    /** A cutscene has finished naturally */
    FINISH("finish"),

    /** A cutscene has been interrupted by another cutscene or the player logging out */
    INTERRUPT("interrupt"),

    /** A cutscene has been stopped by command */
    COMMAND("command"),

    /** A cutscene ended with an error */
    ERROR("error");

    private final String name;

    EndingReason(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
