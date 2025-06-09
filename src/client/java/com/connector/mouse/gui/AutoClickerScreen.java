package com.connector.mouse.gui;

import com.connector.mouse.config.AutoClickerConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class AutoClickerScreen extends Screen {
    private TextFieldWidget clickDurationField;
    private TextFieldWidget clickIntervalField;
    private TextFieldWidget clickCountField;
    private ButtonWidget clickTypeButton;
    private ButtonWidget toggleKeyButton;
    private ButtonWidget keyModeButton;
    private ButtonWidget randomIntervalButton;
    private ButtonWidget enableButton;
    
    private boolean waitingForKey = false;
    
    public AutoClickerScreen() {
        super(Text.translatable("gui.mouseconnector.autoclicker.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 动态计算布局参数以适应不同屏幕尺寸
        int centerX = this.width / 2;
        int minStartY = 40;
        int maxStartY = Math.max(minStartY, (this.height - 250) / 2); // 确保有足够空间显示所有元素
        int startY = Math.min(maxStartY, 60);
        int spacing = Math.max(20, Math.min(25, (this.height - startY - 80) / 9)); // 动态间距
        int buttonWidth = Math.min(200, this.width - 40); // 按钮宽度适应屏幕
        int fieldWidth = Math.min(90, (buttonWidth - 10) / 2); // 输入框宽度
        
        // 点击类型按钮
        clickTypeButton = ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.click_type", 
                Text.translatable(AutoClickerConfig.leftClick ? "gui.mouseconnector.autoclicker.left" : "gui.mouseconnector.autoclicker.right")),
            button -> {
                AutoClickerConfig.leftClick = !AutoClickerConfig.leftClick;
                button.setMessage(Text.translatable("gui.mouseconnector.autoclicker.click_type", 
                    Text.translatable(AutoClickerConfig.leftClick ? "gui.mouseconnector.autoclicker.left" : "gui.mouseconnector.autoclicker.right")));
            }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, 20).build();
        this.addDrawableChild(clickTypeButton);
        
        // 点击持续时间
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.duration"),
            button -> {}
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth / 2, 20).build());
        
        clickDurationField = new TextFieldWidget(this.textRenderer, centerX + 5, startY + spacing, fieldWidth, 20, Text.translatable("gui.mouseconnector.autoclicker.duration"));
        clickDurationField.setText(String.valueOf(AutoClickerConfig.clickDuration));
        clickDurationField.setMaxLength(5);
        this.addDrawableChild(clickDurationField);
        
        // 点击间隔
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.interval"),
            button -> {}
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth / 2, 20).build());
        
        clickIntervalField = new TextFieldWidget(this.textRenderer, centerX + 5, startY + spacing * 2, fieldWidth, 20, Text.translatable("gui.mouseconnector.autoclicker.interval"));
        clickIntervalField.setText(String.valueOf(AutoClickerConfig.clickInterval));
        clickIntervalField.setMaxLength(5);
        this.addDrawableChild(clickIntervalField);
        
        // 点击次数
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.count"),
            button -> {}
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth / 2, 20).build());
        
        clickCountField = new TextFieldWidget(this.textRenderer, centerX + 5, startY + spacing * 3, fieldWidth, 20, Text.translatable("gui.mouseconnector.autoclicker.count"));
        clickCountField.setText(String.valueOf(AutoClickerConfig.clickCount));
        clickCountField.setMaxLength(6);
        this.addDrawableChild(clickCountField);
        
        // 快捷键设置
        toggleKeyButton = ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.toggle_key", AutoClickerConfig.getKeyName()),
            button -> {
                waitingForKey = true;
                button.setMessage(Text.translatable("gui.mouseconnector.autoclicker.press_key").formatted(Formatting.YELLOW));
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, 20).build();
        this.addDrawableChild(toggleKeyButton);
        
        // 按键模式
        keyModeButton = ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.mode", 
                Text.translatable(AutoClickerConfig.isLongPress ? "gui.mouseconnector.autoclicker.hold" : "gui.mouseconnector.autoclicker.toggle")),
            button -> {
                AutoClickerConfig.isLongPress = !AutoClickerConfig.isLongPress;
                button.setMessage(Text.translatable("gui.mouseconnector.autoclicker.mode", 
                    Text.translatable(AutoClickerConfig.isLongPress ? "gui.mouseconnector.autoclicker.hold" : "gui.mouseconnector.autoclicker.toggle")));
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, 20).build();
        this.addDrawableChild(keyModeButton);
        
        // 随机间隔开关
        randomIntervalButton = ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.random_interval", 
                Text.translatable(AutoClickerConfig.randomInterval ? "gui.mouseconnector.autoclicker.enabled" : "gui.mouseconnector.autoclicker.disabled")),
            button -> {
                AutoClickerConfig.randomInterval = !AutoClickerConfig.randomInterval;
                button.setMessage(Text.translatable("gui.mouseconnector.autoclicker.random_interval", 
                    Text.translatable(AutoClickerConfig.randomInterval ? "gui.mouseconnector.autoclicker.enabled" : "gui.mouseconnector.autoclicker.disabled")));
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, 20).build();
        this.addDrawableChild(randomIntervalButton);
        
        // 启用/禁用按钮
        enableButton = ButtonWidget.builder(
            Text.translatable(AutoClickerConfig.isEnabled ? "gui.mouseconnector.autoclicker.disable" : "gui.mouseconnector.autoclicker.enable"),
            button -> {
                AutoClickerConfig.isEnabled = !AutoClickerConfig.isEnabled;
                if (!AutoClickerConfig.isEnabled) {
                    AutoClickerConfig.isRunning = false;
                }
                button.setMessage(Text.translatable(AutoClickerConfig.isEnabled ? "gui.mouseconnector.autoclicker.disable" : "gui.mouseconnector.autoclicker.enable"));
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, 20).build();
        this.addDrawableChild(enableButton);
        
        // 应用设置按钮和关闭按钮
        int bottomButtonWidth = (buttonWidth - 10) / 2;
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.apply"),
            button -> {
                applySettings();
            }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 8, bottomButtonWidth, 20).build());
        
        // 关闭按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.mouseconnector.autoclicker.close"),
            button -> {
                applySettings();
                this.close();
            }
        ).dimensions(centerX - buttonWidth / 2 + bottomButtonWidth + 10, startY + spacing * 8, bottomButtonWidth, 20).build());
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                AutoClickerConfig.toggleKey = keyCode;
                AutoClickerConfig.isMouseKey = false;
                toggleKeyButton.setMessage(Text.translatable("gui.mouseconnector.autoclicker.toggle_key", AutoClickerConfig.getKeyName()));
            }
            waitingForKey = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (waitingForKey) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_4 || button == GLFW.GLFW_MOUSE_BUTTON_5) {
                AutoClickerConfig.toggleKey = button;
                AutoClickerConfig.isMouseKey = true;
                toggleKeyButton.setMessage(Text.translatable("gui.mouseconnector.autoclicker.toggle_key", AutoClickerConfig.getKeyName()));
            }
            waitingForKey = false;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void applySettings() {
        try {
            AutoClickerConfig.clickDuration = Math.max(1, Integer.parseInt(clickDurationField.getText()));
            AutoClickerConfig.clickInterval = Math.max(1, Integer.parseInt(clickIntervalField.getText()));
            AutoClickerConfig.clickCount = Integer.parseInt(clickCountField.getText());
            AutoClickerConfig.saveConfig();
            
            // 显示保存成功消息
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.translatable("message.mouseconnector.config_saved"), true);
            }
        } catch (NumberFormatException e) {
            // 如果输入无效，保持原值
            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.translatable("message.mouseconnector.config_error"), true);
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        // 动态计算标题位置
        int titleY = Math.max(10, Math.min(20, (this.height > 300) ? 20 : 10));
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, titleY, 0xFFFFFF);
        
        // 动态计算状态信息位置
        int statusY = Math.max(this.height - 40, this.height - 30);
        Text statusText = Text.translatable("gui.mouseconnector.autoclicker.status", 
            Text.translatable(AutoClickerConfig.isEnabled ? 
                (AutoClickerConfig.isRunning ? "gui.mouseconnector.autoclicker.running" : "gui.mouseconnector.autoclicker.ready") : 
                "gui.mouseconnector.autoclicker.disabled"));
        context.drawCenteredTextWithShadow(this.textRenderer, statusText, this.width / 2, statusY, 
            AutoClickerConfig.isRunning ? 0x00FF00 : (AutoClickerConfig.isEnabled ? 0xFFFF00 : 0xFF0000));
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}