import { useState, type Dispatch, type SetStateAction } from 'react';
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
import rectBgImage from '@/assets/images/home/Rectangle 187.png'; // 任务下拉面板的底图(竹叶 bokeh 风格)
import progressModalBg from '@/assets/images/home/image 13.png'; // 学习进度弹窗的底图(Figma 342:2597)
import avatarImage from '@/assets/images/home/byhuo.png'; // 学习进度弹窗的用户头像
import cornerImage from '@/assets/images/home/dfggtr.png'; // 学习进度弹窗右下角装饰图
import dailyPandaImage from '@/assets/images/home/hbuyy.png'; // 每日问题场景用的熊猫(替换原 pandaChat3)
import luggageCardBg from '@/assets/images/home/image 14.png'; // 行囊页的卡片底图(Figma 397:2400)
import luggageAvatarBg from '@/assets/images/home/Ellipse 25.png'; // 行囊页头像的圆框底图
import levelBadgeBg from '@/assets/images/home/terw.png'; // 行囊页"见习弟子"勋章背景框

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
  /** 任务图标点开后是否展开下拉面板(单次展开,不收回) */
  const [taskExpanded, setTaskExpanded] = useState(false);
  /** 点击"进度"图标后弹出的学习进度弹窗 */
  const [progressOpen, setProgressOpen] = useState(false);
  /** 弹窗里的"每日生活问题推荐:"点击后,显示每日问题气泡(Figma 321:2187) */
  const [dailyOpen, setDailyOpen] = useState(false);
  /** 每日问题步骤:dailyOpen 后 1=第一问,2=第二问;点屏幕推进 */
  const [dailyStep, setDailyStep] = useState<1 | 2>(1);
  /** 点击"行囊"横幅后,显示行囊页(Figma 397:2400) */
  const [luggageOpen, setLuggageOpen] = useState(false);
  const advance = () => {
    // 每日问题模式下:dailyStep 1 → 2 → 再点 → 关闭回首页
    if (dailyOpen) {
      if (dailyStep === 1) {
        setDailyStep(2);
      } else {
        // dailyStep === 2:再点一次 → 关闭每日问题,回到首页(chatStep 保持 2)
        setDailyOpen(false);
        setDailyStep(1); // 重置,下次打开仍从问题1 开始
      }
      return;
    }
    setChatStep((s) => Math.min(s + 1, CHAT_STEPS.length - 1));
  };
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
              onPress={() =>
                handleShortcut(a.key, router, setTaskExpanded, setProgressOpen)
              }
            />
          ))}

          {/* 2b. 任务图标点开后的下拉面板(运行时拼装,不贴图) */}
          {taskExpanded && (
            <TaskDropdown
              taskIconX={ACTIONS[1].iconX}
              iconSize={Fig.shortcut.size}
              labelTop={Fig.shortcut.labelTop}
            />
          )}

          {/* 4. 熊猫吉祥物 — 对话 0/1 用 panda,对话 2+ 切到 pandaChat3,
              每日问题场景(dailyOpen=true)切到 hbuyy.png(其他场景不变) */}
          <Image
            source={
              dailyOpen
                ? dailyPandaImage
                : chatStep >= 2
                ? pandaChat3Image
                : pandaImage
            }
            style={
              dailyOpen || chatStep >= 2
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
            弹窗打开时也隐藏 — !progressOpen
            之前(0/1/2)保持页面干净,只在最后阶段揭示四个去处 */}
          {chatStep >= 3 &&
            !progressOpen &&
            !luggageOpen &&
            DECOR_BANNERS.map((b) => (
              <DecorBanner
                key={b.key}
                src={b.src}
                left={b.pos.left}
                top={b.pos.top}
                w={b.pos.w}
                h={b.pos.h}
                chars={b.chars}
                onPress={
                  b.key === 'luggage' ? () => setLuggageOpen(true) : undefined
                }
              />
            ))}

          {/* 3. 欢迎气泡(View 模拟)— 仅在对话 2/3 显示(对话 4+ 不显示);放在熊猫之后 z-order 更高 */}
          {chatStep > 0 && chatStep < 3 && !dailyOpen && CHAT_STEPS[chatStep] && (
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

          {/* 3b. 每日问题气泡(Figma 321:2187)— 点弹窗里的"每日生活问题推荐:"触发
              点击气泡关闭(回到 chatStep 2 的常规状态)
              dailyStep=1 → 第一问,dailyStep=2 → 第二问(点屏幕切到下一问) */}
          {dailyOpen && (
            <Pressable
              onPress={() => setDailyOpen(false)}
              style={[styles.bubble, { left: 70, top: 450, width: 95 }]}
              accessibilityLabel="每日问题气泡 — 点击关闭">
              <View style={styles.bubbleTail} />
              <Text
                style={styles.bubbleText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                {dailyStep === 1
                  ? '。。。去找找秘籍，看看有没有答案'
                  : '生活问题推荐:\n机器人为什么会认错物体?'}
              </Text>
            </Pressable>
          )}

          {/* 5. "我的学习进度" 弹窗(点进度图标触发)— 整张盖在画布上 */}
          {progressOpen && (
            <ProgressModal
              onClose={() => {
                setProgressOpen(false);
                // 关闭后回到"首页2"(对话阶段 2:阿砚介绍身后去处,Group 17 熊猫)
                setChatStep(2);
              }}
              onOpenDaily={() => {
                // 点"每日生活问题推荐:" → 关闭弹窗 + 显示每日问题气泡
                setProgressOpen(false);
                setDailyOpen(true);
                setDailyStep(1); // 每次打开都从问题 1 开始
              }}
              onOpenLuggage={() => {
                // 点"详情请查看行囊" → 关闭弹窗 + 打开行囊页
                setProgressOpen(false);
                setLuggageOpen(true);
              }}
            />
          )}

          {/* 6. "行囊" 页(Figma 397:2400)— 点行囊横幅触发,关闭回首页 */}
          {luggageOpen && (
            <LuggagePage onClose={() => setLuggageOpen(false)} />
          )}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

/* —— 快捷键点击处理 —— 路由暂未确定,TODO 接入 —— */
function handleShortcut(
  key: string,
  router: ReturnType<typeof useRouter>,
  setTaskExpanded: Dispatch<SetStateAction<boolean>>,
  setProgressOpen: Dispatch<SetStateAction<boolean>>,
) {
  switch (key) {
    case 'settings':
      // TODO: 接入 /settings
      console.log('[home] 打开设置');
      break;
    case 'task':
      // 任务图标:点一次展开,再点一次收起(切换)
      setTaskExpanded((prev) => !prev);
      console.log('[home] 任务图标 — 切换下拉');
      break;
    case 'progress':
      console.log('[home] 打开进度弹窗');
      setProgressOpen(true);
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
  onPress,
}: {
  src: ImageSourcePropType;
  left: number;
  top: number;
  w: number;
  h: number;
  chars: readonly string[];
  /** 点击横幅时触发(行囊横幅用这个跳转行囊页) */
  onPress?: () => void;
}) {
  const lineH = DECOR_FONT_SIZE;
  const blockH = chars.length * lineH;
  // 文字块在容器内垂直居中
  const padTop = Math.max(0, (h - blockH) / 3);
  // 装饰图 + 文字 一起作为容器子节点;容器本身可点
  return (
    <Pressable
      onPress={onPress}
      disabled={!onPress}
      hitSlop={6}
      style={{
        position: 'absolute',
        left,
        top,
        width: w,
        height: h,
      }}>
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
        pointerEvents="none"
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
    </Pressable>
  );
}

/* —— 任务图标点开后的下拉面板(对应 Group 162 的展开样式,但用 RN 组件拼,不用贴图) —— */
function TaskDropdown({
  taskIconX,
  iconSize,
  labelTop,
}: {
  taskIconX: number;
  iconSize: number;
  labelTop: number;
}) {
  const PANEL_W = 40;
  const center = taskIconX + iconSize / 2;
  const panelLeft = center - PANEL_W / 2;
  // 标签大约 10px 高(8px 字号 + 行高),面板贴在标签下
  // 向上移让底图盖住 "任务" 标签 — labelTop - 4 ≈ 89,标签(在 93)就坐进面板里
  const panelTop = labelTop - 4;
  // ▼ 倒三角放在标签右侧(标签 "任务" 之后)
  const chevronLeft = center + 5;
  const chevronTop = labelTop + 2;
  return (
    <View pointerEvents="box-none">
      {/* 标签旁的倒三角 ▼ */}
      <Text
        style={{
          position: 'absolute',
          left: chevronLeft,
          top: chevronTop,
          fontSize: 6,
          lineHeight: 8,
          color: TEXT_DARK,
        }}>
        ▼
      </Text>
      {/* 下拉面板 — 底图 Rectangle 187(竹叶 bokeh),再叠内容 */}
      <View
        style={{
          position: 'absolute',
          left: panelLeft,
          top: panelTop,
          width: PANEL_W,
          height: 36,
          borderRadius: 6,
          overflow: 'hidden', // 圆角裁掉底图
        }}>
        {/* 底图(撑满,cover 等比缩放填满容器)— 半透明,这样能透出下面的"任务"文字 */}
        <Image
          source={rectBgImage}
          style={[StyleSheet.absoluteFill, { opacity: 0.5 }]}
          resizeMode="cover"
          // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
          pointerEvents="none"
        />
        {/* 内容层 */}
        <View
          style={{
            position: 'absolute',
            left: 0,
            right: 0,
            top: 0,
            bottom: 0,
            paddingVertical: 3,
            paddingHorizontal: 4,
            justifyContent: 'center', // 两行整体居中,这样"任务"和"挑战"上下对齐
          }}>
          {/* 只显示 "挑战" 文字 + 箭头(任务行已删)— 文字改黑色,跟任务标签同色 */}
          <View
            style={{
              flexDirection: 'row',
              alignItems: 'center',
              justifyContent: 'center',
              marginTop: 6, // 给"挑战"行一点位置(向下挪),跟任务文字隔开
            }}>
            <Text
              style={{
                fontSize: 8, // 跟"任务"标签字号对齐(原来 6)
                color: TEXT_DARK, // 改成黑色(原来是白色)
                fontFamily: 'Microsoft YaHei',
                marginRight: 2,
              }}>
              挑战
            </Text>
            <SvgXml xml={ARROW_SVG} width="5" height="5" />
          </View>
        </View>
      </View>
    </View>
  );
}

/* —— "我的学习进度" 弹窗(Figma 节点 342:2597)
 *  点"进度"图标打开,点 × 关闭
 *  遮罩 + 居中卡片(米黄底,跟气泡同色调)
 */
function ProgressModal({
  onClose,
  onOpenDaily,
  onOpenLuggage,
}: {
  onClose: () => void;
  onOpenDaily: () => void;
  onOpenLuggage: () => void;
}) {
  return (
    <View
      style={{
        position: 'absolute',
        left: 0,
        top: 0,
        width: 412,
        height: 810, // 跟 canvasH 一致
      }}>
      {/* 半透明遮罩 */}
      <Pressable
        onPress={onClose}
        style={{
          position: 'absolute',
          left: 0,
          top: 0,
          width: 412,
          height: 810,
          backgroundColor: 'rgba(0,0,0,0.35)',
        }}
      />
      {/* 弹窗卡片 */}
      <View
        style={{
          position: 'absolute',
          left: 20,
          top: 80,
          width: 372,
          height: 660,
          borderRadius: 12,
          overflow: 'hidden', // 圆角裁掉底图
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 4 },
          shadowOpacity: 0.25,
          shadowRadius: 8,
          elevation: 6,
        }}>
        {/* 底图(Figma 342:2597 同款竹叶渐变) */}
        <Image
          source={progressModalBg}
          style={StyleSheet.absoluteFill}
          resizeMode="cover"
        />

        {/* 内容容器(整体内边距)— 标题、用户信息、两个内容区 */}
        <View style={{ flex: 1, padding: 20 }}>
          {/* 标题 */}
          <Text
            style={{
              fontSize: 18,
              fontWeight: '600',
              color: TEXT_DARK,
              fontFamily: 'Microsoft YaHei',
              textAlign: 'center',
              marginTop: 2,
            }}>
            我的学习进度
          </Text>

          {/* 用户信息行:头像 + 名字/等级 */}
          <View
            style={{
              flexDirection: 'row',
              alignItems: 'center',
              marginTop: 18,
            }}>
            {/* 头像:用 byhuo.png 替换之前的占位(49x48 原图) */}
            <Image
              source={avatarImage}
              style={{
                width: 56,
                height: 56,
                borderRadius: 28, // 圆形
                marginRight: 12,
              }}
              resizeMode="cover"
            />
            <View style={{ flex: 1 }}>
              <Text
                style={{
                  fontSize: 13,
                  color: TEXT_DARK,
                  fontFamily: 'Microsoft YaHei',
                  marginBottom: 4,
                }}>
                名字:阿砚
              </Text>
              <Text
                style={{
                  fontSize: 13,
                  color: TEXT_DARK,
                  fontFamily: 'Microsoft YaHei',
                }}>
                等级:见习弟子
              </Text>
            </View>
          </View>

          {/* 每日生活问题推荐: — 可点击,跳到每日问题(Figma 321:2187) */}
          <Pressable
            onPress={onOpenDaily}
            hitSlop={6}>
            <Text
              style={{
                fontSize: 13,
                color: TEXT_DARK,
                fontFamily: 'Microsoft YaHei',
                marginTop: 18,
                marginBottom: 8,
                textDecorationLine: 'underline', // 视觉上提示可点
              }}>
              每日生活问题推荐:
            </Text>
          </Pressable>
          <View
            style={{
              minHeight: 120,
              borderWidth: 1,
              borderColor: 'rgba(0,0,0,0.08)',
              borderRadius: 6,
            }}
          />

          {/* 每日任务进度: */}
          <Text
            style={{
              fontSize: 13,
              color: TEXT_DARK,
              fontFamily: 'Microsoft YaHei',
              marginTop: 14,
              marginBottom: 8,
            }}>
            每日任务进度:
          </Text>
          <View
            style={{
              minHeight: 100,
              borderWidth: 1,
              borderColor: 'rgba(0,0,0,0.08)',
              borderRadius: 6,
            }}
          />
        </View>

        {/* 底部右:"详情请查看行囊" + 箭头 */}
        <Pressable
          onPress={onOpenLuggage}
          style={{
            position: 'absolute',
            right: 20,
            bottom: 100,
            flexDirection: 'row',
            alignItems: 'center',
          }}>
          <Text
            style={{
              fontSize: 12,
              color: TEXT_DARK,
              fontFamily: 'Microsoft YaHei',
              marginRight: 4,
            }}>
            详情请查看行囊
          </Text>
          <SvgXml xml={ARROW_SVG} width="8" height="8" />
        </Pressable>

        {/* 右下角装饰图(dfggtr.png,223x244 原图)— absolute 定位在卡片右下 */}
        <Image
          source={cornerImage}
          style={{
            position: 'absolute',
            right: -50,
            bottom: 85,
            width: 223,
            height: 244,
          }}
          resizeMode="contain"
          // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
          pointerEvents="none"
        />

        {/* 关闭按钮 × — 放在最末尾 = z-order 最顶层,不会被标题/头像挡住 */}
        <Pressable
          onPress={onClose}
          hitSlop={16}
          style={({ pressed }) => ({
            position: 'absolute',
            left: 8,
            top: 8,
            width: 32,
            height: 32,
            alignItems: 'center',
            justifyContent: 'center',
            opacity: pressed ? 0.5 : 1,
          })}>
          <Text style={{ fontSize: 24, color: TEXT_DARK, lineHeight: 28 }}>×</Text>
        </Pressable>
      </View>
    </View>
  );
}

/* —— "行囊" 页(Figma 397:2400)— 点击"行囊"横幅触发
 *  顶部 × + 标题 + 用户信息(头像/名字/班级/ID)+ 等级勋章
 *  + 签到统计 + 3 个可折叠区(获得的秘籍 / 我的错题 / 我的作品)
 */
function LuggagePage({ onClose }: { onClose: () => void }) {
  return (
    <View
      style={{
        position: 'absolute',
        left: 0,
        top: 0,
        width: 412,
        height: 810,
      }}>
      {/* 背景沿用主页 bg.png */}
      <Image
        source={bgImage}
        style={StyleSheet.absoluteFill}
        resizeMode="cover"
      />

      {/* 关闭按钮 × — 最顶层 */}
      <Pressable
        onPress={onClose}
        hitSlop={16}
        style={({ pressed }) => ({
          position: 'absolute',
          left: 28,
          top: 95,
          width: 32,
          height: 32,
          alignItems: 'center',
          justifyContent: 'center',
          opacity: pressed ? 0.5 : 1,
          zIndex: 10,
        })}>
        <Text style={{ fontSize: 22, color: TEXT_DARK }}>×</Text>
      </Pressable>

      {/* 标题"行囊" */}
      <Text
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          top: 122,
          fontSize: 22,
          fontWeight: '600',
          color: TEXT_DARK,
          fontFamily: 'Microsoft YaHei',
          textAlign: 'center',
          zIndex: 5,
        }}>
        行囊
      </Text>

      {/* 行李卡片 — 底图 image 14.png 撑满,米黄底色兜底防透明 */}
      <View
        style={{
          position: 'absolute',
          left: 20,
          top: 117,
          width: 372,
          height: 750,
          backgroundColor: '#F4E6CF',
          borderRadius: 12,
          overflow: 'hidden',
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 4 },
          shadowOpacity: 0.2,
          shadowRadius: 6,
          elevation: 4,
        }}>
        {/* 底图(image 14.png)撑满 */}
        <Image
          source={luggageCardBg}
          style={StyleSheet.absoluteFill}
          resizeMode="cover"
          // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents
          pointerEvents="none"
        />
        {/* 内容容器 — 在底图上方 */}
        <View style={{ flex: 1, padding: 16 }}>
          {/* 头像 + 名字/班级/ID 行 */}
          <View
            style={{
              flexDirection: 'row',
              alignItems: 'center',
              marginTop: 24,
            }}>
          {/* 头像:圆框底图(Ellipse 25.png) + byhuo.png 真人头像 */}
          <View
            style={{
              width: 70,
              height: 70,
              alignItems: 'center',
              justifyContent: 'center',
            }}>
            {/* 底层圆框底图(76x72 原图) */}
            <Image
              source={luggageAvatarBg}
              style={StyleSheet.absoluteFill}
              resizeMode="contain"
            />
            {/* 上层真人头像(byhuo.png,49x48 原图,显示 36x36,套在圆框内) */}
            <Image
              source={avatarImage}
              style={{ width: 60, height: 60, borderRadius: 10 }}
              resizeMode="cover"
            />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Text
                style={{
                  fontSize: 16,
                  fontWeight: '600',
                  color: TEXT_DARK,
                  fontFamily: 'Microsoft YaHei',
                }}>
                哈哈哈
              </Text>
              <Text
                style={{
                  fontSize: 12, // 五(三)班 字号(原来 11)
                  color: TEXT_DARK,
                  fontFamily: 'Microsoft YaHei',
                  marginLeft: 8,
                }}>
                五(三)班
              </Text>
            </View>
            <Text
              style={{
                fontSize: 12, // ID 字号(原来 11)
                color: 'rgba(0,0,0,0.55)',
                fontFamily: 'Microsoft YaHei',
                marginTop: 2,
              }}>
              ID: 1326528988
            </Text>
            {/* 等级 + 勋章 badge 行 */}
            <View
              style={{
                flexDirection: 'row',
                marginTop: 6,
              }}>
              {/* "见习弟子" 勋章框 — 用 terw.png(83x21)作背景 */}
              <View
                style={{
                  width: 83, // 与原图等宽
                  height: 21, // 与原图等高
                  marginRight: 6,
                  alignItems: 'center',
                  justifyContent: 'center',
                  overflow: 'hidden',
                  borderRadius: 8,
                }}>
                <Image
                  source={levelBadgeBg}
                  style={StyleSheet.absoluteFill}
                  resizeMode="stretch"
                />
                <Text
                  style={{
                    fontSize: 10,
                    color: TEXT_DARK, // 黑色(原来是 #FFFFFF 跟金色底图撞)
                    fontFamily: 'Microsoft YaHei',
                  }}>
                  见习弟子
                </Text>
              </View>
              <View
                style={{
                  backgroundColor: '#7FA988',
                  borderRadius: 8,
                  paddingHorizontal: 8,
                  paddingVertical: 2,
                }}>
                <Text
                  style={{
                    fontSize: 10,
                    color: '#FFFFFF',
                    fontFamily: 'Microsoft YaHei',
                  }}>
                  勋章
                </Text>
              </View>
            </View>
          </View>
        </View>

        {/* 签到统计 */}
        <Text
          style={{
            fontSize: 12,
            color: TEXT_DARK,
            fontFamily: 'Microsoft YaHei',
            marginTop: 14,
          }}>
          签到:3天  累计修行:7天  已通关试炼:5个
        </Text>

        {/* 获得的秘籍 区 */}
        <Section
          title="获得的秘籍:"
          defaultOpen
          content={
            <View style={{ flexDirection: 'row', flexWrap: 'wrap' }}>
              <ManualItem name="《拆招心法》" />
              <ManualItem name="《识机真诀》" />
              <ManualItem name="《百炼识物诀》" />
            </View>
          }
        />

        {/* 我的错题 区 — 背景色 #8E9A75 */}
        <Section
          title="我的错题:"
          bgColor="#8E9A75"
          content={
            <Text
              style={{
                fontSize: 12,
                color: 'rgba(0,0,0,0.45)',
                fontFamily: 'Microsoft YaHei',
                padding: 8,
              }}>
              暂无错题
            </Text>
          }
        />

        {/* 我的作品 区 */}
        <Section
          title="我的作品:"
          content={
            <View style={{ flexDirection: 'row' }}>
              <WorkItem label="作品 1" />
              <WorkItem label="作品 2" />
            </View>
          }
        />
        </View>
      </View>
    </View>
  );
}

