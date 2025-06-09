package com.connector.mouse;

import com.connector.mouse.config.AutoClickerConfig;
import com.connector.mouse.gui.AutoClickerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ConnectorClient implements ClientModInitializer {
	private static KeyBinding openGuiKey;
	private AutoClickerManager autoClickerManager;

	@Override
	public void onInitializeClient() {
		// 初始化配置
		AutoClickerConfig.loadConfig();
		
		// 初始化自动点击器管理器
		autoClickerManager = AutoClickerManager.getInstance();
		
		// 注册打开GUI的按键绑定（H键）
		openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.mouseconnector.open_gui", // 翻译键
			InputUtil.Type.KEYSYM, // 键盘按键
			GLFW.GLFW_KEY_H, // H键
			"category.mouseconnector.general" // 分类
		));
		
		// 注册客户端tick事件
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// 处理打开GUI的按键
			while (openGuiKey.wasPressed()) {
				client.setScreen(new AutoClickerScreen());
			}
			
			// 处理自动点击器逻辑
			autoClickerManager.tick();
		});
		
		Connector.LOGGER.info("Auto Clicker Client initialized!");
	}
}