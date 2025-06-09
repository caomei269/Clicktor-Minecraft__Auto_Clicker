package com.connector.mouse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoClickerConfig {
    // 点击设置
    public static boolean leftClick = true; // true为左键，false为右键
    public static int clickDuration = 50; // 点击持续时间（毫秒）
    public static int clickInterval = 100; // 点击间隔（毫秒）
    public static int clickCount = -1; // 点击次数，-1表示无限
    
    // 多人游戏限制
    public static final int MULTIPLAYER_MIN_INTERVAL = 1000; // 多人游戏最小间隔（毫秒）
    
    // 随机间隔设置
    public static boolean randomInterval = false; // 是否启用随机间隔
    public static final int RANDOM_MIN_OFFSET = 50; // 随机偏移最小值（毫秒）
    public static final int RANDOM_MAX_OFFSET = 300; // 随机偏移最大值（毫秒）
    
    // 启停快捷键设置
    public static int toggleKey = GLFW.GLFW_KEY_F1; // 默认F1
    public static boolean isMouseKey = false; // 是否为鼠标按键
    public static boolean isLongPress = false; // 是否为长按模式
    
    // 运行状态
    public static boolean isRunning = false;
    public static boolean isEnabled = false;
    
    // 计数器
    public static int currentClickCount = 0;
    
    // 重置计数器
    public static void resetClickCount() {
        currentClickCount = 0;
    }
    
    // 检查是否应该停止点击
    public static boolean shouldStopClicking() {
        return clickCount > 0 && currentClickCount >= clickCount;
    }
    
    // 获取按键名称
    public static String getKeyName() {
        if (isMouseKey) {
            switch (toggleKey) {
                case GLFW.GLFW_MOUSE_BUTTON_4:
                    return "Mouse 4";
                case GLFW.GLFW_MOUSE_BUTTON_5:
                    return "Mouse 5";
                default:
                    return "Mouse " + toggleKey;
            }
        } else {
            return InputUtil.fromKeyCode(toggleKey, 0).getLocalizedText().getString();
        }
    }
    
    // 检测是否在多人游戏环境中
    public static boolean isInMultiplayer() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        return client.getCurrentServerEntry() != null || 
               (client.getNetworkHandler() != null && !client.isInSingleplayer());
    }
    
    // 获取有效的点击间隔（考虑多人游戏限制和随机间隔）
    public static int getEffectiveClickInterval() {
        int baseInterval = clickInterval;
        
        // 应用多人游戏限制
        if (isInMultiplayer()) {
            baseInterval = Math.max(baseInterval, MULTIPLAYER_MIN_INTERVAL);
        }
        
        // 应用随机间隔
        if (randomInterval) {
            // 生成-300到+300之间的随机偏移（包含50-300的正负范围）
            int randomOffset = (int) (Math.random() * (RANDOM_MAX_OFFSET - RANDOM_MIN_OFFSET + 1) + RANDOM_MIN_OFFSET);
            // 随机决定是增加还是减少
            if (Math.random() < 0.5) {
                randomOffset = -randomOffset;
            }
            baseInterval += randomOffset;
            
            // 确保间隔不会小于1毫秒
            baseInterval = Math.max(1, baseInterval);
            
            // 如果在多人游戏中，确保不会低于最小限制
            if (isInMultiplayer()) {
                baseInterval = Math.max(baseInterval, MULTIPLAYER_MIN_INTERVAL);
            }
        }
        
        return baseInterval;
    }
    
    // 配置文件路径
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("mouseconnector.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 配置数据类
    public static class ConfigData {
        public boolean leftClick = true;
        public int clickDuration = 50;
        public int clickInterval = 100;
        public int clickCount = -1;
        public int toggleKey = GLFW.GLFW_KEY_F1;
        public boolean isMouseKey = false;
        public boolean isLongPress = false;
        public boolean isEnabled = false;
        public boolean randomInterval = false;
    }
    
    // 保存配置到文件
    public static void saveConfig() {
        try {
            ConfigData config = new ConfigData();
            config.leftClick = leftClick;
            config.clickDuration = clickDuration;
            config.clickInterval = clickInterval;
            config.clickCount = clickCount;
            config.toggleKey = toggleKey;
            config.isMouseKey = isMouseKey;
            config.isLongPress = isLongPress;
            config.isEnabled = isEnabled;
            config.randomInterval = randomInterval;
            
            String json = GSON.toJson(config);
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.write(CONFIG_FILE, json.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    
    // 从文件加载配置
    public static void loadConfig() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                String json = new String(Files.readAllBytes(CONFIG_FILE));
                ConfigData config = GSON.fromJson(json, ConfigData.class);
                
                if (config != null) {
                    leftClick = config.leftClick;
                    clickDuration = config.clickDuration;
                    clickInterval = config.clickInterval;
                    clickCount = config.clickCount;
                    toggleKey = config.toggleKey;
                    isMouseKey = config.isMouseKey;
                    isLongPress = config.isLongPress;
                    isEnabled = config.isEnabled;
                    randomInterval = config.randomInterval;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
}