/* —— 行囊页内的可折叠小节(浅绿底 + 标题 + 倒三角)—— */
function Section({
  title,
  content,
  defaultOpen = false,
  bgColor = 'rgba(127,169,136,0.18)', // 默认浅绿
}: {
  title: string;
  content: React.ReactNode;
  defaultOpen?: boolean;
  /** 自定义背景色,缺省 = 浅绿 */
  bgColor?: string;
}) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <View
      style={{
        marginTop: 14,
        backgroundColor: bgColor,
        borderRadius: 8,
        overflow: 'hidden',
      }}>
      <Pressable
        onPress={() => setOpen((v) => !v)}
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'space-between',
          paddingHorizontal: 10,
          paddingVertical: 8,
        }}>
        <Text
          style={{
            fontSize: 13,
            fontWeight: '600',
            color: TEXT_DARK,
            fontFamily: 'Microsoft YaHei',
          }}>
          {title}
        </Text>
        <Text
          style={{
            fontSize: 12,
            color: TEXT_DARK,
            transform: [{ rotate: open ? '0deg' : '180deg' }],
          }}>
          ▼
        </Text>
      </Pressable>
      {open && (
        <View
          style={{
            paddingHorizontal: 10,
            paddingVertical: 8,
          }}>
          {content}
        </View>
      )}
    </View>
  );
}

