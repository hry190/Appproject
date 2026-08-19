import { useEffect } from 'react';
import { Image, Pressable, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';

import { ThemedText } from '@/components/themed-text';

/**
 * 开屏页：铺满熊猫图，整屏可点击 → 进入 /login。
 * 挂载时调用 SplashScreen.hideAsync() 隐藏原生 splash，让出 JS 渲染。
 */
export default function SplashScreenPage() {
  const router = useRouter();

  useEffect(() => {
    SplashScreen.hideAsync().catch(() => {
      /* ignore: hideAsync may already have been called */
    });
  }, []);

  return (
    <Pressable
      style={styles.container}
      onPress={() => router.replace('/login')}
      accessibilityRole="button"
      accessibilityLabel="点击进入登录页">
      <Image
        source={require('@/assets/images/panda-launch.png')}
        style={styles.image}
        resizeMode="cover"
      />
      <ThemedText style={styles.hint}>点击进入</ThemedText>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  image: {
    width: '100%',
    height: '100%',
    position: 'absolute',
    inset: 0,
  },
  hint: {
    position: 'absolute',
    bottom: 80,
    opacity: 0.55,
    fontStyle: 'italic',
  },
});