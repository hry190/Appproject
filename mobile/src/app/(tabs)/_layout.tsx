import { DarkTheme, DefaultTheme, ThemeProvider } from 'expo-router';
import { useColorScheme } from 'react-native';

import AppTabs from '@/components/app-tabs';

/**
 * 路由组 (tabs) 的布局：在该组内渲染 NativeTabs。
 * 主题 provider 只在这里挂，因为 splash/login 页是独立样式。
 */
export default function TabsLayout() {
  const colorScheme = useColorScheme();
  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <AppTabs />
    </ThemeProvider>
  );
}