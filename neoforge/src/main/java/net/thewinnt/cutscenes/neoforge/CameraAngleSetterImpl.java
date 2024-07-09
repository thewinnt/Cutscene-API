package net.thewinnt.cutscenes.neoforge;

import net.neoforged.neoforge.client.event.ViewportEvent;
import net.thewinnt.cutscenes.platform.CameraAngleSetter;

public class CameraAngleSetterImpl implements CameraAngleSetter {
    public final ViewportEvent.ComputeCameraAngles event;

    public CameraAngleSetterImpl(ViewportEvent.ComputeCameraAngles event) {
        this.event = event;
    }

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
