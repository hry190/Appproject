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

import forgotBg from '@/assets/images/login/forgot-bg.png';
import forgetMascot from '@/assets/images/forget/-15.png';
import iconPhoneBody from '@/assets/images/login/icon-phone-body.svg';
import iconPhoneHome from '@/assets/images/login/icon-phone-home.svg';
import iconPhoneSpeaker from '@/assets/images/login/icon-phone-speaker.svg';
import iconKeyBody from '@/assets/images/login/icon-key-body.svg';
import iconKeyBow from '@/assets/images/login/icon-key-bow.svg';
import iconKeyRing from '@/assets/images/login/icon-key-ring.svg';
import peopleSafeBody from '@/assets/images/login/icon-people-safe-body.svg';
import peopleSafeTop from '@/assets/images/login/icon-people-safe-top.svg';
import peopleSafeMid from '@/assets/images/login/icon-people-safe-mid.svg';

/* —— SVG 字符串常量(复杂 SVG 用 SvgXml 让库解析) —— */

const SVG_CARD_FRAME = `<svg preserveAspectRatio="none" width="408" height="340" viewBox="0 0 408 340" fill="none" xmlns="http://www.w3.org/2000/svg"><g><path d="M17.56 35.94C18.76 19.86 26.57 17.72 30.9 17.72C34.89 5.44 43.68 2.26 47.01 3.14H361.45C371.03 3.14 375.09 13.01 375.92 17.95C384.71 16.68 388.24 26.94 388.9 32.23V308.37C388.1 318.1 379.92 323.71 375.92 325.3C370.73 335.88 364.11 338.17 361.45 337.99H47.01C36.63 335.03 33.03 327.94 32.53 324.77C20.56 321.38 17.56 311.37 17.56 306.78C17.06 223.2 16.36 52.02 17.56 35.94Z" fill="#F2E6D1"/><path d="M17.56 35.94C18.76 19.86 26.57 17.72 30.9 17.72C34.89 5.44 43.68 2.26 47.01 3.14H361.45C371.03 3.14 375.09 13.01 375.92 17.95C384.71 16.68 388.24 26.94 388.9 32.23V308.37C388.1 318.1 379.92 323.71 375.92 325.3C370.73 335.88 364.11 338.17 361.45 337.99H47.01C36.63 335.03 33.03 327.94 32.53 324.77C20.56 321.38 17.56 311.37 17.56 306.78C17.06 223.2 16.36 52.02 17.56 35.94Z" stroke="#E7DCAE" stroke-width="6"/></g><g><path d="M36.49 50.32C37.57 36.01 45.48 34.02 49.36 34.02C51.85 22.52 59.95 20.36 62.93 21.15H345.25C353.86 21.15 357.5 29.93 358.25 34.32C366.14 33.19 369.3 42.32 369.9 47.02V292.64C369.18 301.3 365.4 306.45 359.9 306.45C358.05 317.74 347.64 319.15 345.25 318.99H62.93C53.9 318.99 50.9 318.99 47.86 305.4C37.11 302.39 36.49 295.31 36.49 291.23C36.05 216.89 35.42 64.62 36.49 50.32Z" stroke="#E7DCAE" stroke-width="2"/></g></svg>`;

const SVG_BTN_BG = `<svg preserveAspectRatio="none" width="310" height="54" viewBox="0 0 310 54" fill="none" xmlns="http://www.w3.org/2000/svg"><g><path d="M22.67 0.53C16.21 0.53 14.16 6.47 14.16 8.99C11.51 8.99 4 10.38 4 22.64C4 32.62 10.51 37.29 14.16 37.29C14.16 42.85 19.53 45.01 21.67 45C108.15 44.66 282.02 45 286.11 45C290.98 45 295.63 41.33 295.63 37.93C301.99 37.93 306 32.62 306 21.12C306 11.9 300.49 8.99 295.63 8.99C295.63 3.44 287.97 0.03 284.11 0.03C199.63 -0.14 29.54 0.53 22.67 0.53Z" fill="url(#g0)"/><path d="M27.48 4.44C21.25 4.44 19.27 9.32 19.27 11.4C16.72 11.4 9.48 12.54 9.48 22.61C9.48 30.82 15.76 34.66 19.27 34.66C19.27 39.23 24.44 41.01 26.51 41C109.85 40.72 277.41 41 281.35 41C286.04 41 290.52 37.99 290.52 35.18C296.66 35.18 300.52 30.82 300.52 21.37C300.52 13.78 295.21 11.4 290.52 11.4C290.52 6.83 283.14 4.03 279.42 4.02C198.01 3.88 34.1 4.44 27.48 4.44Z" fill="url(#g1)" stroke="url(#g2)"/></g><defs><linearGradient id="g0" x1="155" y1="0" x2="155" y2="33" gradientUnits="userSpaceOnUse"><stop stop-color="#AACC99"/><stop offset="1" stop-color="#546942"/></linearGradient><linearGradient id="g1" x1="155" y1="41" x2="155" y2="4" gradientUnits="userSpaceOnUse"><stop stop-color="#527F50"/><stop offset="1" stop-color="#92B57A"/></linearGradient><linearGradient id="g2" x1="9.48" y1="22.5" x2="300.52" y2="22.5" gradientUnits="userSpaceOnUse"><stop stop-color="#DCCCA1"/><stop offset="1" stop-color="#FAF4D8"/></linearGradient></defs></svg>`;

