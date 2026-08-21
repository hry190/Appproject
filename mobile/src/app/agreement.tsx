import { useRouter } from 'expo-router';
import { Image, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import bgPaper from '@/assets/images/login/bg-paper.png';

const BG_CREAM = '#F5E8D4';
const TEXT_DARK = '#000000';

export default function AgreementScreen() {
  const router = useRouter();
  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <Image
        source={bgPaper}
        style={StyleSheet.absoluteFill}
        resizeMode="cover"
        // @ts-expect-error RN 此版本的 ImageProps 不含 pointerEvents prop
        pointerEvents="none"
      />
      <ScrollView contentContainerStyle={styles.scroll}>
        <View style={styles.header}>
          <Pressable
            onPress={() => (router.canGoBack() ? router.back() : router.replace('/login'))}
            hitSlop={12}
            accessibilityRole="button"
            accessibilityLabel="返回">
            <Text style={styles.back}>‹</Text>
          </Pressable>
          <Text style={styles.title}>用户协议</Text>
        </View>
        <View style={styles.body}>
          <Text style={styles.placeholder}>(用户协议正文占位)</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: BG_CREAM },
  scroll: { flexGrow: 1 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 24,
  },
  back: {
    fontSize: 36,
    color: TEXT_DARK,
    paddingHorizontal: 4,
  },
  title: {
    marginLeft: 8,
    fontSize: 28,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },
  body: {
    paddingHorizontal: 24,
    paddingTop: 32,
  },
  placeholder: {
    fontSize: 14,
    color: TEXT_DARK,
    lineHeight: 22,
    opacity: 0.6,
    fontFamily: 'Microsoft YaHei',
  },
});