import { useEffect, useState } from 'react';
import { useRouter } from 'expo-router';
import {
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Svg, { Path, SvgXml } from 'react-native-svg';

import bgPaper from '@/assets/images/login/bg-paper.png';
import mascot from '@/assets/images/login/mascot.png';
import peopleSafe from '@/assets/images/login/People-safe.png';

/* —— SVG 字符串常量 —— *
 * 复杂 SVG(渐变、滤镜)用 SvgXml 让 react-native-svg 自己解析;
 * 图标这种简单 path 直接用 <Svg><Path/> 内联,体积更小。
 */

const SVG_BTN_LOGIN = `<svg preserveAspectRatio="none" width="310" height="54" viewBox="0 0 310 54" fill="none" xmlns="http://www.w3.org/2000/svg"><g><path d="M22.67 0.53C16.21 0.53 14.16 6.47 14.16 8.99C11.51 8.99 4 10.38 4 22.64C4 32.62 10.51 37.29 14.16 37.29C14.16 42.85 19.53 45.01 21.67 45C108.15 44.66 282.02 45 286.11 45C290.98 45 295.63 41.33 295.63 37.93C301.99 37.93 306 32.62 306 21.12C306 11.9 300.49 8.99 295.63 8.99C295.63 3.44 287.97 0.03 284.11 0.03C199.63 -0.14 29.54 0.53 22.67 0.53Z" fill="url(#g0)"/><path d="M27.48 4.44C21.25 4.44 19.27 9.32 19.27 11.4C16.72 11.4 9.48 12.54 9.48 22.61C9.48 30.82 15.76 34.66 19.27 34.66C19.27 39.23 24.44 41.01 26.51 41C109.85 40.72 277.41 41 281.35 41C286.04 41 290.52 37.99 290.52 35.18C296.66 35.18 300.52 30.82 300.52 21.37C300.52 13.78 295.21 11.4 290.52 11.4C290.52 6.83 283.14 4.03 279.42 4.02C198.01 3.88 34.1 4.44 27.48 4.44Z" fill="url(#g1)" stroke="url(#g2)"/></g><defs><linearGradient id="g0" x1="155" y1="0" x2="155" y2="33" gradientUnits="userSpaceOnUse"><stop stop-color="#AACC99"/><stop offset="1" stop-color="#546942"/></linearGradient><linearGradient id="g1" x1="155" y1="41" x2="155" y2="4" gradientUnits="userSpaceOnUse"><stop stop-color="#527F50"/><stop offset="1" stop-color="#92B57A"/></linearGradient><linearGradient id="g2" x1="9.48" y1="22.5" x2="300.52" y2="22.5" gradientUnits="userSpaceOnUse"><stop stop-color="#DCCCA1"/><stop offset="1" stop-color="#FAF4D8"/></linearGradient></defs></svg>`;

const SVG_CARD_FRAME = `<svg preserveAspectRatio="none" width="408" height="373" viewBox="0 0 408 373" fill="none" xmlns="http://www.w3.org/2000/svg"><g><path d="M17.56 35.94C18.76 19.86 26.57 17.72 30.9 17.72C34.89 5.44 43.68 2.26 47.01 3.14H361.45C371.03 3.14 375.09 13.01 375.92 17.95C384.71 16.68 388.24 26.94 388.9 32.23V308.37C388.1 318.1 379.92 323.71 375.92 325.3C370.73 335.88 364.11 338.17 361.45 337.99H47.01C36.63 335.03 33.03 327.94 32.53 324.77C20.56 321.38 17.56 311.37 17.56 306.78C17.06 223.2 16.36 52.02 17.56 35.94Z" fill="#F2E6D1"/><path d="M17.56 35.94C18.76 19.86 26.57 17.72 30.9 17.72C34.89 5.44 43.68 2.26 47.01 3.14H361.45C371.03 3.14 375.09 13.01 375.92 17.95C384.71 16.68 388.24 26.94 388.9 32.23V308.37C388.1 318.1 379.92 323.71 375.92 325.3C370.73 335.88 364.11 338.17 361.45 337.99H47.01C36.63 335.03 33.03 327.94 32.53 324.77C20.56 321.38 17.56 311.37 17.56 306.78C17.06 223.2 16.36 52.02 17.56 35.94Z" stroke="#E7DCAE" stroke-width="6"/></g><g><path d="M36.49 50.32C37.57 36.01 45.48 34.02 49.36 34.02C51.85 22.52 59.95 20.36 62.93 21.15H345.25C353.86 21.15 357.5 29.93 358.25 34.32C366.14 33.19 369.3 42.32 369.9 47.02V292.64C369.18 301.3 365.4 306.45 359.9 306.45C358.05 317.74 347.64 319.15 345.25 318.99H62.93C53.9 318.99 50.9 318.99 47.86 305.4C37.11 302.39 36.49 295.31 36.49 291.23C36.05 216.89 35.42 64.62 36.49 50.32Z" stroke="#E7DCAE" stroke-width="2"/></g></svg>`;

const SVG_CHECKBOX = `<svg width="9" height="9" viewBox="0 0 9 9" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="4.5" cy="4.5" r="4" stroke="#A7AD8E"/></svg>`;

/**
 * 登录 * 纯密码登录(Figma 节点 413:3271)。
 *
 * v4 改版要点(v3 → v4):
 * - 把所有 SVG 从 `<Image source={x.svg}>` 换成 react-native-svg 的
 *   `<SvgXml>` / `<Svg><Path/>`,因为 RN 原生端 `<Image>` 不渲染 SVG
 * - 复杂 SVG(按钮背景 / 卡框 / 勾选框)用 `SvgXml` 让库解析渐变
 * - 图标(手机/钥匙)用内联 `<Svg><Path/>`,3 段绝对定位叠加
 *
 * 设计 token(来源 Figma 413:3271 实测,基线 412 × 917 dp):
 *   背景米黄     #F5E8D4
 *   输入框边框   #DCCCA1
 *   协议/链接    #A7AD8E
 *   placeholder  #939393
 */

const BG_CREAM = '#F5E8D4';
const INPUT_BORDER = '#DCCCA1';
const LINK_OLIVE = '#A7AD8E';
const PLACEHOLDER = '#939393';
const TEXT_DARK = '#000000';
const ICON_STROKE = '#949494';
const ICON_STROKE_WIDTH = 1.83;

const Fig = {
  canvasW: 412,
  canvasH: 800,
  bgPaperLeft: -124,
  bgPaperTop: 0,
  bgPaperW: 536,
  bgPaperH: 817,
  welcomeLeft: 20,
  welcomeTop: 110,
  welcomeW: 278,
  mascotLeft: 286,
  mascotTop: 307,
  mascotW: 111,
  mascotH: 197,
  cardLeft: 20,
  cardTop: 485,
  cardW: 372,
  cardH: 335,
  phoneInput: { left: 59, top: 547, w: 294, h: 40 },
  passwordInput: { left: 59, top: 600, w: 294, h: 40 },
  forgot: { left: 59, top: 652 },
  agreement: { left: 90, top: 700 },
  button: { left: 55, top: 720, w: 302, h: 50 },
} as const;

type LoginMode = 'code' | 'password';

export default function LoginScreen() {
  const router = useRouter();
  const [mode, setMode] = useState<LoginMode>('password');
  const [phone, setPhone] = useState('');
  const [secret, setSecret] = useState('');
  /** 注册模式下,验证码之外还要设置登录密码 */
  const [registerPassword, setRegisterPassword] = useState('');
  const [agreed, setAgreed] = useState(false);
  /** 验证码冷却倒计时(秒),0 = 未冷却可发送 */
  const [countdown, setCountdown] = useState(0);

  /**
   * 倒计时自动递减:每次 countdown > 0 时启动 1s 定时器,
   * 组件卸载或 countdown 触底时通过 cleanup 自动清除,
   * 无需手动管理 timer ID。
   */
  useEffect(() => {
    if (countdown <= 0) return;
    const id = setInterval(() => {
      setCountdown((c) => Math.max(0, c - 1));
    }, 1000);
    return () => clearInterval(id);
  }, [countdown]);

  /**
   * 触发发送验证码。冷却期内重复点击会被忽略。
   * 真实场景应:先 POST `/api/sms/send` → 成功后启动倒计时 → 失败给出 toast。
   */
  const handleSendCode = () => {
    if (countdown > 0) return;
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      console.warn('[login] 发送验证码前请先输入正确手机号');
      return;
    }
    // TODO: 接入短信发送 API
    setCountdown(60);
  };

  const handleLogin = () => {
    if (!phone.trim()) {
      console.warn('[login] 请输入手机号');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      console.warn('[login] 手机号格式不正确');
      return;
    }
    if (!secret.trim()) {
      console.warn(`[login] 请输入${mode === 'code' ? '验证码' : '密码'}`);
      return;
    }
    if (mode === 'code' && secret.length !== 6) {
      console.warn('[login] 验证码需为 6 位');
      return;
    }
    if (mode === 'password' && secret.length < 6) {
      console.warn('[login] 密码至少 6 位');
      return;
    }
    if (mode === 'code' && registerPassword.length < 6) {
      console.warn('[login] 请设置至少 6 位登录密码');
      return;
    }
    if (!agreed) {
      console.warn('[login] 未勾选用户协议');
      return;
    }
    // TODO: 接入登录 API
    router.replace('/(tabs)');
  };

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>
        {/* 412×800 画布,所有元素绝对定位对应 Figma 坐标 */}
        <View style={styles.canvas}>
          {/* 1. 背景旧纸(PNG) */}
          <Image
            source={bgPaper}
            style={styles.bgPaper}
            resizeMode="cover"
            pointerEvents="none"
          />

          {/* 2. 熊猫吉祥物(右上,水平翻转) */}
          <Image
            source={mascot}
            style={styles.mascot}
            resizeMode="contain"
            pointerEvents="none"
          />

          {/* 3. WELCOME 标题 */}
          <View style={styles.welcome}>
            <Text
              style={styles.welcomeTitle}
              maxFontSizeMultiplier={1.25}
              allowFontScaling>
              WELCOME
            </Text>
            <Text
              style={styles.welcomeSubtitle}
              maxFontSizeMultiplier={1.4}
              allowFontScaling>
              欢迎来到机巧江湖
            </Text>
          </View>

          {/* 4. 密码登录卡 */}
          <View style={styles.card}>
            <View style={StyleSheet.absoluteFill} pointerEvents="none">
              <SvgXml xml={SVG_CARD_FRAME} width="100%" height="100%" />
            </View>

            {/* 登录方式切换(密码 / 验证码)— 卡内顶部居中 */}
            <View style={styles.modeToggle}>
              <Pressable
                accessibilityRole="tab"
                accessibilityState={{ selected: mode === 'password' }}
                onPress={() => setMode('password')}
                hitSlop={4}
                style={[
                  styles.modeBtn,
                  mode === 'password' && styles.modeBtnActive,
                ]}>
                <Text
                  style={[
                    styles.modeBtnText,
                    mode === 'password' && styles.modeBtnTextActive,
                  ]}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling>
                  登录
                </Text>
              </Pressable>
              <Pressable
                accessibilityRole="tab"
                accessibilityState={{ selected: mode === 'code' }}
                onPress={() => setMode('code')}
                hitSlop={4}
                style={[
                  styles.modeBtn,
                  mode === 'code' && styles.modeBtnActive,
                ]}>
                <Text
                  style={[
                    styles.modeBtnText,
                    mode === 'code' && styles.modeBtnTextActive,
                  ]}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling>
                  注册
                </Text>
              </Pressable>
            </View>

            {/* 手机号输入框 */}
            <View style={styles.inputBox}>
              <PhoneIcon />
              <TextInput
                style={styles.input}
                placeholder="请输入手机号"
                placeholderTextColor={PLACEHOLDER}
                keyboardType="phone-pad"
                maxLength={11}
                value={phone}
                onChangeText={setPhone}
                maxFontSizeMultiplier={1.4}
                allowFontScaling
              />
            </View>

            {/* 第二个输入框:验证码 或 密码 */}
            <View style={[styles.inputBox, styles.passwordInput]}>
              {mode === 'password' ? (
                <KeyIcon />
              ) : (
                <Image
                  source={peopleSafe}
                  style={codeIconStyles.icon}
                  resizeMode="contain"
                />
              )}
              <TextInput
                style={styles.input}
                placeholder={mode === 'code' ? '请输入验证码' : '请输入密码'}
                placeholderTextColor={PLACEHOLDER}
                keyboardType={mode === 'code' ? 'number-pad' : 'default'}
                secureTextEntry={mode === 'password'}
                maxLength={mode === 'code' ? 6 : 24}
                value={secret}
                onChangeText={setSecret}
                maxFontSizeMultiplier={1.4}
                allowFontScaling
              />
              {mode === 'code' && (
                <>
                  <View style={styles.divider} />
                  <Pressable
                    hitSlop={8}
                    disabled={countdown > 0}
                    accessibilityRole="button"
                    accessibilityState={{ disabled: countdown > 0 }}
                    accessibilityLabel={
                      countdown > 0
                        ? `${countdown} 秒后可重新发送验证码`
                        : '发送验证码'
                    }
                    onPress={handleSendCode}>
                    <Text
                      style={[
                        styles.actionText,
                        countdown > 0 && styles.actionTextDisabled,
                      ]}
                      maxFontSizeMultiplier={1.6}
                      allowFontScaling>
                      {countdown > 0 ? `${countdown}s 后重发` : '获取验证码'}
                    </Text>
                  </Pressable>
                </>
              )}
            </View>

            {/* 仅密码模式:忘记密码链接 */}
            {mode === 'password' && (
              <Pressable
                accessibilityRole="link"
                accessibilityLabel="忘记密码 找回密码"
                onPress={() => router.push('/forgot')}
                hitSlop={8}
                style={styles.forgotWrap}>
                <Text
                  style={styles.linkText}
                  maxFontSizeMultiplier={1.6}
                  allowFontScaling>
                  忘记密码?找回密码
                </Text>
              </Pressable>
            )}

            {/* 仅注册模式:设置登录密码(占"忘记密码"行的位置) */}
            {mode === 'code' && (
              <View style={[styles.inputBox, styles.registerPasswordInput]}>
                <KeyIcon />
                <TextInput
                  style={styles.input}
                  placeholder="请输入密码"
                  placeholderTextColor={PLACEHOLDER}
                  secureTextEntry
                  maxLength={24}
                  value={registerPassword}
                  onChangeText={setRegisterPassword}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling
                />
              </View>
            )}

            {/* 协议行 */}
            <View style={styles.agreementRow}>
              <Pressable
                accessibilityRole="checkbox"
                accessibilityState={{ checked: agreed }}
                onPress={() => setAgreed((v) => !v)}
                hitSlop={8}
                style={styles.checkboxWrap}>
                {agreed ? (
                  <View style={styles.checkboxOn}>
                    <Text style={styles.checkmark}>✓</Text>
                  </View>
                ) : (
                  <SvgXml
                    xml={SVG_CHECKBOX}
                    width="9"
                    height="9"
                    style={styles.checkboxImg}
                  />
                )}
              </Pressable>
              <Text
                style={styles.agreementText}
                maxFontSizeMultiplier={1.6}
                allowFontScaling>
                我已阅读并同意
                <Text
                  style={styles.linkInline}
                  onPress={() => {
                    /* TODO: 跳《用户协议》 */
                  }}>
                  《用户协议》
                </Text>
                和
                <Text
                  style={styles.linkInline}
                  onPress={() => {
                    /* TODO: 跳《隐私条款》 */
                  }}>
                  《隐私条款》
                </Text>
              </Text>
            </View>

            {/* 登录 / 注册按钮 */}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={mode === 'code' ? '注册' : '登录'}
              onPress={handleLogin}
              style={({ pressed }) => [
                styles.loginBtn,
                pressed && styles.btnPressed,
              ]}>
              <View style={StyleSheet.absoluteFill} pointerEvents="none">
                <SvgXml xml={SVG_BTN_LOGIN} width="100%" height="100%" />
              </View>
              <Text
                style={styles.loginBtnText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                {mode === 'code' ? '注册' : '登录'}
              </Text>
            </Pressable>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

/* —— 图标组件 —— */

function PhoneIcon() {
  return (
    <View style={phoneStyles.frame}>
      {/* 手机外壳:圆角矩形 15.375 × 22.67 */}
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
      {/* 顶部听筒:水平短线 3.92 × 1.83 */}
      <Svg width="3.92" height="1.83" viewBox="0 0 3.92 1.83" style={phoneStyles.speaker}>
        <Path
          d="M0.92 0.92H3"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      {/* 底部 home 键:水平短线 6 × 1.83 */}
      <Svg width="6" height="1.83" viewBox="0 0 6 1.83" style={phoneStyles.home}>
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
  frame: {
    width: 25,
    height: 25,
    marginRight: 14,
  },
  body: {
    position: 'absolute',
    top: 1,
    left: 4,
  },
  speaker: {
    position: 'absolute',
    top: 5,
    left: 10,
  },
  home: {
    position: 'absolute',
    bottom: 5,
    left: 9,
  },
});

function KeyIcon() {
  return (
    <View style={keyStyles.frame}>
      {/* 钥匙头:圆 10.92 × 10.87 */}
      <Svg width="10.92" height="10.87" viewBox="0 0 10.92 10.87" style={keyStyles.body}>
        <Path
          d="M8.65 2.19C9.81 3.33 10.27 5.01 9.85 6.58C9.43 8.16 8.2 9.39 6.61 9.8C5.03 10.22 3.35 9.77 2.2 8.61C0.47 6.83 0.49 3.99 2.25 2.25C4.01 0.49 6.86 0.47 8.65 2.19Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      {/* 对角线:9.625 × 9.625 */}
      <Svg width="9.625" height="9.625" viewBox="0 0 9.625 9.625" style={keyStyles.ring}>
        <Path
          d="M0.92 8.71L8.71 0.92"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      {/* 钥匙齿:Z 形 7.22 × 7.20 */}
      <Svg width="7.22" height="7.20" viewBox="0 0 7.22 7.20" style={keyStyles.bow}>
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
  frame: {
    width: 22,
    height: 22,
    marginRight: 12,
  },
  body: {
    position: 'absolute',
    top: 11,
    right: 10,
    bottom: 3,
    left: 3,
  },
  ring: {
    position: 'absolute',
    top: 3,
    left: 11,
    right: 4,
    bottom: 9,
  },
  bow: {
    position: 'absolute',
    top: 5,
    right: 3,
    bottom: 12,
    left: 14,
  },
});

const codeIconStyles = StyleSheet.create({
  icon: {
    width: 22,
    height: 22,
    marginRight: 12,
  },
});

/* —— 样式 —— */

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: BG_CREAM,
  },
  scroll: {
    flexGrow: 1,
  },
  canvas: {
    width: Fig.canvasW,
    height: Fig.canvasH,
    alignSelf: 'center',
    backgroundColor: 'transparent',
  },

  /* 装饰层 */
  bgPaper: {
    position: 'absolute',
    left: Fig.bgPaperLeft,
    top: Fig.bgPaperTop,
    width: Fig.bgPaperW,
    height: Fig.bgPaperH,
    opacity: 0.85,
  },
  mascot: {
    position: 'absolute',
    left: Fig.mascotLeft,
    top: Fig.mascotTop,
    width: Fig.mascotW,
    height: Fig.mascotH,
    transform: [{ scaleX: -1 }],
  },

  /* WELCOME */
  welcome: {
    position: 'absolute',
    left: Fig.welcomeLeft,
    top: Fig.welcomeTop,
    width: Fig.welcomeW,
  },
  welcomeTitle: {
    fontSize: 36,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1.8,
  },
  welcomeSubtitle: {
    fontSize: 24,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    marginTop: 2,
  },

  /* 卡 */
  card: {
    position: 'absolute',
    left: Fig.cardLeft,
    top: Fig.cardTop,
    width: Fig.cardW,
    height: Fig.cardH,
  },

  /* 登录方式切换(卡内顶部居中) */
  modeToggle: {
    position: 'absolute',
    left: 39,
    right: 39,
    top: 20,
    height: 32,
    flexDirection: 'row',
    backgroundColor: 'rgba(220, 204, 161, 0.35)',
    borderRadius: 16,
    padding: 2,
  },
  modeBtn: {
    flex: 1,
    height: 28,
    borderRadius: 14,
    justifyContent: 'center',
    alignItems: 'center',
  },
  modeBtnActive: {
    backgroundColor: LINK_OLIVE,
  },
  modeBtnText: {
    fontSize: 13,
    color: LINK_OLIVE,
    fontFamily: 'Microsoft YaHei',
    fontWeight: '500',
  },
  modeBtnTextActive: {
    color: '#FFFFFF',
    fontWeight: '600',
  },

  /* 输入框 */
  inputBox: {
    position: 'absolute',
    left: Fig.phoneInput.left - Fig.cardLeft,
    top: Fig.phoneInput.top - Fig.cardTop,
    width: Fig.phoneInput.w,
    height: Fig.phoneInput.h,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: INPUT_BORDER,
    borderRadius: 5,
    paddingHorizontal: 8,
    backgroundColor: 'transparent',
  },
  passwordInput: {
    top: Fig.passwordInput.top - Fig.cardTop,
  },
  /** 注册模式下,验证码下方还要一个密码框,占"忘记密码"那行的位置 */
  registerPasswordInput: {
    top: Fig.forgot.top - Fig.cardTop,
  },
  input: {
    flex: 1,
    height: 36,
    fontSize: 16,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    paddingVertical: 0,
    textAlignVertical: 'center',
    includeFontPadding: false,
  },
  divider: {
    width: 1,
    height: 20,
    backgroundColor: '#C0C0C0',
    opacity: 0.6,
    marginHorizontal: 10,
  },
  actionText: {
    fontSize: 12,
    color: '#888888',
    fontFamily: 'Microsoft YaHei',
  },
  actionTextDisabled: {
    color: '#BBBBBB',
    opacity: 0.7,
  },

  /* 忘记密码 */
  forgotWrap: {
    position: 'absolute',
    left: Fig.forgot.left - Fig.cardLeft,
    top: Fig.forgot.top - Fig.cardTop,
  },

  /* 登录按钮 */
  loginBtn: {
    position: 'absolute',
    left: Fig.button.left - Fig.cardLeft,
    top: Fig.button.top - Fig.cardTop,
    width: Fig.button.w,
    height: Fig.button.h,
    justifyContent: 'center',
    alignItems: 'center',
    overflow: 'hidden',
  },
  btnPressed: { opacity: 0.85 },
  loginBtnText: {
    fontSize: 20,
    color: '#FFFFFF',
    fontFamily: 'Microsoft YaHei',
    fontWeight: '700',
    letterSpacing: 12,
  },

  /* 协议行 */
  agreementRow: {
    position: 'absolute',
    left: Fig.agreement.left - Fig.cardLeft,
    top: Fig.agreement.top - Fig.cardTop,
    right: 8,
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  checkboxWrap: {
    paddingRight: 6,
    paddingVertical: 2,
  },
  checkboxImg: {
    marginTop: 2,
  },
  checkboxOn: {
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 1,
    borderColor: TEXT_DARK,
    backgroundColor: TEXT_DARK,
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkmark: {
    color: BG_CREAM,
    fontSize: 9,
    fontWeight: '700',
    lineHeight: 10,
  },
  agreementText: {
    fontSize: 10,
    color: LINK_OLIVE,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
    lineHeight: 14,
    flexShrink: 1,
  },
  linkInline: {
    color: LINK_OLIVE,
    textDecorationLine: 'underline',
  },

  /* 通用链接 */
  linkText: {
    fontSize: 10,
    color: LINK_OLIVE,
    fontFamily: 'Microsoft YaHei',
  },
});