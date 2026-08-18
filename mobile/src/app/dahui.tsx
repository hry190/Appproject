import { ScrollView, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';

const PROMPTS = [
  '我最欣赏的是 …',
  '我从中学到了 …',
  '我建议可以 …',
  '我想了解的是 …',
] as const;

/**
 * 一级导航：武林大会 — 学习成果展示与同伴互评空间。
 * 对应策划书 §六.4：不采用无尽信息流和单一人气排名；评招帖结构化四段。
 */
export default function DahuiTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">武林大会</ThemedText>
        <ThemedText style={styles.lead}>
          学习成果展示与同伴互评空间，不采用无尽信息流和单一人气排名。
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.section}>
          <ThemedText style={styles.sectionTitle}>评招帖四段式</ThemedText>
          {PROMPTS.map((p, i) => (
            <ThemedText key={p} type="default">
              <ThemedText type="smallBold">{`${i + 1}. `}</ThemedText>
              <ThemedText themeColor="textSecondary">{p}</ThemedText>
            </ThemedText>
          ))}
        </ThemedView>

        <ThemedText type="small" style={styles.note}>
          MVP 占位：示例作品列表接入 Python 后端 /api/works
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
