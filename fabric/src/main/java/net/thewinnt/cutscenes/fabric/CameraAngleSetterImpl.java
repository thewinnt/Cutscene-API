package net.thewinnt.cutscenes.fabric;

import net.thewinnt.cutscenes.platform.CameraAngleSetter;

public class CameraAngleSetterImpl implements CameraAngleSetter {
    private float pitch, yaw, roll;

    public CameraAngleSetterImpl(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public float getYaw() {
        return yaw;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public float getRoll() {
        return roll;
    }

    @Override
    public void setRoll(float roll) {
        this.roll = roll;
    }
}
