/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grafkom.viewing3d.threedviewing;

/**
 *
 * @author Rendy Aditya
 */
import static org.lwjgl.opengl.GL11.*;

public class StereoAnaglyphRenderer {
    private float ipd = 0.2f;

    public void renderAnaglyph(Runnable renderSceneCallback) {
        // PASS 1: Mata Kiri (Saluran Warna Merah)
        glColorMask(true, false, false, true);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(ipd / 2.0f, 0, 0);
        renderSceneCallback.run();

        // PASS 2: Mata Kanan (Saluran Warna Hijau & Biru)
        glColorMask(false, true, true, true);
        glClear(GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(-ipd / 2.0f, 0, 0);
        renderSceneCallback.run();

        glColorMask(true, true, true, true);
    }

    public float getIpd() { return ipd; }
    public void setIpd(float ipd) { this.ipd = ipd; }
}