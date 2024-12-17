package net.thewinnt.cutscenes.forge;

import net.minecraftforge.client.event.ViewportEvent;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;

public record CameraAngleSetterImpl(ViewportEvent.ComputeCameraAngles event) implements CameraAngleSetter {
    @Override
    public float getPitch() {
        return event.getPitch();
    }

    @Override
    public float getYaw() {
        return event.getYaw();
    }

    @Override
    public float getRoll() {
        return event.getRoll();
    }

    @Override
    public void setPitch(float pitch) {
        event.setPitch(pitch);
    }

    @Override
    public void setYaw(float yaw) {
        event.setYaw(yaw);
    }

    @Override
    public void setRoll(float roll) {
        event.setRoll(roll);
    }
}
