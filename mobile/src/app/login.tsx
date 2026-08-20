import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { Layout } from '@/constants/theme';

/**
 * 登录页 —— 验证码登录 / 密码登录 双 Tab 切换。
 *
 * 布局策略（v2，已按 Figma 节点 185:1476 实测调整）：
 * 1. flex 替代 absolute——WELCOME / Tab / Card 按内容流排版
 * 2. SafeAreaView + ScrollView 兜底——覆盖 360×640 模拟器 + 412×917 小米 12
 * 3. Layout token 化——padding/gap/radius 全走常量，禁散落 px
 * 4. maxFontSizeMultiplier 分级——防 iOS 系统字号放大破版
 *
 * 设计 token（来源 Figma 节点 185:1476 实测）：
 *   基线      402 × 866 dp  （iPhone 16 Pro 风格）
 *   目标真机   360×640 (模拟器) / 412×917 (小米 12)
 */

const OLIVE = '#9DA27F';
const CREAM = '#F4E7D1';
const CREAM_BTN = '#F7ECDA';
const WHITE = '#FFFFFF';
const TEXT_DARK = '#000000';
const PLACEHOLDER = '#898989';
const DIVIDER_GRAY = '#C0C0C0';
const FACE_BG = '#F5DEC1';
const FACE_BORDER = '#CEB187';
const ACTION_GRAY = '#888888';

type LoginMode = 'code' | 'password';

export default function LoginScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const [mode, setMode] = useState<LoginMode>('code');
  const [phone, setPhone] = useState('');
  const [secret, setSecret] = useState('');

  const handleLogin = () => {
    // TODO: 接入登录 API
    router.replace('/(tabs)');
  };

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}>

        {/* 1. WELCOME 标题 */}
        <View style={styles.welcomeWrap}>
          <Text
            style={styles.welcome}
            maxFontSizeMultiplier={1.25}
            allowFontScaling>
            WELCOME!
          </Text>
        </View>

        {/* 2. Tab 切换条 */}
        <View style={styles.tabBar}>
          <Pressable
            accessibilityRole="tab"
            accessibilityState={{ selected: mode === 'code' }}
            onPress={() => setMode('code')}
            style={[styles.tabHalf, styles.tabActive]}>
            <View style={styles.faceIcon}>
              <View style={[styles.eye, styles.eyeLeft]} />
              <View style={[styles.eye, styles.eyeRight]} />
              <View style={styles.faceMouth} />
            </View>
            <Text
              style={[styles.tabText, styles.tabTextActive]}
              maxFontSizeMultiplier={1.4}
              allowFontScaling>
              验证码登录
            </Text>
          </Pressable>

          <Pressable
            accessibilityRole="tab"
            accessibilityState={{ selected: mode === 'password' }}
            onPress={() => setMode('password')}
            style={[styles.tabHalf, styles.tabInactive]}>
            <Text
              style={[styles.tabText, styles.tabTextInactive]}
              maxFontSizeMultiplier={1.4}
              allowFontScaling>
              密码登录
            </Text>
          </Pressable>
        </View>

        {/* 3. 主区卡：flex:1 拿剩余高度 */}
        <View style={styles.card}>
          {/* 字段 1：手机号 */}
          <View style={styles.field}>
            <Text style={styles.label} allowFontScaling>手机号</Text>
            <View style={styles.inputBox}>
              <IphoneGlyph />
              <TextInput
                style={styles.input}
                placeholder="请输入手机号"
                placeholderTextColor={PLACEHOLDER}
                keyboardType="phone-pad"
                maxLength={11}
                value={phone}
                onChangeText={setPhone}
                allowFontScaling
              />
            </View>
          </View>

          {/* 字段 2：验证码 / 密码 */}
          <View style={[styles.field, styles.fieldGap]}>
            <Text style={styles.label} allowFontScaling>
              {mode === 'code' ? '验证码' : '密码'}
            </Text>
            <View style={styles.inputBox}>
              <ShieldGlyph />
              <TextInput
                style={styles.input}
                placeholder={mode === 'code' ? '请输入验证码' : '请输入密码'}
                placeholderTextColor={PLACEHOLDER}
                keyboardType={mode === 'code' ? 'number-pad' : 'default'}
                secureTextEntry={mode === 'password'}
                maxLength={mode === 'code' ? 6 : 24}
                value={secret}
                onChangeText={setSecret}
                allowFontScaling
              />
              {mode === 'code' && (
                <>
                  <View style={styles.divider} />
                  <Pressable hitSlop={8} onPress={() => { /* TODO: 触发验证码 */ }}>
                    <Text
                      style={styles.actionText}
                      maxFontSizeMultiplier={1.6}
                      allowFontScaling>
                      获取验证码
                    </Text>
                  </Pressable>
                </>
              )}
            </View>
          </View>

          {/* Spacer: 把下面推到卡的下半 */}
          <View style={styles.spacer} />

          {/* 登录按钮 */}
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="登录"
            onPress={handleLogin}
            style={({ pressed }) => [styles.loginBtn, pressed && styles.btnPressed]}>
            <Text
              style={styles.loginBtnText}
              maxFontSizeMultiplier={1.4}
              allowFontScaling>
              登录
            </Text>
          </Pressable>

          {/* 注册链接 */}
          <Pressable
            accessibilityRole="link"
            onPress={() => router.push('/register')}
            style={styles.registerLinkWrap}
            hitSlop={8}>
            <Text
              style={styles.registerLink}
              maxFontSizeMultiplier={1.6}
              allowFontScaling>
              没有账号？立即注册
            </Text>
          </Pressable>

          {/* 底部安全区缓冲（避手势条 / 三键导航） */}
          <View style={{ height: insets.bottom }} />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

