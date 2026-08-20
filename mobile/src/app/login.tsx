import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

/**
 * 登录页 —— 验证码登录 / 密码登录 双 Tab 切换。
 * 视觉参考 Figma 节点 185:1476：米黄底、橄榄绿卡、白色圆角输入、
 * 米黄大按钮、黑色 Home Indicator。
 *
 * 设计 token（与 mobile/src/constants/theme.ts 一致）：
 *   米黄     #F4E7D1  (页面底)
 *   橄榄绿   #9DA27F  (Tab / 主体卡)
 *   按钮米黄 #F7ECDA
 *   文字     #000 / 占位 #898989
 *   字体     Microsoft YaHei
 */

const OLIVE = '#9DA27F';
const CREAM = '#F4E7D1';
const CREAM_BTN = '#F7ECDA';
const WHITE = '#FFFFFF';
const TEXT_DARK = '#000000';
const PLACEHOLDER = '#898989';

// 触发 Metro 重 bundle（duplicate OLIVE 已修复）

type LoginMode = 'code' | 'password';

export default function LoginScreen() {
  const router = useRouter();
  const [mode, setMode] = useState<LoginMode>('code');
  const [phone, setPhone] = useState('');
  const [secret, setSecret] = useState('');

  const handleLogin = () => {
    // TODO: 接入登录 API；当前直接进 Tab
    router.replace('/(tabs)');
  };

  return (
    <SafeAreaView style={styles.root}>
      {/* WELCOME! 标题：x=20 y=26 w=224 h=53 微软雅黑 Bold 40 */}
      <Text style={styles.welcome}>WELCOME!</Text>

      {/* 顶部 Tab 切换条：左 olive+face 「验证码登录」 / 右 白 「密码登录」 */}
      <View style={styles.tabBar}>
        <Pressable
          accessibilityRole="tab"
          accessibilityState={{ selected: mode === 'code' }}
          onPress={() => setMode('code')}
          style={[styles.tabHalf, styles.tabActive]}>
          {/* 简易小脸图标：圆底 + 双眼 + 嘴 */}
          <View style={styles.faceIcon}>
            <View style={[styles.eye, styles.eyeLeft]} />
            <View style={[styles.eye, styles.eyeRight]} />
            <View style={styles.faceMouth} />
          </View>
          <Text style={[styles.tabText, styles.tabTextActive]}>验证码登录</Text>
        </Pressable>

        <Pressable
          accessibilityRole="tab"
          accessibilityState={{ selected: mode === 'password' }}
          onPress={() => setMode('password')}
          style={[styles.tabHalf, styles.tabInactive]}>
          <Text style={[styles.tabText, styles.tabTextInactive]}>密码登录</Text>
        </Pressable>
      </View>

      {/* 主体卡：橄榄绿，Figma y≈240 起，h≈560 */}
      <View style={styles.card}>
        {/* 手机号字段：label + 白色圆角输入 */}
        <View style={[styles.field, styles.fieldTop]}>
          <Text style={styles.label}>手机号</Text>
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
            />
          </View>
        </View>

        {/* 验证码 / 密码 字段：复用同结构 */}
        <View style={[styles.field, styles.fieldMid]}>
          <Text style={styles.label}>{mode === 'code' ? '验证码' : '密码'}</Text>
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
            />
            {mode === 'code' && (
              <>
                <View style={styles.divider} />
                <Pressable hitSlop={8} onPress={() => { /* TODO: 触发验证码 */ }}>
                  <Text style={styles.actionText}>获取验证码</Text>
                </Pressable>
              </>
            )}
          </View>
        </View>

        {/* 登录按钮：圆角米黄，按钮文字 24px 居中 */}
        <Pressable
          accessibilityRole="button"
          accessibilityLabel="登录"
          onPress={handleLogin}
          style={({ pressed }) => [styles.loginBtn, pressed && styles.btnPressed]}>
          <Text style={styles.loginBtnText}>登录</Text>
        </Pressable>

        {/* 注册链接：Figma 上没有下划线，按纯文字实现 */}
        <Pressable
          accessibilityRole="link"
          accessibilityLabel="立即注册"
          onPress={() => router.push('/register')}
          hitSlop={8}>
          <Text style={styles.registerLink}>没有账号？立即注册</Text>
        </Pressable>
      </View>

      {/* Home Indicator：Figma y≈1577，宽 144 高 5 */}
      <View style={styles.homeIndicator} />
    </SafeAreaView>
  );
}

/* -------- 内置图标（用 View 拼装，无第三方 SVG 依赖） -------- */

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
  // iPhone 简化图标
  iphoneBody: {
    width: 14,
    height: 20,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
    borderRadius: 2,
    backgroundColor: 'transparent',
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
  // Shield 简化图标（圆顶 + 勾）
  shieldBody: {
    width: 18,
    height: 18,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
    borderTopLeftRadius: 9,
    borderTopRightRadius: 9,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    transform: [{ rotate: '180deg' }],
    backgroundColor: 'transparent',
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

/* -------- 样式 -------- */

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: CREAM,
  },

  /* WELCOME! */
  welcome: {
    position: 'absolute',
    left: 20,
    top: 26,
    fontSize: 40,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
    lineHeight: 53,
  },

  /* Tab 切换条 */
  tabBar: {
    position: 'absolute',
    left: 8,
    top: 102,
    width: 357,
    height: 71,
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
  tabActive: {
    backgroundColor: OLIVE,
  },
  tabInactive: {
    backgroundColor: WHITE,
  },
  tabText: {
    fontSize: 24,
    fontFamily: 'Microsoft YaHei',
  },
  tabTextActive: {
    color: WHITE,
    fontWeight: '500',
  },
  tabTextInactive: {
    color: TEXT_DARK,
    fontWeight: '400',
  },

  /* 简易脸图标 */
  faceIcon: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F5DEC1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    overflow: 'hidden',
    borderWidth: 0.5,
    borderColor: '#CEB187',
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

  /* 主体卡 */
  card: {
    position: 'absolute',
    left: 8,
    top: 173,
    width: 357,
    height: 564,
    backgroundColor: OLIVE,
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
  },

  /* 字段通用 */
  field: {
    position: 'absolute',
    left: 23,
    width: 332,
  },
  fieldTop: { top: 60 },
  fieldMid: { top: 152 },
  label: {
    fontSize: 16,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    lineHeight: 21,
    marginBottom: 5,
  },
  inputBox: {
    flexDirection: 'row',
    alignItems: 'center',
    height: 50,
    borderRadius: 5,
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
    backgroundColor: '#C0C0C0',
    opacity: 0.57,
    marginHorizontal: 12,
  },
  actionText: {
    fontSize: 12,
    color: '#888888',
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1.2,
  },

  /* 按钮 */
  loginBtn: {
    position: 'absolute',
    left: 23,
    top: 268,
    width: 332,
    height: 56,
    borderRadius: 10,
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
  registerLink: {
    position: 'absolute',
    left: 23,
    top: 348,
    width: 332,
    textAlign: 'center',
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },

  /* Home Indicator */
  homeIndicator: {
    position: 'absolute',
    alignSelf: 'center',
    bottom: 8,
    width: 144,
    height: 5,
    borderRadius: 100,
    backgroundColor: TEXT_DARK,
  },
});
