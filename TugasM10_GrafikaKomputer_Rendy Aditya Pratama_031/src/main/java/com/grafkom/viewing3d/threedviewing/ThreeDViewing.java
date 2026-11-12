/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.grafkom.viewing3d.threedviewing;

/**
 *
 * @author Rendy Aditya
 */
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ThreeDViewing {
    private long window;
    private int width = 800;
    private int height = 600;

    // Menyesuaikan dengan Mesh3D Modern OpenGL (Modul 11)
    private Mesh3D mesh = Mesh3D.createSphere(1.0f, 36, 36, true);
    private Camera3D camera = new Camera3D(new Vector3f(0, 0, 5), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
    private StereoAnaglyphRenderer stereoRenderer = new StereoAnaglyphRenderer();
    private ProjectionTaxonomyManager.ProjectionType currentProjection = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;

    private boolean isStereoEnabled = false;
    private float cameraRadius = 5.0f;
    private float azimuth = 0.0f;
    private float elevation = 0.0f;

    public void run() {
        init();
        loop();

        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Gagal inisialisasi GLFW");

        window = glfwCreateWindow(width, height, "3D Viewing Visualizer - Modul 10/11 Compatibility", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Gagal membuat jendela GLFW");

        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) currentProjection = ProjectionTaxonomyManager.ProjectionType.ORTHOGRAPHIC;
                if (key == GLFW_KEY_2) currentProjection = ProjectionTaxonomyManager.ProjectionType.ISOMETRIC;
                if (key == GLFW_KEY_3) currentProjection = ProjectionTaxonomyManager.ProjectionType.OBLIQUE_CAVALIER;
                if (key == GLFW_KEY_4) currentProjection = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_1POINT;
                if (key == GLFW_KEY_5) currentProjection = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_2POINT;
                if (key == GLFW_KEY_6) currentProjection = ProjectionTaxonomyManager.ProjectionType.PERSPECTIVE_3POINT;
                if (key == GLFW_KEY_S) isStereoEnabled = !isStereoEnabled;
                if (key == GLFW_KEY_LEFT) azimuth -= 5.0f;
                if (key == GLFW_KEY_RIGHT) azimuth += 5.0f;
                if (key == GLFW_KEY_UP) elevation += 5.0f;
                if (key == GLFW_KEY_DOWN) elevation -= 5.0f;
            }
        });

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
    }

    private void loop() {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            float aspect = (float) width / (float) height;
            ProjectionTaxonomyManager.applyProjection(currentProjection, aspect);

            camera.updateOrbitalPosition(cameraRadius, azimuth, elevation);
            Matrix4f viewMatrix = camera.calculateLookAtMatrix();

            if (isStereoEnabled) {
                stereoRenderer.renderAnaglyph(this::renderScene);
            } else {
                glMatrixMode(GL_MODELVIEW);
                float[] m = new float[16];
                viewMatrix.get(m);
                glLoadMatrixf(m);
                renderScene();
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void renderScene() {
        // Menggunakan method draw() dari Mesh3D Modern OpenGL
        mesh.draw();
    }

    public static void main(String[] args) {
        new ThreeDViewing().run();
    }
}