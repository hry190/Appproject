import { ScrollView, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';

/**
 * 一级导航：江湖 — 项目首页与学习入口。
 * 对应策划书 §六.1：生活问题推荐、当前任务、学习进度、最新挑战。
 */
export default function JianghuTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">江湖</ThemedText>
        <ThemedText style={styles.tagline}>学一招 AI 心法，造一件自己的江湖作品。</ThemedText>

        <ThemedText themeColor="textSecondary" style={styles.lead}>
          首页与学习入口：生活问题推荐、当前任务、学习进度、最新挑战。
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.card}>
          <ThemedText type="default" style={styles.cardTitle}>当前重点章节</ThemedText>
          <ThemedText type="default" style={styles.cardBody}>
            《百炼识物诀》— 数据、标签与特征
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary">
            相册和垃圾分类 · 数学 / 科学 / 艺术
          </ThemedText>
        </ThemedView>

        <ThemedText type="small" style={styles.note}>
          本故事由策划团队与教育专家联合编绘
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
  tagline: { lineHeight: 24 },
  lead: { lineHeight: 22 },
  card: {
    padding: Spacing.three,
    borderRadius: Spacing.three,
    gap: Spacing.one,
  },
  cardTitle: { fontWeight: '600' },
  cardBody: { lineHeight: 24 },
  note: { fontStyle: 'italic', opacity: 0.7 },
});
