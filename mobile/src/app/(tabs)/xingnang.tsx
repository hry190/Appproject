import { ScrollView, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { Colors, Spacing } from '@/constants/theme';

const PROFILE = {
  name: '机巧弟子',
  joined: '入学 12 天',
  badges: 5,
  manuals: 11,
  works: 3,
};

const WORKS = [
  { title: '《我家乡的秋叶》', format: '四格漫画', status: '已发大会' },
  { title: '《银杏落满街》', format: '四格漫画', status: '已发大会' },
  { title: '《叶绿素分解演示》', format: '水彩涂鸦', status: '草稿' },
];

const MANUALS = [
  { name: '叶绿素是什么', state: '习得', date: '2026-08-10' },
  { name: '为什么秋天叶子变色', state: '习得', date: '2026-08-08' },
  { name: '光合作用入门', state: '偶得', date: '2026-08-06' },
  { name: '落叶是厨余垃圾吗', state: '悟得', date: '2026-08-12' },
];

const PRACTICE = [
  { date: '08-12', title: '完成了《落叶垃圾分类》的迁移创作', xp: '+60 XP' },
  { date: '08-10', title: '通过了《叶绿素是什么》原理试炼', xp: '+30 XP' },
  { date: '08-08', title: '发布第一篇评招帖', xp: '+15 XP' },
];

const BADGES = [
  { name: '初出茅庐', color: '#D6E1D0', earned: true },
  { name: '第一篇四格', color: '#F2D9DB', earned: true },
  { name: '善问者', color: '#E8D9A8', earned: true },
  { name: '互评达人', color: '#D6E1D0', earned: true },
  { name: '原创 70%+', color: '#F2D9DB', earned: true },
  { name: '造物首作', color: '#E8D9A8', earned: false },
  { name: '预言家', color: '#D6E1D0', earned: false },
  { name: '习得 5 招', color: '#F2D9DB', earned: false },
];

/**
 * 一级导航：行囊 — 个人成长。
 * 对应策划书 §六.5：作品、秘籍、修行记录、勋章、个人成长情况。
 */
export default function XingnangTab() {
  return (
    <ThemedView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <ThemedText type="subtitle">行囊</ThemedText>

        {/* —— 个人信息 —— */}
        <ThemedView type="backgroundElement" style={styles.profileCard}>
          <ThemedText type="title" style={styles.profileName}>
            {PROFILE.name}
          </ThemedText>
          <ThemedText themeColor="textSecondary" style={styles.profileJoined}>
            {PROFILE.joined}
          </ThemedText>
          <View style={styles.profileStats}>
            <Stat label="作品" value={PROFILE.works} />
            <Stat label="秘籍" value={PROFILE.manuals} />
            <Stat label="勋章" value={PROFILE.badges} />
          </View>
        </ThemedView>

        {/* —— 勋章 —— */}
        <Section title="勋章" subtitle="Badges">
          <View style={styles.badgeGrid}>
            {BADGES.map((b) => (
              <View
                key={b.name}
                style={[
                  styles.badgeChip,
                  { backgroundColor: b.earned ? b.color : Colors.light.border, opacity: b.earned ? 1 : 0.45 },
                ]}>
                <ThemedText
                  style={[
                    styles.badgeText,
                    { color: b.earned ? Colors.light.text : Colors.light.textSecondary },
                  ]}>
                  {b.name}
                </ThemedText>
              </View>
            ))}
          </View>
        </Section>

        {/* —— 我的作品 —— */}
        <Section title="我的作品" subtitle="Works">
          {WORKS.map((w) => (
            <ThemedView key={w.title} type="backgroundElement" style={styles.workCard}>
              <View style={styles.workHeader}>
                <ThemedText type="default" style={styles.workTitle}>
                  {w.title}
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  {w.status}
                </ThemedText>
              </View>
              <ThemedText type="small" themeColor="bamboo">
                {w.format}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        {/* —— 我的秘籍 —— */}
        <Section title="我的秘籍" subtitle="Manuals">
          {MANUALS.map((m) => (
            <ThemedView key={m.name} type="backgroundElement" style={styles.manualCard}>
              <View style={styles.manualHeader}>
                <ThemedText type="default" style={styles.manualName}>
                  {m.name}
                </ThemedText>
                <ManualStateTag state={m.state as '偶得' | '习得' | '悟得'} />
              </View>
              <ThemedText type="small" themeColor="textSecondary">
                习得于 {m.date}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        {/* —— 修行记录 —— */}
        <Section title="修行记录" subtitle="Practice Log">
          {PRACTICE.map((p) => (
            <ThemedView key={`${p.date}-${p.title}`} type="backgroundElement" style={styles.practiceCard}>
              <View style={styles.practiceHeader}>
                <ThemedText type="smallBold" themeColor="cinnabar">
                  {p.date}
                </ThemedText>
                <ThemedText type="smallBold" themeColor="accent">
                  {p.xp}
                </ThemedText>
              </View>
              <ThemedText type="default" style={styles.practiceTitle}>
                {p.title}
              </ThemedText>
            </ThemedView>
          ))}
        </Section>

        <ThemedText type="small" style={styles.note}>
          数据每晚与服务器同步；离线状态会暂存本地，恢复后上传
        </ThemedText>
      </ScrollView>
    </ThemedView>
  );
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <View style={styles.statBlock}>
      <ThemedText type="subtitle" style={styles.statValue}>
        {value}
      </ThemedText>
      <ThemedText type="small" themeColor="textSecondary">
        {label}
      </ThemedText>
    </View>
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

function ManualStateTag({ state }: { state: '偶得' | '习得' | '悟得' }) {
  const colors = Colors.light;
  const palette = {
    偶得: { bg: '#D6E1D0', fg: colors.bamboo },
    习得: { bg: colors.accentSoft, fg: colors.text },
    悟得: { bg: '#F2D9DB', fg: colors.cinnabar },
  }[state];
  return (
    <View style={[styles.tag, { backgroundColor: palette.bg }]}>
      <ThemedText style={[styles.tagText, { color: palette.fg }]}>{state}</ThemedText>
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
  note: { fontStyle: 'italic', opacity: 0.6 },

  section: { gap: Spacing.two },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
  },

  // 个人信息
  profileCard: {
    padding: Spacing.four,
    borderRadius: Spacing.three,
    gap: Spacing.two,
    alignItems: 'center',
  },
  profileName: { fontWeight: '700' },
  profileJoined: { lineHeight: 22 },
  profileStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    marginTop: Spacing.two,
  },
  statBlock: { alignItems: 'center', gap: Spacing.half },
  statValue: { fontWeight: '700' },

  // 勋章
  badgeGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.two,
  },
  badgeChip: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    borderRadius: 999,
  },
  badgeText: { fontWeight: '700', fontSize: 13 },

  // 通用 tag
  tag: {
    paddingHorizontal: Spacing.two,
    paddingVertical: Spacing.half,
    borderRadius: 999,
  },
  tagText: { fontSize: 12, fontWeight: '700' },

  // 作品
  workCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  workHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    gap: Spacing.two,
  },
  workTitle: { fontWeight: '700', flex: 1 },

  // 秘籍
  manualCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  manualHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: Spacing.two,
  },
  manualName: { fontWeight: '700', flex: 1 },

  // 修行记录
  practiceCard: {
    padding: Spacing.three,
    borderRadius: Spacing.two,
    gap: Spacing.one,
  },
  practiceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  practiceTitle: { lineHeight: 22 },
});