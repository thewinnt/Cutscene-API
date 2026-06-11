package net.thewinnt.cutscenes.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.thewinnt.cutscenes.client.ClientPlatformAbstractions;

public class FabricClientPlatform extends FabricPlatform implements ClientPlatformAbstractions {
    @Override
    public void submitOnLogout(Runnable runnable) {
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> runnable.run());
    }
}
