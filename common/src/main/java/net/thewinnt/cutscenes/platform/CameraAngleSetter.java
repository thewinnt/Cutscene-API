package net.thewinnt.cutscenes.platform;

public interface CameraAngleSetter {
    float getPitch();
    float getYaw();
    float getRoll();
    void setPitch(float pitch);
    void setYaw(float yaw);
    void setRoll(float roll);
}
