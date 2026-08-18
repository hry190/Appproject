import { ScrollView, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';

const STATES = [
  { name: '偶得', desc: '发现秘籍残页' },
  { name: '习得', desc: '完成原理试炼' },
  { name: '悟得', desc: '完成迁移创作' },
] as const;

/**
 * 一级导航：修炼 — AI 知识学习模块。
 * 对应策划书 §六.2：学生阅读漫画、进行预测、完成互动试炼并解锁秘籍。
 * 秘籍三态：偶得 → 习得 → 悟得。
 */
export default function XiulianTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">修炼</ThemedText>
        <ThemedText style={styles.lead}>
          AI 知识学习模块。学生阅读漫画、预测原因、完成试炼、解锁秘籍。
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.section}>
          <ThemedText style={styles.sectionTitle}>秘籍三态</ThemedText>
          {STATES.map((s) => (
            <ThemedText key={s.name} type="default">
              <ThemedText type="smallBold">{s.name}</ThemedText>
              <ThemedText themeColor="textSecondary">{` — ${s.desc}`}</ThemedText>
            </ThemedText>
          ))}
        </ThemedView>

        <ThemedText type="small" style={styles.note}>
          MVP 占位：完整章节内容接入《百炼识物诀》后展示。
        </ThemedText>
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
    gap: Spacing.two,
  },
  sectionTitle: { marginBottom: Spacing.one, fontWeight: '600' },
  note: { fontStyle: 'italic', opacity: 0.7 },
});
