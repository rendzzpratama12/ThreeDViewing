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

public class ProjectionTaxonomyManager {
    public enum ProjectionType {
        ORTHOGRAPHIC,
        ISOMETRIC,
        OBLIQUE_CAVALIER,
        PERSPECTIVE_1POINT,
        PERSPECTIVE_2POINT,
        PERSPECTIVE_3POINT
    }

    public static void applyProjection(ProjectionType type, float aspect) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        switch (type) {
            case ORTHOGRAPHIC:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                break;
            case ISOMETRIC:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                glRotatef(35.264f, 1.0f, 0.0f, 0.0f);
                glRotatef(45.0f, 0.0f, 1.0f, 0.0f);
                break;
            case OBLIQUE_CAVALIER:
                glOrtho(-3.0 * aspect, 3.0 * aspect, -3.0, 3.0, 1.0, 20.0);
                float[] shearMatrix = {
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.5f, 0.5f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
                };
                glMultMatrixf(shearMatrix);
                break;
            case PERSPECTIVE_1POINT:
            case PERSPECTIVE_2POINT:
            case PERSPECTIVE_3POINT:
                gluPerspective(45.0f, aspect, 0.1f, 50.0f);
                break;
        }
    }

    private static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        float f = 1.0f / (float) Math.tan(Math.toRadians(fovy) / 2.0);
        float[] m = new float[16];
        m[0] = f / aspect;
        m[5] = f;
        m[10] = (zFar + zNear) / (zNear - zFar);
        m[11] = -1.0f;
        m[14] = (2.0f * zFar * zNear) / (zNear - zFar);
        glMultMatrixf(m);
    }
}