const SVG_CHECKBOX = `<svg width="9" height="9" viewBox="0 0 9 9" fill="none" xmlns="http://www.w3.org/2000/svg"><circle cx="4.5" cy="4.5" r="4" stroke="#A7AD8E"/></svg>`;

const SVG_BACK_ARROW = `<svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14 5L8 11L14 17" stroke="#000000" stroke-width="1.83" stroke-linecap="round" stroke-linejoin="round"/></svg>`;

const ICON_STROKE = '#949494';
const ICON_STROKE_WIDTH = 1.83;

/**
 * 忘记密码页(Figma 节点 413:3377)。
 *
 * 布局:
 *   顶部 (y=110):「忘记密码」标题 + 返回箭头 + 右上小熊猫
 *   卡片 (y=349-819):
 *     - 手机号 (y=411)
 *     - 验证码 (y=471) + 获取验证码
 *     - 新密码 (y=536)
 *     - 确定密码 (y=596)
 *     - 协议行 (y=700)
 *     - 确定修改按钮 (y=716)
 *
 * 校验:手机号正则、验证码 6 位、新密码 ≥ 6 位、两次一致、勾选协议
 */

const BG_CREAM = '#F5E8D4';
const INPUT_BORDER = '#DCCCA1';
const LINK_OLIVE = '#A7AD8E';
const PLACEHOLDER = '#939393';
const TEXT_DARK = '#000000';
const ACTION_GRAY = '#888888';

const Fig = {
  canvasW: 412,
  canvasH: 800,

  bgLeft: -122,
  bgTop: 0,
  bgW: 536,
  bgH: 817,

  backLeft: 20,
  backTop: 120,
  backSize: 22,

  titleLeft: 47,
  titleTop: 110,

  mascotLeft: 296,
  mascotTop: 140,
  mascotW: 87,
  mascotH: 235,

  cardLeft: 20,
  cardTop: 349,
  cardW: 372,
  cardH: 400,

  phoneInput: { left: 59, top: 400, w: 294, h: 50 },
  codeInput: { left: 59, top: 460, w: 294, h: 50 },
  newPwdInput: { left: 59, top: 520, w: 294, h: 50 },
  confirmInput: { left: 59, top: 580, w: 294, h: 50 },

  agreement: { left: 90, top: 640 },
  button: { left: 55, top: 666, w: 302, h: 55 },
} as const;

