import { ScrollView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing } from '@/constants/theme';

const CHAPTER = {
  title: '《百炼识物诀》第 3 章 — 树叶为什么会变色？',
  unit: '8 页漫画 · 3 道预测 · 2 次试炼',
  blurb:
    '本章讲述：当气温下降，树叶里的叶绿素分解，类胡萝卜素和花青素逐渐显现。',
};

const STATES = [
  {
    name: '偶得',
    desc: '在阅读或预测中偶然发现一个知识点',
    example: '发现「叶绿素」是绿色的原因',
    achieved: 7,
    total: 12,
    tone: 'bamboo' as const,
  },
  {
    name: '习得',
    desc: '完成原理试炼，能向他人解释原理',
    example: '解释为什么秋天树叶变黄或变红',
    achieved: 3,
    total: 12,
    tone: 'accent' as const,
  },
  {
    name: '悟得',
    desc: '完成迁移创作：把这个原理用在自己的作品里',
    example: '《我家乡的秋叶》四格漫画',
    achieved: 1,
    total: 12,
    tone: 'cinnabar' as const,
  },
];

const PREDICTIONS = [
  {
    question: '如果把一片绿叶放进冰箱一周，它会变成什么颜色？',
    type: '单选',
    status: 'in_progress',
  },
  {
    question: '枫叶和银杏叶，哪个先变色？为什么？',
    type: '开放问答',
    status: 'new',
  },
];

const TRIALS = [
  {
    title: '试炼 · 原理解释',
    desc: '用 60 秒向同桌解释「叶绿素分解」，并回答他的追问。',
    xp: '+ 30 XP',
    status: 'ready',
  },
  {
    title: '试炼 · 同伴互检',
    desc: '和同伴交换预测答案，互相点评对方的「理由是否站得住脚」。',
    xp: '+ 20 XP',
    status: 'locked',
  },
];

/**
 * 一级导航：修炼 — AI 知识学习模块。
 * 对应策划书 §六.2：阅读漫画 → 进行预测 → 完成互动试炼 → 解锁秘籍。
 * 秘籍三态：偶得 → 习得 → 悟得。
 */
export default function XiulianTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">修炼</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.tagline}>
          读漫画 → 做预测 → 闯试炼 → 解锁秘籍
        </ThemedText>

        {/* —— 当前章节 ——
        */}
        <Section title="当前章节" subtitle="Now Reading">
          <ThemedView type="backgroundElement" style={styles.chapterCard}>
            <ThemedText type="smallBold" themeColor="cinnabar">
              进行中
            </ThemedText>
            <ThemedText type="default" style={styles.chapterTitle}>
              {CHAPTER.title}
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary">
              {CHAPTER.unit}
            </ThemedText>
            <ThemedText style={styles.chapterBlurb}>{CHAPTER.blurb}</ThemedText>
            <View style={styles.chapterCta}>
              <ThemedText type="smallBold" style={styles.ctaPrimary}>
                继续阅读 →
              </ThemedText>
            </View>
          </ThemedView>
        </Section>

        {/* —— 秘籍三态 —— */}
        <Section title="秘籍三态" subtitle="Three Stages">
          {STATES.map((s) => (
            <ThemedView key={s.name} type="backgroundElement" style={styles.stateCard}>
              <View style={styles.stateHeader}>
                <Tag label={s.name} tone={s.tone} />
                <ThemedText type="small" themeColor="textSecondary">
                  {s.achieved} / {s.total}
                </ThemedText>
              </View>
              <ThemedText type="default" style={styles.stateDesc}>
                {s.desc}
              </ThemedText>
              <View style={styles.exampleRow}>
                <ThemedText type="small" themeColor="textSecondary">
                  例：
                </ThemedText>
                <ThemedText type="small">{s.example}</ThemedText>
              </View>
              <View style={styles.stateBar}>
                <View
                  style={[
                    styles.stateBarFill,
                    {
                      width: `${Math.max(
                        8,
                        Math.round((s.achieved / s.total) * 100)
                      )}%`,
                    },
                  ]}
                />
              </View>
            </ThemedView>
          ))}
        </Section>

        {/* —— 预测题 —— */}
        <Section title="预测题" subtitle="Predict">
          {PREDICTIONS.map((p) => (
            <ThemedView key={p.question} type="backgroundElement" style={styles.predictCard}>
              <View style={styles.predictHeader}>
                <ThemedText type="small" themeColor="bamboo">
                  {p.type}
                </ThemedText>
                <ThemedText
                  type="small"
                  themeColor={p.status === 'new' ? 'cinnabar' : 'textSecondary'}>
                  {p.status === 'new' ? '新' : '进行中'}
                </ThemedText>
              </View>
              <ThemedText type="default" style={styles.predictQuestion}>
                {p.question}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        {/* —— 试炼 —— */}
        <Section title="试炼" subtitle="Trials">
          {TRIALS.map((t) => (
            <ThemedView
              key={t.title}
              type={t.status === 'locked' ? 'backgroundSelected' : 'backgroundElement'}
              style={[styles.trialCard, t.status === 'locked' && styles.trialCardLocked]}>
              <View style={styles.trialHeader}>
                <ThemedText type="default" style={styles.trialTitle}>
                  {t.title}
                </ThemedText>
                <ThemedText type="smallBold" themeColor="accent">
                  {t.xp}
                </ThemedText>
              </View>
              <ThemedText type="small" themeColor="textSecondary">
                {t.desc}
              </ThemedText>
              <ThemedText type="small" themeColor={t.status === 'ready' ? 'bamboo' : 'textSecondary'}>
                {t.status === 'ready' ? '可开始' : '需要先完成上一试炼'}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        <ThemedText type="small" style={styles.note}>
          每次「习得」与「悟得」都会在你的「行囊」中永久留下记录
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

  tag: {
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
    borderRadius: 999,
  },
  tagText: { fontSize: 12, fontWeight: '700' },

  // 当前章节
  chapterCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  chapterTitle: { fontSize: 17, fontWeight: '700', lineHeight: 24 },
  chapterBlurb: { lineHeight: 22, marginTop: Spacing.one },
  chapterCta: { marginTop: Spacing.two },
  ctaPrimary: { color: Colors.light.cinnabar },

  // 秘籍三态
  stateCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  stateHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  stateDesc: { lineHeight: 22 },
  exampleRow: { flexDirection: 'row', gap: Spacing.one, alignItems: 'baseline' },
  stateBar: {
    height: 6,
    backgroundColor: Colors.light.border,
    borderRadius: 999,
    overflow: 'hidden',
    marginTop: Spacing.one,
  },
  stateBarFill: {
    height: '100%',
    backgroundColor: Colors.light.accent,
    borderRadius: 999,
  },

  // 预测题
  predictCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  predictHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  predictQuestion: { fontWeight: '600', lineHeight: 22 },

  // 试炼
  trialCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  trialCardLocked: { opacity: 0.55 },
  trialHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  trialTitle: { fontWeight: '700', flex: 1, marginRight: Spacing.two },
});