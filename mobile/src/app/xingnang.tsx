import { ScrollView, StyleSheet } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Spacing } from '@/constants/theme';

const SECTIONS = ['我的作品', '我的秘籍', '修行记录', '勋章'] as const;

/**
 * 一级导航：行囊 — 个人成长。
 * 对应策划书 §六.5：展示学生获得的秘籍、作品、修行记录、勋章和个人成长情况。
 */
export default function XingnangTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">行囊</ThemedText>
        <ThemedText style={styles.lead}>
          个人成长档案：作品、秘籍、修行记录、勋章。
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.section}>
          {SECTIONS.map((s) => (
            <ThemedText key={s} type="default" style={styles.row}>
              <ThemedText type="smallBold">{`· ${s}`}</ThemedText>
            </ThemedText>
          ))}
        </ThemedView>

        <ThemedText type="small" style={styles.note}>
          MVP 占位：登录/本地用户绑定后接入
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
  row: { lineHeight: 26 },
  note: { fontStyle: 'italic', opacity: 0.7 },
});
