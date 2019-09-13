package com.example.zahid.a3dtryon;

/**
 * Created by Zahid on 23-Jul-18.
 */

import android.hardware.Camera.Size;

public interface VideoRecordingHandler {
    public boolean onPrepareRecording();
    public Size getVideoSize();
    public int getDisplayRotation();
}
