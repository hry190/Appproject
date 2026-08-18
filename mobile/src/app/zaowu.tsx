import { ScrollView, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';

/**
 * 一级导航：造物 — 神机造物 Agent 入口。
 * 对应策划书 §六.3：以创作教练的方式辅助学生进行绘画、漫画或短视频创作。
 * AI 不是一键生成器，而是引导学生思考和修改的创作教练。
 */
export default function ZaowuTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">造物</ThemedText>
        <ThemedText style={styles.lead}>
          神机造物 Agent —— 以提问引导学生构思与修改的创作教练。
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.section}>
          <ThemedText style={styles.sectionTitle}>造物原则</ThemedText>
          <ThemedText type="default">· 一次只问一个引导问题</ThemedText>
          <ThemedText type="default">· 不代替学生完成全部创作</ThemedText>
          <ThemedText type="default">· 记录学生原创部分、AI 辅助部分与修改过程</ThemedText>
        </ThemedView>

        <ThemedView type="backgroundSelected" style={styles.cta}>
          <ThemedText type="default" style={styles.ctaText}>
            MVP 占位：开始造物按钮 + 八步引导式对话
          </ThemedText>
        </ThemedView>
      </ScrollView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: {
    padding: Spacing.four,
    gap: Spacing.three,
    maxWidth: 800,
    alignSelf: 'center',
  },
  lead: { lineHeight: 24 },
  section: {
    padding: Spacing.three,
    borderRadius: Spacing.three,
    gap: Spacing.one,
  },
  sectionTitle: { marginBottom: Spacing.one, fontWeight: '600' },
  cta: {
    padding: Spacing.three,
    borderRadius: Spacing.three,
    alignItems: 'center',
  },
  ctaText: { fontStyle: 'italic' },
});
