import { ScrollView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing } from '@/constants/theme';

const PROMPTS = [
  { key: 'appreciate', label: '我最欣赏的是 ……' },
  { key: 'learn', label: '我从中学到了 ……' },
  { key: 'suggest', label: '我建议可以 ……' },
  { key: 'curious', label: '我想了解的是 ……' },
] as const;

const WORKS = [
  {
    title: '《银杏落满街》四格漫画',
    author: '@霜降同学',
    format: '四格漫画',
    createdByMe: 0.85,
    helpedByAI: 0.15,
    blurb: '用银杏叶的飘落节奏讲了「循环」的小知识。',
    reviews: 12,
  },
  {
    title: '《我把落叶做成了书签》短视频',
    author: '@木叶',
    format: '短视频',
    createdByMe: 0.9,
    helpedByAI: 0.1,
    blurb: '60 秒纪录了把校园落叶压成书签的全过程。',
    reviews: 8,
  },
  {
    title: '《叶绿素是什么》水彩涂鸦',
    author: '@小溪',
    format: '绘画',
    createdByMe: 0.7,
    helpedByAI: 0.3,
    blurb: '用三层水彩演示叶绿素分解时的颜色变化。',
    reviews: 15,
  },
];

const TAGS = ['有创意', '有方法', '有思考', '我学到一招'] as const;

/**
 * 一级导航：武林大会 — 学习成果展示与同伴互评空间。
 * 对应策划书 §六.4：不采用无尽信息流和单一人气排名；
 * 评招帖四段式：「我最欣赏 / 我从中学到 / 我建议可以 / 我想了解的是」。
 */
export default function DahuiTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">武林大会</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.tagline}>
          不是无尽信息流，也不是单一人气排名 —— 是结构化的「评招帖」互评空间。
        </ThemedText>

        {/* —— 评价维度 —— */}
        <Section title="你可以这样评价" subtitle="How to Review">
          <View style={styles.tagWrap}>
            {TAGS.map((t) => (
              <View key={t} style={styles.reviewTag}>
                <ThemedText style={styles.reviewTagText}>{t}</ThemedText>
              </View>
            ))}
          </View>
        </Section>

        {/* —— 评招帖模板 —— */}
        <Section title="评招帖模板" subtitle="Template">
          <ThemedView type="backgroundElement" style={styles.templateCard}>
            {PROMPTS.map((p, i) => (
              <View key={p.key} style={styles.templateRow}>
                <View style={styles.templateIdx}>
                  <ThemedText style={styles.templateIdxText}>{i + 1}</ThemedText>
                </View>
                <ThemedText type="default" style={styles.templateText}>
                  {p.label}
                </ThemedText>
              </View>
            ))}
          </ThemedView>
        </Section>

        {/* —— 精选作品 —— */}
        <Section title="精选作品" subtitle="Featured">
          {WORKS.map((w) => (
            <ThemedView key={w.title} type="backgroundElement" style={styles.workCard}>
              <View style={styles.workHeader}>
                <ThemedText type="default" style={styles.workTitle}>
                  {w.title}
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {w.author}
                </ThemedText>
              </View>
              <ThemedText type="small" themeColor="bamboo">
                {w.format} · 原创 {Math.round(w.createdByMe * 100)}% · AI 辅助 {Math.round(w.helpedByAI * 100)}%
              </ThemedText>
              <ThemedText style={styles.workBlurb}>{w.blurb}</ThemedText>

              <View style={styles.reviewStatRow}>
                <ThemedText type="small" themeColor="textSecondary">
                  已收到 {w.reviews} 篇评招帖
                </ThemedText>
                <ThemedText type="smallBold" themeColor="cinnabar">
                  写一篇评招帖 →
                </ThemedText>
              </View>
            </ThemedView>
          ))}
        </Section>

        {/* —— 我发过的作品 —— */}
        <Section title="我发过的作品" subtitle="Mine">
          <ThemedView type="backgroundSelected" style={styles.myWorkCard}>
            <ThemedText type="default" style={styles.myWorkTitle}>
              《我家乡的秋叶》
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              四格漫画 · 已收到 4 篇评招帖 · 收到 2 个勋章
            </ThemedText>
            <View style={styles.myWorkFooter}>
              <ThemedText type="smallBold" themeColor="cinnabar">
                进入查看 →
              </ThemedText>
            </View>
          </ThemedView>
        </Section>

        <ThemedText type="small" style={styles.note}>
          武林大会只展示同伴互评数量，不按人气排名
        </ThemedText>
      </ScrollView>
    </ThemedView>
  );
}

function Section({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <ThemedText type="smallBold">{title}</ThemedText>
        <ThemedText type="small" themeColor="textSecondary">
          {subtitle}
        </ThemedText>
      </View>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: {
    padding: Spacing.four,
    gap: Spacing.four,
    maxWidth: 800,
    alignSelf: 'center',
  },
  tagline: { lineHeight: 22 },
  note: { fontStyle: 'italic', opacity: 0.6 },

  section: { gap: Spacing.two },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
  },

  // 评价维度
  tagWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.two,
  },
  reviewTag: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    borderRadius: 999,
    backgroundColor: Colors.light.accentSoft,
  },
  reviewTagText: { fontWeight: '700', color: Colors.light.text },

  // 评招帖模板
  templateCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  templateRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
  templateIdx: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: Colors.light.bamboo,
    alignItems: 'center',
    justifyContent: 'center',
  },
  templateIdxText: { color: '#FFFFFF', fontWeight: '700', fontSize: 12 },
  templateText: { flex: 1 },

  // 作品卡
  workCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  workHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    gap: Spacing.two,
  },
  workTitle: { fontWeight: '700', flex: 1 },
  workBlurb: { lineHeight: 22 },
  reviewStatRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: Spacing.one,
  },

  // 我的作品
  myWorkCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  myWorkTitle: { fontWeight: '700' },
  myWorkFooter: { marginTop: Spacing.one },
});