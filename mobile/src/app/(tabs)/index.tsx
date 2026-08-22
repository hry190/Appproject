import { useState } from 'react';
import { useRouter } from 'expo-router';
import {
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Svg, { SvgXml } from 'react-native-svg';

import bgImage from '@/assets/images/home/bg.png';
import pandaImage from '@/assets/images/home/panda.png';
import pandaChat3Image from '@/assets/images/home/Group 17.png';
import iconSettings from '@/assets/images/home/icon-settings.png';
import iconTask from '@/assets/images/home/icon-task.png';
import iconProgress from '@/assets/images/home/icon-progress.png';
import iconWorks from '@/assets/images/home/icon-works.png';

/**
 * 首页(节点 Figma 301:1695) — 阿砚的聊天界面。
 *
 * v5 改版:
 * - 删掉底部 5 个 Tab(江湖/修炼/造物/大会/行囊)
 * - 新的"一级导航"是右上 4 个快捷键:设置 / 任务 / 进度 / 作品
 *
 * 设计 token(基线 412 × 917 dp):
 *   背景         #FFFFFF(纯白,竹林图做底图)
 *   文字主色     #000000
 *   欢迎气泡背景 SVG(bubble-bg.svg)
 *   提示点: 红色(dot-red.svg) = 有新任务
 *           灰色(dot-gray.svg) = 无新任务
 */

const TEXT_DARK = '#000000';
const DOT_RED_SVG = `<svg width="3.34" height="3.34" viewBox="0 0 3.34 3.34" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="1.67" cy="1.67" r="1.67" fill="#FF0000"/></svg>`;
const DOT_GRAY_SVG = `<svg width="3.34" height="3.34" viewBox="0 0 3.34 3.34" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="1.67" cy="1.67" r="1.67" fill="#999999"/></svg>`;
const ARROW_SVG = `<svg width="8" height="8" viewBox="0 0 8 8" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 2L6 4L3 6" stroke="#000" stroke-width="0.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`;

const Fig = {
  canvasW: 412,
  canvasH: 917,

  bgLeft: -51,
  bgTop: 0,
  bgW: 516,
  bgH: 917,

  shortcut: { size: 22, top: 71, labelTop: 93, spacing: 40 },
  bubble: { left: 29, top: 373, w: 195, h: 74 },
  /** 对话0/1/2 通用熊猫(默认位置) */
  panda: { left: 111, top: 470, w: 153, h: 312 },
  /** 对话3 专属熊猫(Group 17.png,aspect 0.725,新姿态) */
  pandaChat3: { left: 107, top: 473, w: 200, h: 276 },
} as const;

/** 3 段对话的内容 — 文字在 Figma 节点里直接抓 */
const CHAT_STEPS = [
  /* 0 — 初始(进首页时),不显示任何气泡 */
  null,
  /* 1 — 首页对话2:阿砚的欢迎词 */
  'hi，欢迎来到机巧江湖，我是阿砚，在这里，以学识为剑，以思考为途开启你的求知冒险吧！',
  /* 2 — 首页对话3:阿砚介绍身后的去处 */
  '在我身后有三个奇妙去处哦!\n修炼场,可以完成互动试炼,解锁神秘秘籍;\n大会,同伴互评空间,锻炼思考能力;\n作品创作,辅助学生进行AI创作;\n哦差点忘了,行囊,可以查看收获的成果哦。\n聪明的你,已经迫不及待准备出发了吧,我们\n一起开始冒险吧!',
] as const;

type QuickAction = {
  key: string;
  label: string;
  icon: number;
  iconX: number;
  hasDot?: 'red' | 'gray';
  hasArrow?: boolean;
};

const ACTIONS: QuickAction[] = [
  { key: 'settings', label: '设置', icon: iconSettings, iconX: 371 },
  { key: 'task', label: '任务', icon: iconTask, iconX: 331, hasDot: 'red', hasArrow: true },
  { key: 'progress', label: '进度', icon: iconProgress, iconX: 290 },
  { key: 'works', label: '作品', icon: iconWorks, iconX: 250 },
];

export default function HomeScreen() {
  const router = useRouter();
  /** 点击屏幕推进对话;0 = 静默, 1 = 对话2, 2 = 对话3 */
  const [chatStep, setChatStep] = useState(0);
  const advance = () => setChatStep((s) => Math.min(s + 1, CHAT_STEPS.length - 1));
  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>
        {/* 整个画布接 tap 推进对话;用 View 不用 Pressable 避免和内部快捷键按钮嵌套 */}
        <View
          style={styles.canvas}
          onStartShouldSetResponder={() => true}
          onResponderRelease={advance}
          accessibilityLabel="点击推进对话">
          {/* 1. 背景(竹林图,延伸出右 104dp) */}
          <Image
            source={bgImage}
            style={styles.bg}
            resizeMode="cover"
            // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
            pointerEvents="none"
          />

          {/* 2. 4 个右上快捷键 */}
          {ACTIONS.map((a) => (
            <QuickActionItem
              key={a.key}
              action={a}
              onPress={() => handleShortcut(a.key, router)}
            />
          ))}

          {/* 4. 熊猫吉祥物 — 对话 0/1/2 用默认图,对话 3 切到 Group 17 */}
          <Image
            source={chatStep >= 2 ? pandaChat3Image : pandaImage}
            style={
              chatStep >= 2
                ? {
                    position: 'absolute',
                    left: Fig.pandaChat3.left,
                    top: Fig.pandaChat3.top,
                    width: Fig.pandaChat3.w,
                    height: Fig.pandaChat3.h,
                  }
                : styles.panda
            }
            resizeMode="contain"
            // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
            pointerEvents="none"
          />

          {/* 3. 欢迎气泡(View 模拟)— 仅在 chatStep > 0 时显示;放在熊猫之后 z-order 更高 */}
          {chatStep > 0 && CHAT_STEPS[chatStep] && (
            <View style={styles.bubble}>
              <Text
                style={styles.bubbleText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                {CHAT_STEPS[chatStep]}
              </Text>
            </View>
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

/* —— 快捷键点击处理 —— 路由暂未确定,TODO 接入 —— */
function handleShortcut(
  key: string,
  router: ReturnType<typeof useRouter>
) {
  switch (key) {
    case 'settings':
      // TODO: 接入 /settings
      console.log('[home] 打开设置');
      break;
    case 'task':
      // TODO: 接入 /task
      console.log('[home] 打开任务');
      break;
    case 'progress':
      // TODO: 接入 /progress
      console.log('[home] 打开进度');
      break;
    case 'works':
      // TODO: 接入 /works
      console.log('[home] 打开作品');
      break;
    default:
      console.log('[home] unknown shortcut:', key);
  }
}

/* —— 单个快捷键(icon + 标签 + 可选 红点/箭头) —— */
function QuickActionItem({
  action,
  onPress,
}: {
  action: QuickAction;
  onPress: () => void;
}) {
  return (
    <Pressable
      onPress={onPress}
      hitSlop={6}
      accessibilityRole="button"
      accessibilityLabel={action.label}
      style={({ pressed }) => [
        styles.actionItem,
        { left: action.iconX, top: Fig.shortcut.top },
        pressed && styles.actionItemPressed,
      ]}>
      <Image
        source={action.icon}
        style={styles.actionIcon}
        resizeMode="contain"
      />
      <Text
        style={styles.actionLabel}
        maxFontSizeMultiplier={1.4}
        allowFontScaling>
        {action.label}
      </Text>
      {action.hasDot && (
        <View style={styles.dot}>
          <SvgXml
            xml={action.hasDot === 'red' ? DOT_RED_SVG : DOT_GRAY_SVG}
            width="3.34"
            height="3.34"
          />
        </View>
      )}
      {action.hasArrow && (
        <View style={styles.arrow}>
          <SvgXml xml={ARROW_SVG} width="8" height="8" />
        </View>
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#FFFFFF' },
  scroll: { flexGrow: 1 },
  canvas: {
    width: Fig.canvasW,
    height: Fig.canvasH,
    alignSelf: 'center',
    backgroundColor: 'transparent',
  },

  /* 背景 */
  bg: {
    position: 'absolute',
    left: Fig.bgLeft,
    top: Fig.bgTop,
    width: Fig.bgW,
    height: Fig.bgH,
  },

  /* 4 个快捷键 — 绝对定位(每個 iconX 不同) */
  actionItem: {
    position: 'absolute',
    alignItems: 'center',
  },
  actionItemPressed: { opacity: 0.7 },
  actionIcon: {
    width: Fig.shortcut.size,
    height: Fig.shortcut.size,
  },
  actionLabel: {
    marginTop: 2,
    fontSize: 8,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },
  dot: {
    position: 'absolute',
    right: -1,
    top: 0,
  },
  arrow: {
    position: 'absolute',
    right: -3,
    top: 7,
  },

  /* 欢迎气泡(View 模拟,不用 Image) */
  bubble: {
    position: 'absolute',
    left: Fig.bubble.left,
    top: Fig.bubble.top,
    width: Fig.bubble.w,
    minHeight: Fig.bubble.h,
    backgroundColor: '#F4E6CF',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  bubbleText: {
    fontSize: 9,
    lineHeight: 13,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },

  /* 熊猫 */
  panda: {
    position: 'absolute',
    left: Fig.panda.left,
    top: Fig.panda.top,
    width: Fig.panda.w,
    height: Fig.panda.h,
  },
});