package com.google.ar.sceneform.samples.gltf;

public interface ResponseCallback {
    void onResponse(String result);
    void onError(Throwable throwable);
}
