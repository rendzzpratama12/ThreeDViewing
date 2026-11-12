package com.grafkom.viewing3d.threedviewing;

import static org.lwjgl.opengl.GL11.*;

public class HSRManager {
    public static void setupDepthTest(boolean enable) {
        if (enable) {
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS); // Pixel yang lebih dekat akan lolos uji depth
        } else {
            glDisable(GL_DEPTH_TEST);
        }
    }

    public static void setupBackFaceCulling(boolean enable) {
        if (enable) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);  // Membuang polygon bagian belakang
            glFrontFace(GL_CCW);  // Counter-Clockwise dianggap tampak depan
        } else {
            glDisable(GL_CULL_FACE);
        }
    }
}