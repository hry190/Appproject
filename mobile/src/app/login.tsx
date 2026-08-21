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
import { SvgXml } from 'react-native-svg';

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

import bgPaper from '@/assets/images/login/bg-paper.png';
import mascot from '@/assets/images/login/mascot.png';

/* —— SVG 字符串常量 —— *
 * 已抽到 components/auth/authSvgs.ts,这里直接用公共版本。
 */

const SVG_CARD_FRAME = buildCardFrameSvg(373);

/**
 * 登录 / 注册页(Figma 节点 413:3271)。
 *
 * v5 改版要点(v4 → v5):
 * - 全部 SVG 改用 react-native-svg(SvgXml 解析复杂 SVG / Svg+Path 内联图标)
 * - 新增登录 / 注册 tab 切换(密码登录 vs 短信注册)
 * - 短信验证码 60s 冷却倒计时
 * - 提交按钮 disabled 控制(canSubmit 派生)
 * - 切 tab 自动清空上一个模式的输入
 *
 * 设计 token(来源 Figma 413:3271 实测,基线 412 × 917 dp):
 *   背景米黄     #F5E8D4
 *   输入框边框   #DCCCA1
 *   协议/链接    #A7AD8E
 *   placeholder  #939393
 */

/* —— 颜色从 components/auth/authColors 引入 —— */

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
  mascotTop: 280,
  mascotW: 111,
  mascotH: 197,
  cardLeft: 20,
  cardTop: 455,
  cardW: 372,
  cardH: 380,
  phoneInput: { left: 59, top: 557, w: 294, h: 40 },
  passwordInput: { left: 59, top: 610, w: 294, h: 40 },
  forgot: { left: 59, top: 656 },
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
  /** 内联错误提示(发送验证码 / 提交时的友好反馈) */
  const [phoneError, setPhoneError] = useState<string | null>(null);
  /** 防止双击 race:同 tick 内多次点只触发一次 */
  const sendingRef = useRef(false);

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

  const handleLogin = () => {
    if (!phone.trim()) {
      console.warn('[login] 请输入手机号');
      return;
    }
    if (!isPhone(phone)) {
      console.warn('[login] 手机号格式不正确');
      return;
    }
    if (!secret.trim()) {
      console.warn(`[login] 请输入${mode === 'code' ? '验证码' : '密码'}`);
      return;
    }
    if (mode === 'code' && !/^\d{6}$/.test(secret)) {
      console.warn('[login] 验证码需为 6 位数字');
      return;
    }
    if (mode === 'password' && secret.trim().length < 6) {
      console.warn('[login] 密码至少 6 位');
      return;
    }
    if (mode === 'code' && registerPassword.trim().length < 6) {
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

  /** 按钮可用性:所有字段合法 + 勾选协议 */
  const phoneValid = isPhone(phone);
  const secretValid = mode === 'code' ? isCode(secret) : secret.trim().length >= 6;
  const registerPwdValid =
    mode !== 'code' || registerPassword.trim().length >= 6;
  const canSubmit = phoneValid && secretValid && registerPwdValid && agreed;

  /** 切 tab 时清空上一个模式的输入,避免旧值残留 */
  const switchMode = (next: LoginMode) => {
    if (next === mode) return;
    setMode(next);
    setSecret('');
    if (next === 'password') setRegisterPassword('');
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
            // @ts-expect-error RN 此版本的 ImageStyle 类型不含 pointerEvents(View 的有),所以保留 prop 写法 + 注释说明
            pointerEvents="none"
            style={styles.bgPaper}
            resizeMode="cover"
          />

          {/* 2. 熊猫吉祥物(右上,水平翻转) */}
          <Image
            source={mascot}
            // @ts-expect-error props.pointerEvents 已弃用,但 RN <Image> 的 RegisteredStyle spread 不接受 pointerEvents 字段类型
            pointerEvents="none"
            style={styles.mascot}
            resizeMode="contain"
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
            <View style={[StyleSheet.absoluteFill, { pointerEvents: 'none' }]}>
              <SvgXml xml={SVG_CARD_FRAME} width="100%" height="100%" />
            </View>

            {/* 登录方式切换(密码 / 验证码)— 卡内顶部居中 */}
            <View style={styles.modeToggle}>
              <Pressable
                accessibilityRole="tab"
                accessibilityState={{ selected: mode === 'password' }}
                onPress={() => switchMode('password')}
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
                onPress={() => switchMode('code')}
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

            {/* 第二个输入框:验证码 或 密码 */}
            <View style={[styles.inputBox, styles.passwordInput]}>
              {mode === 'password' ? (
                <KeyIcon />
              ) : (
                <PeopleSafeIcon />
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
                accessibilityLabel={mode === 'code' ? '验证码' : '密码'}
                accessibilityHint={mode === 'code' ? '6 位数字' : '至少 6 位'}
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
                accessibilityRole="button"
                accessibilityLabel="忘记密码 找回密码"
                accessibilityHint="导航到密码找回页"
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
                  accessibilityLabel="设置登录密码"
                  accessibilityHint="至少 6 位"
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

            {/* 登录 / 注册按钮 */}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={mode === 'code' ? '注册' : '登录'}
              accessibilityState={{ disabled: !canSubmit }}
              disabled={!canSubmit}
              onPress={handleLogin}
              style={({ pressed }) => [
                styles.loginBtn,
                pressed && styles.btnPressed,
                !canSubmit && styles.btnDisabled,
              ]}>
              <View style={[StyleSheet.absoluteFill, { pointerEvents: 'none' }]}>
                <SvgXml xml={SVG_BTN} width="100%" height="100%" />
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
    top: 45,
    height: 48,
    flexDirection: 'row',
    backgroundColor: 'rgba(220, 204, 161, 0.35)',
    borderRadius: 24,
    padding: 2,
  },
  modeBtn: {
    flex: 1,
    height: 44,
    borderRadius: 22,
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
    backgroundColor: DIVIDER_GRAY,
    opacity: 0.6,
    marginHorizontal: 10,
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
  btnDisabled: { opacity: 0.5 },
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
  /* 内联错误提示 */
  errorText: {
    position: 'absolute',
    left: 59 - 20,
    top: Fig.phoneInput.top - Fig.cardTop + Fig.phoneInput.h + 4,
    fontSize: 10,
    color: ERROR_RED,
    fontFamily: 'Microsoft YaHei',
  },
});