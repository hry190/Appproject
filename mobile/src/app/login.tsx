import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Image, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { Layout } from '@/constants/theme';
import ipNiang from '@/assets/images/login/ip-niang.png';
import pandaFace from '@/assets/images/login/panda-face.png';
import decor1 from '@/assets/images/login/decor-1.png';
import decor2 from '@/assets/images/login/decor-2.png';
import decor3 from '@/assets/images/login/decor-3.png';

/**
 * 登录页 —— 验证码 / 密码 双 Tab 切换。
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
  const [agreed, setAgreed] = useState(false);

  const handleLogin = () => {
    if (!agreed) {
      // TODO: 弹 toast 提示勾选协议
      console.warn('[login] 未勾选用户协议');
      return;
    }
    // TODO: 接入登录 API
    router.replace('/(tabs)');
  };

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <View style={styles.canvas}>
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

        {/* 2. Rectangle 172 白条（含 Tab 切换条） */}
        <View style={styles.tabStrip}>
          <View style={styles.tabBar}>
            <Pressable
              accessibilityRole="tab"
              accessibilityState={{ selected: mode === 'code' }}
              onPress={() => setMode('code')}
              style={[styles.tabHalf, mode === 'code' ? styles.tabActive : styles.tabInactive]}>
              <Image
                source={pandaFace}
                style={styles.faceIcon}
                resizeMode="contain"
              />
              <Text
                style={[styles.tabText, mode === 'code' ? styles.tabTextActive : styles.tabTextInactive]}
                maxFontSizeMultiplier={1.4}
                numberOfLines={1}
                allowFontScaling>
                验证码登录
              </Text>
            </Pressable>

            <Pressable
              accessibilityRole="tab"
              accessibilityState={{ selected: mode === 'password' }}
              onPress={() => setMode('password')}
              style={[styles.tabHalf, mode === 'password' ? styles.tabActive : styles.tabInactive]}>
              <Text
                style={[styles.tabText, mode === 'password' ? styles.tabTextActive : styles.tabTextInactive]}
                maxFontSizeMultiplier={1.4}
                numberOfLines={1}
                allowFontScaling>
                密码登录
              </Text>
            </Pressable>
          </View>
        </View>

        {/* 3. 主区卡：flex:1 拿剩余高度 */}
        <View style={styles.card}>
          {/* 装饰图：橄榄绿底上的竹枝（覆盖在卡背景） */}
          <Image
            source={decor2}
            style={styles.cardDecor}
            resizeMode="cover"
            pointerEvents="none"
          />

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
                maxFontSizeMultiplier={1.4}
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
                maxFontSizeMultiplier={1.4}
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

          {/* 协议行（圆点复选框 + 文本；点击行内任意非链接区切换状态） */}
          <View style={styles.agreementRow}>
            <Pressable
              accessibilityRole="checkbox"
              accessibilityState={{ checked: agreed }}
              onPress={() => setAgreed((v) => !v)}
              style={styles.checkboxWrap}
              hitSlop={8}>
              <View style={[styles.checkbox, agreed && styles.checkboxOn]}>
                {agreed ? <Text style={styles.checkmark}>✓</Text> : null}
              </View>
            </Pressable>

            <Pressable
              onPress={() => setAgreed((v) => !v)}
              style={styles.agreementTextWrap}
              hitSlop={4}>
              <Text
                style={styles.agreementText}
                maxFontSizeMultiplier={1.6}
                allowFontScaling>
                我已阅读并同意
              </Text>
            </Pressable>

            <Text
              style={styles.agreementLink}
              maxFontSizeMultiplier={1.6}
              allowFontScaling
              onPress={() => { /* TODO: 跳《用户协议》 */ }}>
              《用户协议》
            </Text>

            <Text
              style={styles.agreementText}
              maxFontSizeMultiplier={1.6}
              allowFontScaling>
              和
            </Text>

            <Text
              style={styles.agreementLink}
              maxFontSizeMultiplier={1.6}
              allowFontScaling
              onPress={() => { /* TODO: 跳《隐私条款》 */ }}>
              《隐私条款》
            </Text>
          </View>

          {/* 底部安全区缓冲（避手势条 / 三键导航） */}
          <View style={{ height: insets.bottom }} />
        </View>
      </ScrollView>

      {/* IP娘角色立绘（右侧装饰，绝对定位覆盖内容） */}
      <Image
        source={ipNiang}
        style={[styles.ipNiang, { pointerEvents: 'none' }]}
        resizeMode="contain"
      />

      {/* 装饰图 decor-3：右上角灰色竹叶 */}
      <Image
        source={decor3}
        style={[styles.decor3, { pointerEvents: 'none' }]}
        resizeMode="contain"
      />

      {/* 装饰图 decor-1：底部水墨氛围（半透）） */}
      <Image
        source={decor1}
        style={[styles.decor1, { pointerEvents: 'none' }]}
        resizeMode="contain"
      />
      </View>
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
  canvas: {
    flex: 1,
    position: 'relative',
  },
  scrollContent: {
    flexGrow: 1,
  },
  ipNiang: {
    position: 'absolute',
    top: 60,
    right: 0,
    width: 70,
    height: 100,
  },
  decor3: {
    position: 'absolute',
    top: 0,
    left:-35,
    width: 220,
    height: 220,
    opacity: 0.9,
  },
  decor1: {
    position: 'absolute',
    bottom: 0,
    left: -50,
    width: 300,
    height: 190,
    opacity: 0.5,
  },
  cardDecor: {
    position: 'absolute',
    top: 0,
    left: -300,
    right: 0,
    bottom: 0,
    opacity: 0.85,
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

  /* Rectangle 172 白条 */
  tabStrip: {
    marginTop: 80,            // Figma 148dp 间距缩到 80，平衡可视图比例
    marginHorizontal: Layout.px,
    backgroundColor: WHITE,
    paddingTop: 0,           // 模拟 Figma 白条顶部 21dp 溢出区
    borderTopRightRadius: 25,
    borderTopLeftRadius: 0,
  },

  /* Tab Bar */
  tabBar: {
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
    width: 30,
    height: 28,
    marginRight: 8,
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
    paddingBottom: 60,    // ← 呼吸区放卡片底部
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
  // 已移除 — 由 card.paddingBottom 60 提供呼吸区

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

  /* Agreement Row */
  agreementRow: {
    marginTop: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    flexWrap: 'wrap',
    paddingHorizontal: 4,
  },
  checkboxWrap: {
    paddingVertical: 4,
    paddingRight: 4,
  },
  agreementTextWrap: {
    paddingVertical: 4,
  },
  checkbox: {
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 1,
    borderColor: TEXT_DARK,
    backgroundColor: 'transparent',
    marginRight: 3,
    justifyContent: 'center',
    alignItems: 'center',
  },
  checkboxOn: {
    backgroundColor: TEXT_DARK,
  },
  checkmark: {
    color: '#F4E7D1',
    fontSize: 9,
    fontWeight: '700',
    lineHeight: 10,
  },
  agreementText: {
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },
  agreementLink: {
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    textDecorationLine: 'underline',
  },
});
