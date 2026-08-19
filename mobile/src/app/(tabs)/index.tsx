import { ScrollView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing } from '@/constants/theme';

const LIFE_QUESTION = {
  title: '今天校园里的落叶应该按“可回收”扔吗？',
  tags: ['垃圾分类', '生物', '日常'],
  blurb:
    '落叶属于厨余垃圾还是其他垃圾？一起来拆解这个问题的判断依据。',
};

const CURRENT_TASKS = [
  { title: '阅读《百炼识物诀》第 3 章', progress: 0.6, unit: '5 / 8 页' },
  { title: '完成预测题：为什么树叶会变色', progress: 0.0, unit: '未开始' },
  { title: '向同学提交一份「评招帖」', progress: 0.5, unit: '1 / 2 篇' },
];

const PROGRESS = [
  { name: '偶得秘籍', current: 7, total: 12 },
  { name: '习得秘籍', current: 3, total: 12 },
  { name: '悟得秘籍', current: 1, total: 12 },
];

const CHALLENGES = [
  { title: '7 日试炼：观察一种植物', due: '剩余 3 天', badge: '新' },
  { title: '周末武林大会：我的作品', due: '周六 20:00', badge: '热门' },
  { title: '把今天的发现讲给家人听', due: '今日', badge: '日常' },
];

/**
 * 一级导航：江湖 — 项目首页与学习入口。
 * 对应策划书 §六.1：生活问题推荐、当前任务、学习进度、最新挑战。
 */
export default function JianghuTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">江湖</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.tagline}>
          学一招 AI 心法，造一件自己的江湖作品。
        </ThemedText>

        {/* —— 生活问题推荐 —— */}
        <Section title="今日生活问题" subtitle="Daily Question">
          <ThemedView type="backgroundElement" style={styles.questionCard}>
            <ThemedText type="smallBold" themeColor="cinnabar">
              {LIFE_QUESTION.tags.join(' · ')}
            </ThemedText>
            <ThemedText type="default" style={styles.questionTitle}>
              {LIFE_QUESTION.title}
            </ThemedText>
            <ThemedText themeColor="textSecondary" style={styles.questionBlurb}>
              {LIFE_QUESTION.blurb}
            </ThemedText>
            <View style={styles.tagsRow}>
              <Tag label="想一想" tone="accent" />
              <Tag label="写下你的预测" tone="bamboo" />
            </View>
          </ThemedView>
        </Section>

        {/* —— 当前任务 —— */}
        <Section title="当前任务" subtitle="In Progress">
          {CURRENT_TASKS.map((t) => (
            <ThemedView key={t.title} type="backgroundElement" style={styles.taskCard}>
              <View style={styles.taskHeader}>
                <ThemedText type="default" style={styles.taskTitle}>
                  {t.title}
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {t.unit}
                </ThemedText>
              </View>
              <ProgressBar value={t.progress} />
            </ThemedView>
          ))}
        </Section>

        {/* —— 学习进度 —— */}
        <Section title="学习进度" subtitle="Progress">
          <ThemedView type="backgroundElement" style={styles.progressCard}>
            {PROGRESS.map((p) => (
              <View key={p.name} style={styles.progressRow}>
                <ThemedText type="default">{p.name}</ThemedText>
                <View style={styles.progressBarWrap}>
                  <View
                    style={[
                      styles.progressBarFill,
                      {
                        width: `${Math.max(8, Math.round((p.current / p.total) * 100))}%`,
                        backgroundColor:
 p.current >= p.total ? Colors.light.bamboo : Colors.light.accent,
                      },
                    ]}
                  />
                </View>
                <ThemedText type="small" themeColor="textSecondary">
                  {p.current} / {p.total}
                </ThemedText>
              </View>
            ))}
          </ThemedView>
        </Section>

        {/* —— 最新挑战 —— */}
        <Section title="最新挑战" subtitle="Latest Challenges">
          {CHALLENGES.map((c) => (
            <ThemedView key={c.title} type="backgroundElement" style={styles.challengeCard}>
              <View style={styles.challengeHeader}>
                <ThemedText type="default" style={styles.challengeTitle}>
                  {c.title}
                </ThemedText>
                <Tag label={c.badge} tone={c.badge === '热门' ? 'cinnabar' : 'accent'} />
              </View>
              <ThemedText type="small" themeColor="textSecondary">
                {c.due}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        <ThemedText type="small" style={styles.note}>
          今日推荐由《百炼识物诀》编辑组与教育专家共同筛选
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

function Tag({ label, tone }: { label: string; tone: 'accent' | 'bamboo' | 'cinnabar' }) {
  const colors = Colors.light;
  const palette = {
    accent: { bg: colors.accentSoft, fg: colors.text },
    bamboo: { bg: '#D6E1D0', fg: colors.bamboo },
    cinnabar: { bg: '#F2D9DB', fg: colors.cinnabar },
  }[tone];
  return (
    <View style={[styles.tag, { backgroundColor: palette.bg }]}>
      <ThemedText style={[styles.tagText, { color: palette.fg }]}>{label}</ThemedText>
    </View>
  );
}

function ProgressBar({ value }: { value: number }) {
  const pct = Math.round(value * 100);
  return (
    <View style={styles.taskBar}>
      <View style={[styles.taskBarFill, { width: `${pct}%` }]} />
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

  // —— 生活问题 ——
  questionCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  questionTitle: { fontSize: 17, fontWeight: '700', lineHeight: 24 },
  questionBlurb: { lineHeight: 22 },
  tagsRow: { flexDirection: 'row', gap: Spacing.two, marginTop: Spacing.one },
  tag: {
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
    borderRadius: 999,
  },
  tagText: { fontSize: 12, fontWeight: '600' },

  // —— 当前任务 ——
  taskCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  taskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  taskTitle: { fontWeight: '600', flex: 1, marginRight: Spacing.two },
  taskBar: {
    height: 6,
    backgroundColor: Colors.light.border,
    borderRadius: 999,
    overflow: 'hidden',
  },
  taskBarFill: {
    height: '100%',
    backgroundColor: Colors.light.accent,
    borderRadius: 999,
  },

  // —— 学习进度 ——
  progressCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.three,
  },
  progressRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
  progressBarWrap: {
    flex: 1,
    height: 8,
    backgroundColor: Colors.light.border,
    borderRadius: 999,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 999,
  },

  // —— 最新挑战 ——
  challengeCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  challengeHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: Spacing.two,
  },
  challengeTitle: { fontWeight: '600', flex: 1 },
});