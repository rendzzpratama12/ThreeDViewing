package com.grafkom.viewing3d.threedviewing;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VisualRealismSuite {
    private long window;
    private int width = 1200, height = 900;

    // Shader IDs
    private int phongProgram, shadowProgram;

    // Objek 3D & Lantai
    private Mesh3D sphereSmooth, sphereFlat, groundPlane;
    private Texture2D texture;
    private ShadowMapFBO shadowFBO;

    // Interaktif Toggles (Sesuai Spesifikasi Tugas Mandiri)
    private boolean useBlinnPhong = true;   // Key 'B'
    private boolean useFlatShading = false;  // Key 'F'
    private boolean enableDepthTest = true;  // Key 'Z'
    private boolean enableCulling = true;    // Key 'C'
    private boolean useNearestFilter = false;// Key 'T'
    private boolean useShadows = true;       // Key 'S'

    private Vector3f lightPos = new Vector3f(-4.0f, 6.0f, -4.0f);

    public void run() {
        init();
        loop();

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Gagal menginisialisasi GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(width, height, "3D Visual Realism Suite - Minggu 11", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Gagal membuat window GLFW");

        // Key Callback untuk Kontrol Interaktif Tugas Mandiri
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW_PRESS) {
                    if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(window, true);
                    if (key == GLFW_KEY_B) { useBlinnPhong = !useBlinnPhong; System.out.println("Mode Blinn-Phong: " + useBlinnPhong); }
                    if (key == GLFW_KEY_F) { useFlatShading = !useFlatShading; System.out.println("Mode Flat Shading: " + useFlatShading); }
                    if (key == GLFW_KEY_Z) { enableDepthTest = !enableDepthTest; HSRManager.setupDepthTest(enableDepthTest); System.out.println("Z-Buffer Depth Test: " + enableDepthTest); }
                    if (key == GLFW_KEY_C) { enableCulling = !enableCulling; HSRManager.setupBackFaceCulling(enableCulling); System.out.println("Back-Face Culling: " + enableCulling); }
                    if (key == GLFW_KEY_T) { useNearestFilter = !useNearestFilter; texture.setFilteringMode(useNearestFilter); System.out.println("Texture Filtering Nearest: " + useNearestFilter); }
                    if (key == GLFW_KEY_S) { useShadows = !useShadows; System.out.println("Shadow Mapping: " + useShadows); }
                }
            }
        });

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            glfwSetWindowPos(window, (1920 - pWidth.get(0)) / 2, (1080 - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();

        // Setup HSR Awal (Praktikum 2)
        HSRManager.setupDepthTest(true);
        HSRManager.setupBackFaceCulling(true);
        glClearColor(0.1f, 0.12f, 0.18f, 1.0f);

        // Kompilasi Shader
        phongProgram = ShaderLoader.loadShader("shaders/phong.vert", "shaders/phong.frag");
        shadowProgram = ShaderLoader.loadShader("shaders/shadow_depth.vert", "shaders/shadow_depth.frag");

        // Inisialisasi Mesh, Texture, dan FBO Bayangan
        sphereSmooth = Mesh3D.createSphere(1.0f, 36, 36, true);
        sphereFlat = Mesh3D.createSphere(1.0f, 18, 18, false);
        groundPlane = Mesh3D.createGroundPlane(10.0f);

        // Pastikan Anda menaruh file gambar di src/main/resources/textures/texture.png
        texture = new Texture2D("src/main/resources/textures/texture.png");
        shadowFBO = new ShadowMapFBO();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            float time = (float) glfwGetTime();

            // 1. RENDER PASS 1: Depth Pass dari Sudut Pandang Cahaya (Shadow Mapping)
            shadowFBO.bindForWriting();
            glUseProgram(shadowProgram);

            Matrix4f lightProjection = new Matrix4f().ortho(-10f, 10f, -10f, 10f, 1.0f, 20.0f);
            Matrix4f lightView = new Matrix4f().lookAt(lightPos, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
            Matrix4f lightSpaceMatrix = new Matrix4f().mul(lightProjection).mul(lightView);

            int lightSpaceLoc = glGetUniformLocation(shadowProgram, "lightSpaceMatrix");
            glUniformMatrix4fv(lightSpaceLoc, false, lightSpaceMatrix.get(new float[16]));

            // Render objek ke Depth Map
            renderSceneForShadows(shadowProgram);
            shadowFBO.unbind(width, height);

            // 2. RENDER PASS 2: Lighting Pass ke Layar Utama
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(phongProgram);

            // Matriks Kamera & Proyeksi
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0f), (float) width / height, 0.1f, 100.0f);
            Matrix4f view = new Matrix4f().lookAt(new Vector3f(0, 2, 5), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

            glUniformMatrix4fv(glGetUniformLocation(phongProgram, "projection"), false, projection.get(new float[16]));
            glUniformMatrix4fv(glGetUniformLocation(phongProgram, "view"), false, view.get(new float[16]));
            glUniformMatrix4fv(glGetUniformLocation(phongProgram, "lightSpaceMatrix"), false, lightSpaceMatrix.get(new float[16]));

            // Kirim Uniform Pencahayaan
            glUniform3f(glGetUniformLocation(phongProgram, "lightPos"), lightPos.x, lightPos.y, lightPos.z);
            glUniform3f(glGetUniformLocation(phongProgram, "viewPos"), 0f, 2f, 5f);
            glUniform3f(glGetUniformLocation(phongProgram, "lightColor"), 1.0f, 1.0f, 1.0f);

            // Kirim Status Toggles Interaktif
            glUniform1i(glGetUniformLocation(phongProgram, "useBlinnPhong"), useBlinnPhong ? 1 : 0);
            glUniform1i(glGetUniformLocation(phongProgram, "useFlatShading"), useFlatShading ? 1 : 0);
            glUniform1i(glGetUniformLocation(phongProgram, "useShadows"), useShadows ? 1 : 0);
            glUniform1i(glGetUniformLocation(phongProgram, "useTexture"), 0);

            // Bind Texture & Shadow Map
            texture.bind(0);
            glUniform1i(glGetUniformLocation(phongProgram, "textureSampler"), 0);

            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, shadowFBO.getDepthTextureID());
            glUniform1i(glGetUniformLocation(phongProgram, "shadowMap"), 1);

            // Render Objek Utama (Sphere berputar)
            Matrix4f model = new Matrix4f().translate(0.0f, 0.0f, 0.0f).rotateY(time * 0.5f);
            glUniformMatrix4fv(glGetUniformLocation(phongProgram, "model"), false, model.get(new float[16]));
            glUniform3f(glGetUniformLocation(phongProgram, "objectColor"), 0.2f, 0.6f, 0.9f);
            
            if (useFlatShading) {
                sphereFlat.draw();
            } else {
                sphereSmooth.draw();
            }

            // Render Bidang Lantai (Ground Plane)
            Matrix4f groundModel = new Matrix4f().translate(0.0f, -1.0f, 0.0f);
            glUniformMatrix4fv(glGetUniformLocation(phongProgram, "model"), false, groundModel.get(new float[16]));
            glUniform3f(glGetUniformLocation(phongProgram, "objectColor"), 0.7f, 0.7f, 0.7f);
            groundPlane.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void renderSceneForShadows(int shaderProgram) {
        // Render Sphere
        Matrix4f model = new Matrix4f().translate(0.0f, 0.0f, 0.0f).rotateY((float) glfwGetTime() * 0.5f);
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(new float[16]));
        if (useFlatShading) sphereFlat.draw(); else sphereSmooth.draw();

        // Render Ground Plane
        Matrix4f groundModel = new Matrix4f().translate(0.0f, -1.0f, 0.0f);
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, groundModel.get(new float[16]));
        groundPlane.draw();
    }

    public static void main(String[] args) {
        new VisualRealismSuite().run();
    }
}