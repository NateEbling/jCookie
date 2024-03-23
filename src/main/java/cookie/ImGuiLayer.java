package cookie;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiFreeTypeBuilderFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGui;
import scenes.Scene;

import static org.lwjgl.glfw.GLFW.*;

public class ImGuiLayer {
    public ImGuiImplGlfw imGuiGlfw;
    public ImGuiImplGl3 imGuiGl3;

    public void initImGui(long glfwWindow) {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // Callbacks
        glfwSetKeyCallback(glfwWindow, this::keyCallback);
        glfwSetCharCallback(glfwWindow, this::charCallback);
        glfwSetMouseButtonCallback(glfwWindow, this::mouseCallback);
        glfwSetScrollCallback(glfwWindow, this::scrollCallback);
        glfwSetWindowSizeCallback(glfwWindow, this::resizeCallback);

        // Font
        final ImFontAtlas fontAtlas = io.getFonts();
        final ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setGlyphRanges(fontAtlas.getGlyphRangesDefault());
        fontConfig.setPixelSnapH(true);
        fontAtlas.addFontFromFileTTF("assets/fonts/8bitOperator.ttf", 14, fontConfig);
        fontConfig.destroy(); // Not needed after all the fonts are added
        fontAtlas.setFlags(ImGuiFreeTypeBuilderFlags.LightHinting);
        fontAtlas.build();

        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init("#version 330 core");
    }

    public void update(float dt, Scene currentScene) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        currentScene.sceneImgui();

        ImGui.showDemoWindow();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }

    private void mouseCallback(long window, int button, int action, int mods) {
        final boolean[] mouseDown = new boolean[5];

        mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
        mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
        mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
        mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
        mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

        ImGuiIO io = ImGui.getIO();
        io.setMouseDown(mouseDown);

        if (!io.getWantCaptureMouse() && mouseDown[1]) {
            ImGui.setWindowFocus(null);
        }
        MouseListener.mouseButtonCallback(window, button, action, mods);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        final ImGuiIO io = ImGui.getIO();
        if (action == GLFW_PRESS) {
            io.setKeysDown(key, true);
        } else if (action == GLFW_RELEASE) {
            io.setKeysDown(key, false);
        }

        io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
        io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
        io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
        io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));

        KeyListener.keyCallback(window, key, scancode, action, mods);
    }

    private void charCallback(long window, int c) {
        ImGuiIO io = ImGui.getIO();
        if (c != GLFW_KEY_DELETE)
            io.addInputCharacter(c);
    }

    private void scrollCallback(long window, double xOffset, double yOffset) {
        ImGuiIO io = ImGui.getIO();
        io.setMouseWheelH((float)xOffset + io.getMouseWheelH());
        io.setMouseWheel((float)yOffset + io.getMouseWheel());
        MouseListener.mouseScrollCallback(window, xOffset, yOffset);
    }

    private void resizeCallback(long window, int width, int height) {
        Window.setWidth(width);
        Window.setHeight(height);
    }
}