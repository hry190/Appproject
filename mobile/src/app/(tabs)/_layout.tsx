import { DarkTheme, DefaultTheme, Stack, ThemeProvider } from 'expo-router';
import { useColorScheme } from 'react-native';

/**
 * (tabs) 组布局:主题 provider + Stack(无 header,无 tab bar)。
 *
 * 旧的 5-tab 底部导航(app-tabs.tsx)已删除。文件在 (tabs) 下的几个
 * 页面仍然可达,但用户需从其他入口跳转 —首页本身只有 4 个快捷键。
 */
export default function TabsLayout() {
  const colorScheme = useColorScheme();
  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <Stack
        screenOptions={{
          headerShown: false,
          animation: 'none',
        }}
      />
    </ThemeProvider>
  );
}