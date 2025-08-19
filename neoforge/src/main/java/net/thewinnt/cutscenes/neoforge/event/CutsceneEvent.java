package net.thewinnt.cutscenes.neoforge.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.thewinnt.cutscenes.CutsceneType;

/**
 * CutsceneEvent is fired whenever an event involving a {@link Player} and a cutscene occurs.
 * <p>
 * All children of this event are fired on the {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}.
 */
public abstract class CutsceneEvent extends PlayerEvent {
    private final CutsceneType type;
    private final ResourceLocation id;

    public CutsceneEvent(Player player, CutsceneType type, ResourceLocation id) {
        super(player);
        this.type = type;
        this.id = id;
    }

    public CutsceneType getType() {
        return type;
    }

    public ResourceLocation getId() {
        return id;
    }
}
