import { Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';

// Keep the native splash visible until our in-app splash page is mounted.
SplashScreen.preventAutoHideAsync();

/**
 * Root Stack: splash → login → (tabs)
 * - splash: panda 开屏页，整屏点击进入
 * - login: 登录页，提交后进入 Tab
 * - (tabs): 5 个 Tab 主界面
 */
export default function RootLayout() {
  return (
    <Stack
      initialRouteName="splash"
      screenOptions={{ headerShown: false }}>
      <Stack.Screen name="splash" options={{ headerShown: false }} />
      <Stack.Screen name="login" options={{ headerShown: false }} />
      <Stack.Screen name="register" options={{ headerShown: false }} />
      <Stack.Screen name="forgot" options={{ headerShown: false }} />
      <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
    </Stack>
  );
}