/* —— 内置图标（View 拼装，无外部依赖）—— */

function IphoneGlyph() {
  return (
    <View style={glyphStyles.wrap}>
      <View style={glyphStyles.iphoneBody} />
      <View style={glyphStyles.iphoneSpeaker} />
      <View style={glyphStyles.iphoneHome} />
    </View>
  );
}

function ShieldGlyph() {
  return (
    <View style={glyphStyles.wrap}>
      <View style={glyphStyles.shieldBody} />
      <View style={glyphStyles.shieldCheckA} />
      <View style={glyphStyles.shieldCheckB} />
    </View>
  );
}

const glyphStyles = StyleSheet.create({
  wrap: {
    width: 22,
    height: 22,
    marginRight: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  iphoneBody: {
    width: 14,
    height: 20,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
    borderRadius: 2,
  },
  iphoneSpeaker: {
    position: 'absolute',
    top: 3,
    width: 5,
    height: 1.5,
    backgroundColor: TEXT_DARK,
    borderRadius: 1,
  },
  iphoneHome: {
    position: 'absolute',
    bottom: 3,
    width: 4,
    height: 1.5,
    backgroundColor: TEXT_DARK,
    borderRadius: 1,
  },
  shieldBody: {
    width: 18,
    height: 18,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
    borderTopLeftRadius: 9,
    borderTopRightRadius: 9,
    transform: [{ rotate: '180deg' }],
  },
  shieldCheckA: {
    position: 'absolute',
    width: 4,
    height: 1.5,
    backgroundColor: TEXT_DARK,
    bottom: 7,
    left: 4,
    transform: [{ rotate: '45deg' }],
  },
  shieldCheckB: {
    position: 'absolute',
    width: 8,
    height: 1.5,
    backgroundColor: TEXT_DARK,
    bottom: 6,
    left: 6,
    transform: [{ rotate: '-45deg' }],
  },
});

/* —— 主样式 —— */

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: CREAM,
  },
  scrollContent: {
    flexGrow: 1,
  },

  /* WELCOME */
  welcomeWrap: {
    paddingHorizontal: Layout.contentInset,
    paddingTop: 28,
  },
  welcome: {
    fontSize: 40,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
  },

  /* Tab Bar */
  tabBar: {
    marginTop: Layout.sectionGap * 2,
    marginHorizontal: Layout.px,
    height: 64,
    flexDirection: 'row',
    overflow: 'hidden',
  },
  tabHalf: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 12,
  },
  tabActive: { backgroundColor: OLIVE },
  tabInactive: { backgroundColor: WHITE },
  tabText: {
    fontSize: 24,
    fontFamily: 'Microsoft YaHei',
  },
  tabTextActive: { color: WHITE, fontWeight: '500' },
  tabTextInactive: { color: TEXT_DARK, fontWeight: '400' },

  /* Face Icon */
  faceIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: FACE_BG,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    overflow: 'hidden',
    borderWidth: 0.5,
    borderColor: FACE_BORDER,
  },
  eye: {
    position: 'absolute',
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: TEXT_DARK,
    top: 12,
  },
  eyeLeft: { left: 9 },
  eyeRight: { left: 21 },
  faceMouth: {
    position: 'absolute',
    width: 14,
    height: 7,
    borderBottomLeftRadius: 7,
    borderBottomRightRadius: 7,
    borderColor: TEXT_DARK,
    borderWidth: 1.5,
    borderTopWidth: 0,
    top: 18,
    left: 11,
  },

  /* Card */
  card: {
    marginTop: 2,
    marginHorizontal: Layout.px,
    flex: 1,
    borderTopLeftRadius: Layout.radiusCard,
    borderTopRightRadius: Layout.radiusCard,
    backgroundColor: OLIVE,
    paddingHorizontal: Layout.contentInset,
    paddingTop: 60,
  },

  /* Field */
  field: {},
  fieldGap: { marginTop: Layout.contentGap },
  label: {
    fontSize: 16,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    marginBottom: 8,
    lineHeight: 21,
  },
  inputBox: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: Layout.inputH,
    borderRadius: Layout.radiusField,
    backgroundColor: WHITE,
    paddingHorizontal: 14,
  },
  input: {
    flex: 1,
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1.2,
    paddingVertical: 0,
  },
  divider: {
    width: 1,
    height: 28,
    backgroundColor: DIVIDER_GRAY,
    opacity: 0.57,
    marginHorizontal: 12,
  },
  actionText: {
    fontSize: 12,
    color: ACTION_GRAY,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1.2,
  },

  /* Spacer */
  spacer: {
    flex: 1,
  },

  /* Login Button */
  loginBtn: {
    marginTop: 24,
    paddingVertical: 16,
    borderRadius: Layout.radiusButton,
    backgroundColor: CREAM_BTN,
    justifyContent: 'center',
    alignItems: 'center',
  },
  btnPressed: { opacity: 0.85 },
  loginBtnText: {
    fontSize: 24,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },

  /* Register Link */
  registerLinkWrap: {
    marginTop: 16,
    alignSelf: 'center',
  },
  registerLink: {
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },
});
