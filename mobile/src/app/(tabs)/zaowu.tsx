import { ScrollView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing } from '@/constants/theme';

const PRINCIPLES = [
  '一次只问一个引导问题',
  '不代替学生完成全部创作',
  '记录学生原创、AI 辅助与修改过程',
];

const FORMATS = [
  { key: 'painting', label: '绘画', desc: '水彩 / 油画棒 / 数字绘画' },
  { key: 'comic', label: '漫画', desc: '四格 / 短篇 / 连载' },
  { key: 'animation', label: '动画', desc: '定格 / 简易动画 / 故事板' },
  { key: 'video', label: '短视频', desc: '60 秒讲解 / 过程纪录' },
] as const;

const STEPS = [
  { idx: 1, name: '明确主题', ask: '你想做一件什么样的作品？为什么？' },
  { idx: 2, name: '任务拆解', ask: '把它拆成 3 个小步骤，每一步要做什么？' },
  { idx: 3, name: '制作分镜', ask: '第 1 格 / 第 2 格 / 第 3 格分别画什么？' },
  { idx: 4, name: '修改迭代', ask: '哪一处最不像你最初想的样子？为什么？' },
  { idx: 5, name: '署名留痕', ask: '哪一部分是 AI 帮你做的？你做了哪部分？' },
];

const RECENT_WORKS = [
  {
    title: '我家乡的秋叶',
    format: '四格漫画',
    createdByMe: 0.7,
    helpedByAI: 0.3,
    status: '习得',
  },
  {
    title: '水彩落叶小实验',
    format: '绘画',
    createdByMe: 0.85,
    helpedByAI: 0.15,
    status: '偶得',
  },
];

/**
 * 一级导航：造物 — 神机造物 Agent 入口。
 * 对应策划书 §六.3：以创作教练方式辅助学生进行绘画、漫画、动画、短视频创作。
 * Agent 通过提问引导，不代替学生完成全部创作。
 */
export default function ZaowuTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">造物</ThemedText>
        <ThemedText themeColor="textSecondary" style={styles.tagline}>
          神机造物 Agent —— 用提问带你走完一次创作
        </ThemedText>

        {/* —— 创作教练原则 —— */}
        <Section title="创作教练原则" subtitle="How I Coach">
          <ThemedView type="backgroundElement" style={styles.principlesCard}>
            {PRINCIPLES.map((p, i) => (
              <View key={p} style={styles.principleRow}>
                <ThemedText type="smallBold" themeColor="cinnabar">
                  {i + 1}.
                </ThemedText>
                <ThemedText type="default" style={styles.principleText}>
                  {p}
                </ThemedText>
              </View>
            ))}
          </ThemedView>
        </Section>

        {/* —— 选择创作形式 —— */}
        <Section title="选择创作形式" subtitle="Format">
          <View style={styles.formatGrid}>
            {FORMATS.map((f) => (
              <ThemedView key={f.key} type="backgroundElement" style={styles.formatCard}>
                <ThemedText type="default" style={styles.formatLabel}>
                  {f.label}
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {f.desc}
                </ThemedText>
              </ThemedView>
            ))}
          </View>
        </Section>

        {/* —— 引导式创作步骤 —— */}
        <Section title="引导式创作步骤" subtitle="Guided Steps">
          {STEPS.map((s) => (
            <ThemedView key={s.idx} type="backgroundElement" style={styles.stepCard}>
              <View style={styles.stepHeader}>
                <View style={styles.stepIdx}>
                  <ThemedText style={styles.stepIdxText}>{s.idx}</ThemedText>
                </View>
                <ThemedText type="default" style={styles.stepName}>
                  {s.name}
                </ThemedText>
              </View>
              <ThemedText themeColor="textSecondary" style={styles.stepAsk}>
                Agent 会问：{s.ask}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        {/* —— 开始创作 CTA —— */}
        <ThemedView type="backgroundSelected" style={styles.cta}>
          <ThemedText type="default" style={styles.ctaTitle}>
            开始一次造物
          </ThemedText>
          <ThemedText type="small" themeColor="textSecondary" style={styles.ctaDesc}>
            Agent 会按上面的 5 步引导你，并在最后自动记录「学生原创 / AI 辅助 / 修改过程」。
          </ThemedText>
        </ThemedView>

        {/* —— 最近作品 —— */}
        <Section title="最近作品" subtitle="My Works">
          {RECENT_WORKS.map((w) => (
            <ThemedView key={w.title} type="backgroundElement" style={styles.workCard}>
              <View style={styles.workHeader}>
                <ThemedText type="default" style={styles.workTitle}>
                  {w.title}
                </ThemedText>
                <Tag label={w.status} tone="bamboo" />
              </View>
              <ThemedText type="small" themeColor="textSecondary">
                {w.format}
              </ThemedText>
              <View style={styles.shareRow}>
                <ThemedText type="small" style={{ width: 64 }}>
                  原创
                </ThemedText>
                <View style={styles.shareBar}>
                  <View
                    style={[
                      styles.shareBarFill,
                      { width: `${Math.round(w.createdByMe * 100)}%` },
                    ]}
                  />
                </View>
                <ThemedText type="small">{Math.round(w.createdByMe * 100)}%</ThemedText>
              </View>
              <View style={styles.shareRow}>
                <ThemedText type="small" style={{ width: 64 }}>
                  AI 辅助
                </ThemedText>
                <View style={styles.shareBar}>
                  <View
                    style={[
                      styles.shareBarFillAi,
                      { width: `${Math.round(w.helpedByAI * 100)}%` },
                    ]}
                  />
                </View>
                <ThemedText type="small">{Math.round(w.helpedByAI * 100)}%</ThemedText>
              </View>
            </ThemedView>
          ))}
        </Section>

        <ThemedText type="small" style={styles.note}>
          作品分享到大会前会自动弹出「署名留痕」确认
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

  // 原则
  principlesCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  principleRow: {
    flexDirection: 'row',
    gap: Spacing.two,
    alignItems: 'flex-start',
  },
  principleText: { flex: 1, lineHeight: 22 },

  // 形式
  formatGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.two,
  },
  formatCard: {
    flexBasis: '47%',
    flexGrow: 1,
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  formatLabel: { fontWeight: '700' },

  // 步骤
  stepCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  stepHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
  stepIdx: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: Colors.light.cinnabar,
    alignItems: 'center',
    justifyContent: 'center',
  },
  stepIdxText: { color: '#FFFFFF', fontWeight: '700' },
  stepName: { fontWeight: '700' },
  stepAsk: { lineHeight: 22 },

  // CTA
  cta: {
    padding: Spacing.four,
    borderRadius: Spacing.three,
    alignItems: 'center',
    gap: Spacing.one,
  },
  ctaTitle: { fontSize: 18, fontWeight: '700' },
  ctaDesc: { textAlign: 'center', lineHeight: 20 },

  // 最近作品
  workCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.two,
  },
  workHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  workTitle: { fontWeight: '700' },
  shareRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.two,
  },
  shareBar: {
    flex: 1,
    height: 6,
    backgroundColor: Colors.light.border,
    borderRadius: 999,
    overflow: 'hidden',
  },
  shareBarFill: {
    height: '100%',
    backgroundColor: Colors.light.accent,
    borderRadius: 999,
  },
  shareBarFillAi: {
    height: '100%',
    backgroundColor: Colors.light.bamboo,
    borderRadius: 999,
  },
});