export default function ForgotScreen() {
  const router = useRouter();
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [agreed, setAgreed] = useState(false);
  const [countdown, setCountdown] = useState(0);

  useEffect(() => {
    if (countdown <= 0) return;
    const id = setInterval(() => {
      setCountdown((c) => Math.max(0, c - 1));
    }, 1000);
    return () => clearInterval(id);
  }, [countdown]);

  const handleSendCode = () => {
    if (countdown > 0) return;
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      console.warn('[forgot] 发送验证码前请先输入正确手机号');
      return;
    }
    // TODO: 接入短信发送 API
    setCountdown(60);
  };

  const handleSubmit = () => {
    if (!phone.trim()) {
      console.warn('[forgot] 请输入手机号');
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(phone.trim())) {
      console.warn('[forgot] 手机号格式不正确');
      return;
    }
    if (code.length !== 6 || !/^\d{6}$/.test(code)) {
      console.warn('[forgot] 验证码需为 6 位数字');
      return;
    }
    if (newPwd.length < 6) {
      console.warn('[forgot] 新密码至少 6 位');
      return;
    }
    if (confirmPwd.length < 6) {
      console.warn('[forgot] 请确认至少 6 位密码');
      return;
    }
    if (newPwd !== confirmPwd) {
      console.warn('[forgot] 两次密码不一致');
      return;
    }
    if (!agreed) {
      console.warn('[forgot] 未勾选用户协议');
      return;
    }
    // TODO: 接入找回密码 API
    router.replace('/login');
  };

  /** 按钮可用性:所有字段合法 + 勾选协议 */
  const phoneValid = /^1[3-9]\d{9}$/.test(phone.trim());
  const codeValid = /^\d{6}$/.test(code);
  const newPwdValid = newPwd.length >= 6;
  const confirmValid = confirmPwd.length >= 6 && newPwd === confirmPwd;
  const canSubmit =
    phoneValid && codeValid && newPwdValid && confirmValid && agreed;

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <ScrollView
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>
        {/* 412×800 画布 */}
        <View style={styles.canvas}>
          {/* 1. 背景旧纸 */}
          <Image
            source={forgotBg}
            // @ts-expect-error props.pointerEvents 已弃用,但 RN <Image> 的 RegisteredStyle spread 不接受 pointerEvents 字段类型
            pointerEvents="none"
            style={styles.bg}
            resizeMode="cover"
          />

          {/* 2. 顶部标题区:返回箭头 + 标题 */}
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="返回登录"
            onPress={() =>
              router.canGoBack() ? router.back() : router.replace('/login')
            }
            hitSlop={12}
            style={styles.backBtn}>
            <SvgXml xml={SVG_BACK_ARROW} width={Fig.backSize} height={Fig.backSize} />
          </Pressable>

          <Text
            style={styles.title}
            maxFontSizeMultiplier={1.4}
            allowFontScaling>
            忘记密码
          </Text>

          {/* 3. 熊猫(右上) */}
          <Image
            source={forgetMascot}
            // @ts-expect-error props.pointerEvents 已弃用,但 RN <Image> 的 RegisteredStyle spread 不接受 pointerEvents 字段类型
            pointerEvents="none"
            style={styles.mascot}
            resizeMode="contain"
          />

          {/* 4. 卡片 */}
          <View style={styles.card}>
            <View style={[StyleSheet.absoluteFill, { pointerEvents: 'none' }]}>
              <SvgXml xml={SVG_CARD_FRAME} width="100%" height="100%" />
            </View>

            {/* 手机号 */}
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

            {/* 验证码 + 获取验证码 */}
            <View style={[styles.inputBox, styles.codeInput]}>
              <PeopleSafeIcon />
              <TextInput
                style={styles.input}
                placeholder="请输入验证码"
                placeholderTextColor={PLACEHOLDER}
                keyboardType="number-pad"
                maxLength={6}
                value={code}
                onChangeText={setCode}
                maxFontSizeMultiplier={1.4}
                allowFontScaling
              />
              <View style={styles.divider} />
              <Pressable
                hitSlop={8}
                disabled={countdown > 0}
                accessibilityRole="button"
                accessibilityState={{ disabled: countdown > 0 }}
                accessibilityLabel={
                  countdown > 0 ? `${countdown} 秒后可重新发送` : '发送验证码'
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
            </View>

            {/* 新密码 */}
            <View style={[styles.inputBox, styles.newPwdInput]}>
              <KeyIcon />
              <TextInput
                style={styles.input}
                placeholder="请输入新的密码"
                placeholderTextColor={PLACEHOLDER}
                secureTextEntry
                maxLength={24}
                value={newPwd}
                onChangeText={setNewPwd}
                maxFontSizeMultiplier={1.4}
                allowFontScaling
              />
            </View>

            {/* 确定密码 */}
            <View style={[styles.inputBox, styles.confirmInput]}>
              <KeyIcon />
              <TextInput
                style={styles.input}
                placeholder="请确认密码"
                placeholderTextColor={PLACEHOLDER}
                secureTextEntry
                maxLength={24}
                value={confirmPwd}
                onChangeText={setConfirmPwd}
                maxFontSizeMultiplier={1.4}
                allowFontScaling
              />
            </View>

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

            {/* 确定修改按钮 */}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel="确定修改"
              accessibilityState={{ disabled: !canSubmit }}
              disabled={!canSubmit}
              onPress={handleSubmit}
              style={({ pressed }) => [
                styles.submitBtn,
                pressed && styles.btnPressed,
                !canSubmit && styles.btnDisabled,
              ]}>
              <View style={[StyleSheet.absoluteFill, { pointerEvents: 'none' }]}>
                <SvgXml xml={SVG_BTN_BG} width="100%" height="100%" />
              </View>
              <Text
                style={styles.submitBtnText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                确定修改
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
      <Svg width="15.375" height="22.67" viewBox="0 0 15.375 22.67" style={phoneStyles.body}>
        <Path
          d="M12.9 0.92H2.48C1.62 0.92 0.92 1.62 0.92 2.48V20.19C0.92 21.05 1.62 21.75 2.48 21.75H12.9C13.76 21.75 14.46 21.05 14.46 20.19V2.48C14.46 1.62 13.76 0.92 12.9 0.92Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          fill="none"
        />
      </Svg>
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
  frame: { width: 25, height: 25, marginRight: 14 },
  body: { position: 'absolute', top: 1, left: 4 },
  speaker: { position: 'absolute', top: 5, left: 10 },
  home: { position: 'absolute', bottom: 5, left: 9 },
});

