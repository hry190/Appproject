import { useEffect, useRef, useState } from 'react';
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

import { SVG_BTN, SVG_CHECKBOX, buildCardFrameSvg } from '@/components/auth/authSvgs';
import { KeyIcon, PeopleSafeIcon, PhoneIcon } from '@/components/auth/AuthIcons';
import {
  ACTION_GRAY,
  BG_CREAM,
  DIVIDER_GRAY,
  ERROR_RED,
  INPUT_BORDER,
  LINK_OLIVE,
  PLACEHOLDER,
  TEXT_DARK,
} from '@/components/auth/authColors';
import { isCode, isPhone } from '@/utils/validators';

import forgotBg from '@/assets/images/login/forgot-bg.png';
import forgetMascot from '@/assets/images/forget/-15.png';

/* —— SVG 字符串常量(复杂 SVG 用 SvgXml 让库解析) —— */

const SVG_CARD_FRAME = buildCardFrameSvg(340);

const SVG_BACK_ARROW = `<svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M14 5L8 11L14 17" stroke="#000000" stroke-width="1.83" stroke-linecap="round" stroke-linejoin="round"/></svg>`;

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

/* —— 颜色从 components/auth/authColors 引入 —— */

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
  /** 内联错误提示(发送验证码时反馈) */
  const [phoneError, setPhoneError] = useState<string | null>(null);
  /** 防止双击 race */
  const sendingRef = useRef(false);

  useEffect(() => {
    if (countdown <= 0) return;
    const id = setInterval(() => {
      setCountdown((c) => Math.max(0, c - 1));
    }, 1000);
    return () => clearInterval(id);
  }, [countdown]);

  const handleSendCode = () => {
    if (countdown > 0 || sendingRef.current) return;
    if (!isPhone(phone)) {
      setPhoneError('请输入正确的 11 位手机号');
      return;
    }
    setPhoneError(null);
    // TODO: 接入短信发送 API
    sendingRef.current = true;
    setCountdown(60);
    // API 接入后:try { await sendSms(phone) } finally { sendingRef.current = false }
    sendingRef.current = false;
  };

  const handleSubmit = () => {
    if (!phone.trim()) {
      console.warn('[forgot] 请输入手机号');
      return;
    }
    if (!isPhone(phone)) {
      console.warn('[forgot] 手机号格式不正确');
      return;
    }
    if (code.length !== 6 || !isCode(code)) {
      console.warn('[forgot] 验证码需为 6 位数字');
      return;
    }
    if (newPwd.trim().length < 6) {
      console.warn('[forgot] 新密码至少 6 位');
      return;
    }
    if (confirmPwd.trim().length < 6) {
      console.warn('[forgot] 请确认至少 6 位密码');
      return;
    }
    if (newPwd.trim() !== confirmPwd.trim()) {
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
  const phoneValid = isPhone(phone);
  const codeValid = isCode(code);
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
                accessibilityLabel="手机号"
                accessibilityHint="11 位中国大陆手机号"
              />
            </View>
            {phoneError && (
              <Text
                style={styles.errorText}
                accessibilityLiveRegion="polite">
                {phoneError}
              </Text>
            )}

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
                accessibilityLabel="验证码"
                accessibilityHint="6 位数字"
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
                accessibilityLabel="新密码"
                accessibilityHint="至少 6 位"
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
                accessibilityLabel="确认密码"
                accessibilityHint="再输入一次新密码"
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
                  onPress={() => router.push('/agreement')}
                  accessibilityRole="link">
                  《用户协议》
                </Text>
                和
                <Text
                  style={styles.linkInline}
                  onPress={() => router.push('/privacy')}
                  accessibilityRole="link">
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
                <SvgXml xml={SVG_BTN} width="100%" height="100%" />
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
    backgroundColor: DIVIDER_GRAY,
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
  /* 内联错误提示 */
  errorText: {
    position: 'absolute',
    left: Fig.phoneInput.left - Fig.cardLeft,
    top: Fig.phoneInput.top - Fig.cardTop + Fig.phoneInput.h + 4,
    fontSize: 10,
    color: ERROR_RED,
    fontFamily: 'Microsoft YaHei',
  },
});