/* —— 秘籍格子(占位图 + 书名)—— */
function ManualItem({ name }: { name: string }) {
  return (
    <View style={{ alignItems: 'center', marginRight: 14, marginBottom: 6 }}>
      <View
        style={{
          width: 44,
          height: 44,
          borderRadius: 4,
          backgroundColor: '#D9A86C',
          borderWidth: 1,
          borderColor: 'rgba(0,0,0,0.15)',
        }}
      />
      <Text
        style={{
          fontSize: 10,
          color: TEXT_DARK,
          fontFamily: 'Microsoft YaHei',
          marginTop: 2,
        }}>
        {name}
      </Text>
    </View>
  );
}

/* —— 作品格子(占位图 + 名称)—— */
function WorkItem({ label }: { label: string }) {
  return (
    <View style={{ alignItems: 'center', marginRight: 14 }}>
      <View
        style={{
          width: 60,
          height: 80,
          borderRadius: 4,
          backgroundColor: '#7FA988',
          borderWidth: 1,
          borderColor: 'rgba(0,0,0,0.15)',
        }}
      />
      <Text
        style={{
          fontSize: 10,
          color: TEXT_DARK,
          fontFamily: 'Microsoft YaHei',
          marginTop: 2,
        }}>
        {label}
      </Text>
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