function KeyIcon() {
  return (
    <View style={keyStyles.frame}>
      <Svg width="10.92" height="10.87" viewBox="0 0 10.92 10.87" style={keyStyles.body}>
        <Path
          d="M8.65 2.19C9.81 3.33 10.27 5.01 9.85 6.58C9.43 8.16 8.2 9.39 6.61 9.8C5.03 10.22 3.35 9.77 2.2 8.61C0.47 6.83 0.49 3.99 2.25 2.25C4.01 0.49 6.86 0.47 8.65 2.19Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
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
  frame: { width: 22, height: 22, marginRight: 12 },
  body: { position: 'absolute', top: 11, right: 10, bottom: 3, left: 3 },
  ring: { position: 'absolute', top: 3, left: 11, right: 4, bottom: 9 },
  bow: { position: 'absolute', top: 5, right: 3, bottom: 12, left: 14 },
});

function PeopleSafeIcon() {
  return (
    <View style={peopleSafeStyles.frame}>
      <Svg width="17" height="17" viewBox="0 0 17 17" style={peopleSafeStyles.body}>
        <Path
          d="M1.5 1.5H15.5V15.5H1.5Z"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          fill="none"
        />
      </Svg>
      <Svg width="6" height="2.5" viewBox="0 0 6 2.5" style={peopleSafeStyles.top}>
        <Path
          d="M0.5 0.5H5.5"
          stroke={ICON_STROKE}
          strokeWidth={ICON_STROKE_WIDTH}
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
      </Svg>
      <Svg width="6" height="3" viewBox="0 0 6 3" style={peopleSafeStyles.mid}>
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
  // Figma inset-[8.33%_12.5%] (top 8.33% ≈ 1.83, left/right 12.5% ≈ 2.75)
  body: {
    position: 'absolute',
    top: 2,
    left: 3,
    right: 3,
    bottom: 2,
  },
  // Figma inset-[27.08%_39.58%_52.08%_39.58%] - top stripe
  top: {
    position: 'absolute',
    top: 6,
    left: 9,
    right: 9,
    height: 2.5,
  },
  // Figma inset-[47.92%_33.33%_35.42%_33.33%] - mid stripe
  mid: {
    position: 'absolute',
    top: 10.5,
    left: 7,
    right: 7,
    height: 3,
  },
});

/* —— 样式 —— */

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: BG_CREAM },
  scroll: { flexGrow: 1 },
  canvas: {
    width: Fig.canvasW,
    height: Fig.canvasH,
    alignSelf: 'center',
    backgroundColor: 'transparent',
  },

  /* 装饰 */
  bg: {
    position: 'absolute',
    left: Fig.bgLeft,
    top: Fig.bgTop,
    width: Fig.bgW,
    height: Fig.bgH,
    opacity: 0.85,
  },
  mascot: {
    position: 'absolute',
    left: Fig.mascotLeft,
    top: Fig.mascotTop,
    width: Fig.mascotW,
    height: Fig.mascotH,
  },

  /* 顶部:返回箭头 + 标题 */
  backBtn: {
    position: 'absolute',
    left: Fig.backLeft,
    top: Fig.backTop,
    width: Fig.backSize,
    height: Fig.backSize,
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    position: 'absolute',
    left: Fig.titleLeft,
    top: Fig.titleTop,
    fontSize: 32,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },

  /* 卡 */
  card: {
    position: 'absolute',
    left: Fig.cardLeft,
    top: Fig.cardTop,
    width: Fig.cardW,
    height: Fig.cardH,
  },

  /* 输入框(通用,默认 = phone 位置) */
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
  codeInput: {
    top: Fig.codeInput.top - Fig.cardTop,
  },
  newPwdInput: {
    top: Fig.newPwdInput.top - Fig.cardTop,
  },
  confirmInput: {
    top: Fig.confirmInput.top - Fig.cardTop,
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
    height: 28,
    backgroundColor: '#C0C0C0',
    opacity: 0.6,
    marginHorizontal: 12,
  },
  actionText: {
    fontSize: 12,
    color: ACTION_GRAY,
    fontFamily: 'Microsoft YaHei',
  },
  actionTextDisabled: {
    color: '#BBBBBB',
    opacity: 0.7,
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
  checkboxImg: { marginTop: 2 },
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

  /* 提交按钮 */
  submitBtn: {
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
  btnDisabled: { opacity: 0.5 },
  submitBtnText: {
    fontSize: 20,
    color: '#FFFFFF',
    fontFamily: 'Microsoft YaHei',
    fontWeight: '700',
    letterSpacing: 12,
  },
});