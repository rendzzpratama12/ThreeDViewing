package com.grafkom.viewing3d.threedviewing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.lwjgl.opengl.GL20.*;

public class ShaderLoader {
    public static int loadShader(String vertexPath, String fragmentPath) {
        String vertCode = loadFile("src/main/resources/" + vertexPath);
        String fragCode = loadFile("src/main/resources/" + fragmentPath);

        int vertShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertShader, vertCode);
        glCompileShader(vertShader);
        checkCompile(vertShader, "VERTEX");

        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragShader, fragCode);
        glCompileShader(fragShader);
        checkCompile(fragShader, "FRAGMENT");

        int program = glCreateProgram();
        glAttachShader(program, vertShader);
        glAttachShader(program, fragShader);
        glLinkProgram(program);
        checkLink(program);

        glDeleteShader(vertShader);
        glDeleteShader(fragShader);
        return program;
    }

    private static String loadFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca file shader: " + path, e);
        }
    }

    private static void checkCompile(int shader, String type) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Gagal kompilasi " + type + " Shader: " + glGetShaderInfoLog(shader, 1024));
        }
    }

    private static void checkLink(int program) {
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Gagal linking Shader Program: " + glGetProgramInfoLog(program, 1024));
        }
    }
}