package net.thewinnt.cutscenes.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

public record ActionToggles(
    boolean disableDamage, // depends on considerSpectator
    boolean disableAttacking, // depends on disableBreakingBlocks
    boolean disablePickingBlocks,
    boolean disableBreakingBlocks,
    boolean disableUsingItems,
    boolean disableBlockInteractions, // depends on usingItems
    boolean disableEntityInteractions,
    boolean considerSpectator
) {
    private ActionToggles(Builder builder) {
        this(
            builder.disableDamage,
            builder.disableAttacking,
            builder.disablePickingBlocks,
            builder.disableBreakingBlocks,
            builder.disableUsingItems,
            builder.disableBlockInteractions,
            builder.disableEntityInteractions,
            builder.considerSpectator
        );
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(disableDamage);
        buf.writeBoolean(disableAttacking);
        buf.writeBoolean(disablePickingBlocks);
        buf.writeBoolean(disableBreakingBlocks);
        buf.writeBoolean(disableUsingItems);
        buf.writeBoolean(disableBlockInteractions);
        buf.writeBoolean(disableEntityInteractions);
        buf.writeBoolean(considerSpectator);
    }

    public static ActionToggles fromNetwork(FriendlyByteBuf buf) {
        boolean disableDamage = buf.readBoolean();
        boolean disableAttacking = buf.readBoolean();
        boolean disablePickingBlocks = buf.readBoolean();
        boolean disableBreakingBlocks = buf.readBoolean();
        boolean disableUsingItems = buf.readBoolean();
        boolean disableBlockInteractions = buf.readBoolean();
        boolean disableEntityInteractions = buf.readBoolean();
        boolean considerSpectator = buf.readBoolean();
        return new ActionToggles(disableDamage, disableAttacking, disablePickingBlocks, disableBreakingBlocks, disableUsingItems, disableBlockInteractions, disableEntityInteractions, considerSpectator);
    }

    public static ActionToggles fromJson(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new ActionToggles(new Builder(json.getAsBoolean()));
        }
        JsonObject obj = json.getAsJsonObject();
        Builder builder = new Builder(GsonHelper.getAsBoolean(obj, "default", true));
        if (obj.has("damage")) builder.setDisableDamage(GsonHelper.getAsBoolean(obj, "damage"));
        if (obj.has("attacking")) builder.setDisableAttacking(GsonHelper.getAsBoolean(obj, "attacking"));
        if (obj.has("block_picking")) builder.setDisablePickingBlocks(GsonHelper.getAsBoolean(obj, "block_picking"));
        if (obj.has("block_breaking")) builder.setDisableBreakingBlocks(GsonHelper.getAsBoolean(obj, "block_breaking"));
        if (obj.has("using_items")) builder.setDisableUsingItems(GsonHelper.getAsBoolean(obj, "using_items"));
        if (obj.has("block_interactions")) builder.setDisableBlockInteractions(GsonHelper.getAsBoolean(obj, "block_interactions"));
        if (obj.has("entity_interactions")) builder.setDisableEntityInteractions(GsonHelper.getAsBoolean(obj, "entity_interactions"));
        if (obj.has("consider_spectator")) builder.setConsiderSpectator(GsonHelper.getAsBoolean(obj, "consider_spectator"));
        return builder.build();
    }

    public static class Builder {
        private boolean disableDamage;
        private boolean disableAttacking;
        private boolean disablePickingBlocks;
        private boolean disableBreakingBlocks;
        private boolean disableUsingItems;
        private boolean disableBlockInteractions;
        private boolean disableEntityInteractions;
        private boolean considerSpectator;

        public Builder(boolean defaultValue) {
            this.disableDamage = defaultValue;
            this.disableAttacking = defaultValue;
            this.disablePickingBlocks = defaultValue;
            this.disableBreakingBlocks = defaultValue;
            this.disableUsingItems = defaultValue;
            this.disableBlockInteractions = defaultValue;
            this.disableEntityInteractions = defaultValue;
            this.considerSpectator = defaultValue;
        }

        public Builder setDisableDamage(boolean disableDamage) {
            this.disableDamage = disableDamage;
            return this;
        }

        public Builder setDisableAttacking(boolean disableAttacking) {
            this.disableAttacking = disableAttacking;
            return this;
        }

        public Builder setDisablePickingBlocks(boolean disablePickingBlocks) {
            this.disablePickingBlocks = disablePickingBlocks;
            return this;
        }

        public Builder setDisableBreakingBlocks(boolean disableBreakingBlocks) {
            this.disableBreakingBlocks = disableBreakingBlocks;
            return this;
        }

        public Builder setDisableUsingItems(boolean disableUsingItems) {
            this.disableUsingItems = disableUsingItems;
            return this;
        }

        public Builder setDisableBlockInteractions(boolean disableBlockInteractions) {
            this.disableBlockInteractions = disableBlockInteractions;
            return this;
        }

        public Builder setDisableEntityInteractions(boolean disableEntityInteractions) {
            this.disableEntityInteractions = disableEntityInteractions;
            return this;
        }

        public Builder setConsiderSpectator(boolean considerSpectator) {
            this.considerSpectator = considerSpectator;
            return this;
        }

        public ActionToggles build() {
            return new ActionToggles(this);
        }
    }
}
