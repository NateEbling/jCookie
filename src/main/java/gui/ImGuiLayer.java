package gui;

import core.Window;
import editor.PickingTexture;
import editor.PropertiesWindow;
import listeners.KeyListener;
import listeners.MouseListener;
import scenes.Scene;
import editor.GameViewWindow;

import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGui;
import imgui.type.ImBoolean;

import static org.lwjgl.glfw.GLFW.*;

public class ImGuiLayer {

    public ImGuiImplGlfw imGuiGlfw;
    public ImGuiImplGl3 imGuiGl3;
    private GameViewWindow gameViewWindow;
    private PropertiesWindow propertiesWindow;

    public ImGuiLayer(long glfwWindow, PickingTexture pickingTexture) {
        this.gameViewWindow = new GameViewWindow();
        this.propertiesWindow = new PropertiesWindow(pickingTexture);
    }

    public void initImGui(long glfwWindow) {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable); // this one needs to be "setConfigFlags"
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // Callbacks
        glfwSetKeyCallback(glfwWindow, this::keyCallback);
        glfwSetCharCallback(glfwWindow, this::charCallback);
        glfwSetMouseButtonCallback(glfwWindow, this::mouseCallback);
        glfwSetScrollCallback(glfwWindow, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float)xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float)yOffset);
            MouseListener.mouseScrollCallback(w, xOffset, yOffset);
        });
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

        setupDockspace();

        ImGui.showDemoWindow();

        currentScene.imgui();

        gameViewWindow.imgui();

        propertiesWindow.update(dt, currentScene);
        propertiesWindow.imgui();

        ImGui.end();
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

        if (gameViewWindow.getWantCaptureMouse()) {
            MouseListener.mouseButtonCallback(window, button, action, mods);
        }

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

    private void setupDockspace() {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(Window.getWidth(), Window.getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove
                | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("Dockspace demo", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        // Dockspace
        ImGui.dockSpace(ImGui.getID("Dockspace"));
    }
}