/**
 * 登录 / 注册 / 忘记密码页共用的内联 SVG 图标组件。
 *
 * 为什么内联 SVG 而不是 `<Image source={x.svg}>` ?
 * - RN 原生端 `<Image>` 不渲染 SVG(只在 web 上能用)
 * - react-native-svg 的 `<Svg><Path/></Svg>` 是 native + web 都通用
 *
 * 三个图标都从 Figma 直接复制的 SVG path,保留原始 stroke / strokeWidth。
 */

import { StyleSheet, View } from 'react-native';
import Svg, { Path } from 'react-native-svg';

const ICON_STROKE = '#949494';
const ICON_STROKE_WIDTH = 1.83;

/* —— 手机图标:外壳 + 听筒 + home 键 3 段 SVG 叠加 —— */
export function PhoneIcon() {
  return (
    <View style={phoneStyles.frame}>
      <Svg
        width="15.375"
        height="22.67"
        viewBox="0 0 15.375 22.67"
        style={phoneStyles.body}>
        <Path
          d="M12.9 0.92H2.48C1.62 0.92 0.92 1.62 0.92 2.48V20.19C0.92 21.05 1.62 21.75 2.48 21.75H12.9C13.76 21.75 14.46 21.05 14.46 20.19V2.48C14.46 1.62 13.76 0.92 12.9 0.92Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          fill="none"
        />
      </Svg>
      <Svg
        width="3.92"
        height="1.83"
        viewBox="0 0 3.92 1.83"
        style={phoneStyles.speaker}>
        <Path
          d="M0.92 0.92H3"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      <Svg
        width="6"
        height="1.83"
        viewBox="0 0 6 1.83"
        style={phoneStyles.home}>
        <Path
          d="M0.92 0.92H5.08"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
    </View>
  );
}

const phoneStyles = StyleSheet.create({
  frame: { width: 25, height: 25, marginRight: 14 },
  body: { position: 'absolute', top: 1, left: 4 },
  speaker: { position: 'absolute', top: 5, left: 10 },
  home: { position: 'absolute', bottom: 5, left: 9 },
});

/* —— 钥匙图标:头/柄/齿 3 段 SVG 叠加 —— */
export function KeyIcon() {
  return (
    <View style={keyStyles.frame}>
      <Svg
        width="10.92"
        height="10.87"
        viewBox="0 0 10.92 10.87"
        style={keyStyles.body}>
        <Path
          d="M8.65 2.19C9.81 3.33 10.27 5.01 9.85 6.58C9.43 8.16 8.2 9.39 6.61 9.8C5.03 10.22 3.35 9.77 2.2 8.61C0.47 6.83 0.49 3.99 2.25 2.25C4.01 0.49 6.86 0.47 8.65 2.19Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      <Svg
        width="9.625"
        height="9.625"
        viewBox="0 0 9.625 9.625"
        style={keyStyles.ring}>
        <Path
          d="M0.92 8.71L8.71 0.92"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      <Svg
        width="7.22"
        height="7.20"
        viewBox="0 0 7.22 7.20"
        style={keyStyles.bow}>
        <Path
          d="M0.92 3.80L3.40 6.28L6.31 3.39L3.82 0.92L0.92 3.80Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
    </View>
  );
}

const keyStyles = StyleSheet.create({
  frame: { width: 22, height: 22, marginRight: 12 },
  body: { position: 'absolute', top: 11, right: 10, bottom: 3, left: 3 },
  ring: { position: 'absolute', top: 3, left: 11, right: 4, bottom: 9 },
  bow: { position: 'absolute', top: 5, right: 3, bottom: 12, left: 14 },
});

/* —— 人身安全(盾牌小人)图标 —— */
export function PeopleSafeIcon() {
  return (
    <View style={peopleSafeStyles.frame}>
      <Svg
        width="17"
        height="17"
        viewBox="0 0 17 17"
        style={peopleSafeStyles.body}>
        <Path
          d="M1.5 1.5H15.5V15.5H1.5Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          fill="none"
        />
      </Svg>
      <Svg
        width="6"
        height="2.5"
        viewBox="0 0 6 2.5"
        style={peopleSafeStyles.top}>
        <Path
          d="M0.5 0.5H5.5"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      <Svg
        width="6"
        height="3"
        viewBox="0 0 6 3"
        style={peopleSafeStyles.mid}>
        <Path
          d="M0.5 0.5H5.5"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
    </View>
  );
}

const peopleSafeStyles = StyleSheet.create({
  frame: { width: 22, height: 22, marginRight: 12 },
  body: {
    position: 'absolute',
    top: 2,
    left: 3,
    right: 3,
    bottom: 2,
  },
  top: {
    position: 'absolute',
    top: 6,
    left: 9,
    right: 9,
    height: 2.5,
  },
  mid: {
    position: 'absolute',
    top: 10.5,
    left: 7,
    right: 7,
    height: 3,
  },
});