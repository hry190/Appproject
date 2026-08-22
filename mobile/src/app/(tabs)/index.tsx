import { useState } from 'react';
import { useRouter } from 'expo-router';
import {
  Image,
  type ImageSourcePropType,
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
import image1 from '@/assets/images/home/1.png';
import image2 from '@/assets/images/home/2.png';
import image3 from '@/assets/images/home/3.png';
import image4 from '@/assets/images/home/4.png';
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
 * 设计 token(基线 412 × 800 dp):
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
  canvasH: 810,

  bgLeft: -51,
  bgTop: 0,
  bgW: 500,
  bgH: 900,

  shortcut: { size: 22, top: 71, labelTop: 93, spacing: 40 },
  bubble: { left: 29, top: 363, w: 195, h: 74 },
  /** 对话0/1/2 通用熊猫(默认位置) */
  panda: { left: 107, top: 473, w: 200, h: 276 },
  /** 对话3 专属熊猫(Group 17.png,aspect 0.725,新姿态) */
  pandaChat3: { left: 107, top: 473, w: 200, h: 276 },
  /** 4 张小图(从原 Figma 任务页 home2 拿的)— 都用 33×69 装饰图,按 Figma 位置放 */
  image1: { left: 120, top: 613, w: 40, h: 90 },
  image2: { left: 82, top: 275, w: 40, h: 90 },
  image3: { left: 176, top: 381, w: 40, h: 90 },
  image4: { left: 308, top: 381, w: 40, h: 90 },
} as const;

/** 4 段对话的内容 — 文字在 Figma 节点里直接抓 */
const CHAT_STEPS = [
  /* 0 — 初始(进首页时),不显示任何气泡 */
  null,
  /* 1 — 首页对话2:阿砚的欢迎词 */
  'hi，欢迎来到机巧江湖，我是阿砚，在这里，以学识为剑，以思考为途开启你的求知冒险吧！',
  /* 2 — 首页对话3:阿砚介绍身后的去处 */
  '在我身后有三个奇妙去处哦!\n修炼场,可以完成互动试炼,解锁神秘秘籍;\n大会,同伴互评空间,锻炼思考能力;\n作品创作,辅助学生进行AI创作;\n哦差点忘了,行囊,可以查看收获的成果哦。\n聪明的你,已经迫不及待准备出发了吧,我们\n一起开始冒险吧!',
  /* 3 — 对话3 之后:气泡消失,只留装饰图 */
  null,
] as const;

/** 4 张装饰横幅 + 各自的竖排中文标题
 *  关键:文字不是烧进 PNG 的,而是用 <Text> 在图片上方运行时叠加,
 *  这样字号、颜色、阴影都能在 TS 里调,不用重画图片。
 */
const DECOR_BANNERS = [
  { key: 'luggage',    src: image1, pos: Fig.image1, chars: ['行', '囊'] },
  { key: 'cultivate',  src: image2, pos: Fig.image2, chars: ['修', '炼'] },
  { key: 'conference', src: image3, pos: Fig.image3, chars: ['大', '会'] },
  { key: 'create',     src: image4, pos: Fig.image4, chars: ['作', '品', '创', '作'] },
] as const;

/** 装饰横幅文字样式 token — 跟气泡色调统一 */
const DECOR_TEXT_COLOR = '#F4E6CF';     // 米黄(跟气泡同色)
const DECOR_SHADOW = 'rgba(20,30,20,0.85)'; // 深绿阴影
const DECOR_FONT_SIZE = 13;
/** 文字相对图片中心的水平偏移:负数偏左,正数偏右。0 = 居中。 */
const DECOR_TEXT_OFFSET_X = -5;

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

          {/* 4b. 4 张小图 + 各自的中文标题(<Text> 运行时叠加,不烧进 PNG)
            仅在对话 3 结束(气泡消失)后才显示 — chatStep >= 3
            之前(0/1/2)保持页面干净,只在最后阶段揭示四个去处 */}
          {chatStep >= 3 &&
            DECOR_BANNERS.map((b) => (
              <DecorBanner
                key={b.key}
                src={b.src}
                left={b.pos.left}
                top={b.pos.top}
                w={b.pos.w}
                h={b.pos.h}
                chars={b.chars}
              />
            ))}

          {/* 3. 欢迎气泡(View 模拟)— 仅在对话 2/3 显示(对话 4+ 不显示);放在熊猫之后 z-order 更高 */}
          {chatStep > 0 && chatStep < 3 && CHAT_STEPS[chatStep] && (
            <View style={styles.bubble}>
              <View style={styles.bubbleTail} />
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

/* —— 装饰横幅:装饰图(底层) + 竖排中文(顶层叠加,运行时渲染,不是烧进 PNG) —— */
function DecorBanner({
  src,
  left,
  top,
  w,
  h,
  chars,
}: {
  src: ImageSourcePropType;
  left: number;
  top: number;
  w: number;
  h: number;
  chars: readonly string[];
}) {
  const lineH = DECOR_FONT_SIZE;
  const blockH = chars.length * lineH;
  // 文字块在容器内垂直居中
  const padTop = Math.max(0, (h - blockH) / 3);
  return (
    <View
      style={{
        position: 'absolute',
        left,
        top,
        width: w,
        height: h,
      }}
      pointerEvents="none">
      {/* 底层装饰图 */}
      <Image
        source={src}
        style={StyleSheet.absoluteFill}
        resizeMode="contain"
        // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
        pointerEvents="none"
      />
      {/* 顶层竖排文字:每个字一个 <Text>,垂直堆叠 */}
      <View
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          top: padTop,
          transform: [{ translateX: DECOR_TEXT_OFFSET_X }],
        }}>
        {chars.map((c, i) => (
          <Text
            key={i}
            style={{
              fontSize: DECOR_FONT_SIZE,
              lineHeight: lineH,
              color: DECOR_TEXT_COLOR,
              fontFamily: 'Microsoft YaHei',
              textAlign: 'center',
              textShadowColor: DECOR_SHADOW,
              textShadowOffset: { width: 1, height: 1 },
              textShadowRadius: 0,
            }}
            maxFontSizeMultiplier={1.4}
            allowFontScaling>
            {c}
          </Text>
        ))}
      </View>
    </View>
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
    /* 右下角保持小圆角,让尾巴(下方绝对定位)看起来贴得自然 */
    borderBottomRightRadius: 4,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  /* 气泡尾巴 — 用 border 三角技巧,右下角朝下 */
  bubbleTail: {
    position: 'absolute',
    bottom: -7,
    right: 14,
    width: 0,
    height: 0,
    borderLeftWidth: 6,
    borderRightWidth: 6,
    borderTopWidth: 8,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderTopColor: '#F4E6CF',
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