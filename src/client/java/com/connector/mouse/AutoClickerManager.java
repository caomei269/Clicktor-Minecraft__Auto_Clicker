package com.connector.mouse;

import com.connector.mouse.config.AutoClickerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoClickerManager {
    private static AutoClickerManager instance;
    private final MinecraftClient client;
    private long lastClickTime = 0;
    private long clickStartTime = 0;
    private boolean isClicking = false;
    private boolean keyPressed = false;
    
    private AutoClickerManager() {
        this.client = MinecraftClient.getInstance();
    }
    
    public static AutoClickerManager getInstance() {
        if (instance == null) {
            instance = new AutoClickerManager();
        }
        return instance;
    }
    
    public void tick() {
        if (!AutoClickerConfig.isEnabled || client.player == null) {
            return;
        }
        
        handleToggleKey();
        
        if (AutoClickerConfig.isRunning) {
            handleAutoClick();
        }
    }
    
    private void handleToggleKey() {
        boolean currentKeyState = isKeyPressed();
        
        if (AutoClickerConfig.isLongPress) {
            // 长按模式：按住时运行，松开时停止
            if (currentKeyState && !keyPressed) {
                startAutoClicker();
            } else if (!currentKeyState && keyPressed) {
                stopAutoClicker();
            }
        } else {
            // 切换模式：按下时切换状态
            if (currentKeyState && !keyPressed) {
                if (AutoClickerConfig.isRunning) {
                    stopAutoClicker();
                } else {
                    startAutoClicker();
                }
            }
        }
        
        keyPressed = currentKeyState;
    }
    
    private boolean isKeyPressed() {
        if (AutoClickerConfig.isMouseKey) {
            return GLFW.glfwGetMouseButton(client.getWindow().getHandle(), AutoClickerConfig.toggleKey) == GLFW.GLFW_PRESS;
        } else {
            return InputUtil.isKeyPressed(client.getWindow().getHandle(), AutoClickerConfig.toggleKey);
        }
    }
    
    private void handleAutoClick() {
        long currentTime = System.currentTimeMillis();
        
        if (isClicking) {
            // 检查是否应该释放鼠标按键
            if (currentTime - clickStartTime >= AutoClickerConfig.clickDuration) {
                releaseMouse();
                isClicking = false;
                lastClickTime = currentTime;
                AutoClickerConfig.currentClickCount++;
                
                // 检查是否达到点击次数限制
                if (AutoClickerConfig.shouldStopClicking()) {
                    stopAutoClicker();
                    return;
                }
            }
        } else {
            // 检查是否应该开始新的点击（使用有效间隔，考虑多人游戏限制）
            if (currentTime - lastClickTime >= AutoClickerConfig.getEffectiveClickInterval()) {
                pressMouse();
                isClicking = true;
                clickStartTime = currentTime;
            }
        }
    }
    
    private void pressMouse() {
        if (client.currentScreen != null || client.player == null) {
            return; // 在GUI界面时或玩家为空时不执行点击
        }
        
        try {
            // 首先尝试使用反射调用私有的onMouseButton方法（开发环境）
            int button = AutoClickerConfig.leftClick ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            
            java.lang.reflect.Method onMouseButtonMethod = client.mouse.getClass().getDeclaredMethod(
                "onMouseButton", long.class, int.class, int.class, int.class);
            onMouseButtonMethod.setAccessible(true);
            onMouseButtonMethod.invoke(client.mouse, client.getWindow().getHandle(), button, GLFW.GLFW_PRESS, 0);
            
        } catch (Exception e) {
            // 反射失败时使用替代方案（生产环境）
            Connector.LOGGER.warn("Reflection failed, using alternative method: " + e.getMessage());
            simulateClickAlternative(true);
        }
    }
    
    private void releaseMouse() {
        if (client.currentScreen != null || client.player == null) {
            return;
        }
        
        try {
            // 首先尝试使用反射调用私有的onMouseButton方法（开发环境）
            int button = AutoClickerConfig.leftClick ? GLFW.GLFW_MOUSE_BUTTON_LEFT : GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            
            java.lang.reflect.Method onMouseButtonMethod = client.mouse.getClass().getDeclaredMethod(
                "onMouseButton", long.class, int.class, int.class, int.class);
            onMouseButtonMethod.setAccessible(true);
            onMouseButtonMethod.invoke(client.mouse, client.getWindow().getHandle(), button, GLFW.GLFW_RELEASE, 0);
            
        } catch (Exception e) {
            // 反射失败时使用替代方案（生产环境）
            Connector.LOGGER.warn("Reflection failed, using alternative method: " + e.getMessage());
            simulateClickAlternative(false);
        }
    }
    
    /**
     * 高效的键绑定状态控制方法
     * @param isPress true为按下，false为释放
     */
    private void simulateClickAlternative(boolean isPress) {
        if (client.currentScreen != null || client.player == null) {
            return;
        }
        
        try {
            // 直接使用键绑定状态控制，让客户端自己处理持续逻辑
            if (AutoClickerConfig.leftClick) {
                client.options.attackKey.setPressed(isPress);
                
                // 释放时清理方块挖掘进度
                if (!isPress && client.interactionManager != null) {
                    client.interactionManager.cancelBlockBreaking();
                }
            } else {
                client.options.useKey.setPressed(isPress);
                
                // 释放时停止物品使用
                if (!isPress && client.interactionManager != null && client.player.isUsingItem()) {
                    client.interactionManager.stopUsingItem(client.player);
                }
            }
        } catch (Exception e) {
            Connector.LOGGER.warn("Key binding simulation failed: " + e.getMessage());
        }
    }
    
    public void startAutoClicker() {
        if (!AutoClickerConfig.isEnabled) {
            return;
        }
        
        AutoClickerConfig.isRunning = true;
        AutoClickerConfig.resetClickCount();
        lastClickTime = System.currentTimeMillis();
        isClicking = false;
        
        if (client.player != null) {
            client.player.sendMessage(Text.translatable("message.mouseconnector.started"), true);
            
            // 如果在多人游戏中且间隔被调整，提示用户
            if (AutoClickerConfig.isInMultiplayer() && AutoClickerConfig.clickInterval < AutoClickerConfig.MULTIPLAYER_MIN_INTERVAL) {
                client.player.sendMessage(Text.translatable("message.mouseconnector.multiplayer_limit", AutoClickerConfig.MULTIPLAYER_MIN_INTERVAL), true);
            }
        }
    }
    
    public void stopAutoClicker() {
        AutoClickerConfig.isRunning = false;
        
        // 如果正在点击，立即释放鼠标并中断
        if (isClicking) {
            releaseMouse();
            isClicking = false;
        }
        
        if (client.player != null) {
            client.player.sendMessage(Text.translatable("message.mouseconnector.stopped", AutoClickerConfig.currentClickCount), true);
        }
    }
    
    public void toggle() {
        if (AutoClickerConfig.isRunning) {
            stopAutoClicker();
        } else {
            startAutoClicker();
        }
    }
    
    public boolean isRunning() {
        return AutoClickerConfig.isRunning;
    }
}