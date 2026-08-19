import { useState } from 'react';
import {
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useRouter } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Fonts } from '@/constants/theme';

/**
 * 登录页 —— 直接照 Figma 帧 221:1738「登录页」实现。
 * 画布 412×917 dp。所有装饰元素相对画布绝对定位，
 * 表单 / 按钮 / 文案部分走 flex，避免手算偏移。
 *
 * Figma 调色：
 *   背景渐变  #FFFFFF → #F4E6CF
 *   卡其绿    #9DA27F   ← 与 theme.bamboo 不同，保留 Figma 原色
 *   主文本    #000000
 *   弱化文本  #898989
 *   状态栏黑  图填充
 *
 * 字体：Figma 用的是 Microsoft YaHei，但 themed-text 走主题 sans 字体栈。
 *       标题保留 sans-serif bold，效果接近。
 */

type LoginMode = 'code' | 'password';

export default function LoginScreen() {
  const router = useRouter();
  const [mode, setMode] = useState<LoginMode>('code');
  const [phone, setPhone] = useState('');
  const [code, setCode] = useState('');
  const [agreed, setAgreed] = useState(false);

  const submit = () => {
    // TODO: 接入真实登录接口；目前先跳到 tabs。
    router.replace('/(tabs)');
  };

  return (
    <View style={styles.root}>
      {/* 背景：白 → 米黄 渐变（双层 View 近似实现） */}
      <View style={[styles.gradientTop, { backgroundColor: '#FFFFFF' }]} />
      <View style={[styles.gradientBottom, { backgroundColor: '#F4E6CF' }]} />

      {/* 装饰图层：低对比度的底纹插画 */}
      <Image
        source={require('@/assets/images/login/bg-decor-1.png')}
        style={[styles.decor, styles.decor1]}
        resizeMode="contain"
      />
      <Image
        source={require('@/assets/images/login/bg-decor-2.png')}
        style={[styles.decor, styles.decor2]}
        resizeMode="contain"
      />
      <Image
        source={require('@/assets/images/login/bg-decor-3.png')}
        style={[styles.decor, styles.decor3]}
        resizeMode="contain"
      />
      <Image
        source={require('@/assets/images/login/decor-side.png')}
        style={[styles.decor, styles.decorSide]}
        resizeMode="contain"
      />

      {/* 顶部状态栏 */}
      <Image
        source={require('@/assets/images/login/statusbar.png')}
        style={styles.statusBar}
        resizeMode="cover"
      />

      <SafeAreaView style={styles.safe} edges={['bottom']}>
        <ScrollView
          contentContainerStyle={styles.scroll}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}>
          {/* WELCOME 标题 */}
          <Text style={styles.welcome}>WELCOME!</Text>

          {/* 登录方式 + 表单整体 */}
          <View style={styles.formArea}>
            {/* 切换 Tab —— 验证码登录 在左（激活态），密码登录 在右 */}
            <View style={styles.tabBar}>
              <TabPill
                active={mode === 'code'}
                label="验证码登录"
                icon={require('@/assets/images/login/icon-face.png')}
                onPress={() => setMode('code')}
                style={styles.tabLeft}
              />
              <TabPill
                active={mode === 'password'}
                label="密码登录"
                onPress={() => setMode('password')}
                style={styles.tabRight}
              />
            </View>

            {/* 主卡片 */}
            <View style={styles.card}>
              {/* 手机号 */}
              <View style={styles.fieldGroup}>
                <Text style={styles.fieldLabel}>手机号</Text>
                <View style={styles.inputRow}>
                  <Image
                    source={require('@/assets/images/login/icon-iphone.png')}
                    style={styles.fieldIcon}
                    resizeMode="contain"
                  />
                  <TextInput
                    style={styles.input}
                    placeholder="请输入手机号"
                    placeholderTextColor="#9DA27F"
                    keyboardType="phone-pad"
                    maxLength={11}
                    value={phone}
                    onChangeText={setPhone}
                  />
                </View>
              </View>

              {/* 验证码 / 密码 */}
              <View style={styles.fieldGroup}>
                <Text style={styles.fieldLabel}>{mode === 'code' ? '验证码' : '密码'}</Text>
                <View style={styles.inputRow}>
                  <Image
                    source={require('@/assets/images/login/icon-safe.png')}
                    style={styles.fieldIcon}
                    resizeMode="contain"
                  />
                  <TextInput
                    style={styles.input}
                    placeholder={mode === 'code' ? '请输入验证码' : '请输入密码'}
                    placeholderTextColor="#9DA27F"
                    keyboardType={mode === 'code' ? 'number-pad' : 'default'}
                    secureTextEntry={mode === 'password'}
                    maxLength={mode === 'code' ? 6 : 20}
                    value={code}
                    onChangeText={setCode}
                  />
                  {mode === 'code' ? (
                    <>
                      <View style={styles.divider} />
                      <Pressable hitSlop={8}>
                        <Text style={styles.suffixText}>获取验证码</Text>
                      </Pressable>
                    </>
                  ) : null}
                </View>

                {mode === 'password' ? (
                  <Pressable
                    hitSlop={8}
                    style={styles.forgotLink}
                    onPress={() => router.push('/forgot')}>
                    <Text style={styles.forgotText}>忘记密码？找回密码</Text>
                  </Pressable>
                ) : null}
              </View>

              {/* 登录按钮 */}
              <Pressable
                style={({ pressed }) => [
                  styles.loginBtn,
                  pressed && styles.loginBtnPressed,
                ]}
                onPress={submit}>
                <Text style={styles.loginBtnText}>登录</Text>
              </Pressable>

              {/* 立即注册 */}
              <View style={styles.registerRow}>
                <Text style={styles.registerText}>没有账号？</Text>
                <Pressable hitSlop={6} onPress={() => router.push('/register')}>
                  <Text style={[styles.registerText, styles.registerLink]}>立即注册</Text>
                </Pressable>
              </View>

              {/* 协议 */}
              <Pressable
                style={styles.agreement}
                onPress={() => setAgreed((v) => !v)}
                accessibilityRole="checkbox"
                accessibilityState={{ checked: agreed }}>
                <View style={[styles.checkbox, agreed && styles.checkboxChecked]}>
                  {agreed ? <Text style={styles.checkmark}>✓</Text> : null}
                </View>
                <Text style={styles.agreementText}>
                  我已阅读并同意
                  <Text style={styles.underlined}>《用户协议》</Text>
                  和
                  <Text style={styles.underlined}>《隐私条款》</Text>
                </Text>
              </Pressable>
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

function TabPill({
  active,
  label,
  icon,
  onPress,
  style,
}: {
  active: boolean;
  label: string;
  icon?: ReturnType<typeof require>;
  onPress: () => void;
  style?: object;
}) {
  return (
    <Pressable
      onPress={onPress}
      style={[styles.tab, active && styles.tabActive, style]}
      accessibilityRole="tab"
      accessibilityState={{ selected: active }}>
      {icon ? (
        <Image source={icon} style={styles.tabIcon} resizeMode="contain" />
      ) : null}
      <Text style={[styles.tabLabel, active && styles.tabLabelActive]}>{label}</Text>
    </Pressable>
  );
}

const CANVAS_WIDTH = 412;
const CANVAS_HEIGHT = 917;

const styles = StyleSheet.create({
  root: {
    flex: 1,
    overflow: 'hidden',
    backgroundColor: '#F5EFE0',
  },
  gradientTop: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    height: CANVAS_HEIGHT / 2,
  },
  gradientBottom: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: CANVAS_HEIGHT / 2,
    bottom: 0,
  },

  // 装饰图层
  decor: {
    position: 'absolute',
    opacity: 0.5,
  },
  decor1: {
    width: 274,
    height: 257,
    left: -97,
    top: 731,
  },
  decor2: {
    width: 386,
    height: 560,
    left: 20,
    top: 357,
  },
  decor3: {
    width: 262,
    height: 244,
    left: -60,
    top: 48,
  },
  decorSide: {
    width: 53.41,
    height: 150.25,
    left: 319,
    top: 144,
    opacity: 0.85,
  },

  statusBar: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    width: CANVAS_WIDTH,
    height: 55.56,
  },

  safe: {
    flex: 1,
  },
  scroll: {
    flexGrow: 1,
    paddingBottom: 40,
  },

  // WELCOME 标题
  welcome: {
    position: 'absolute',
    left: 20,
    top: 86,
    width: 224,
    height: 53,
    fontSize: 40,
    fontWeight: '700',
    color: '#000000',
    fontFamily: Fonts.sans,
    letterSpacing: 1,
    lineHeight: 53,
  },

  // 登录方式 + 表单整体容器
  formArea: {
    marginTop: 287,
    marginHorizontal: 20,
  },

  // 主卡片
  card: {
    marginTop: -22,
    backgroundColor: '#9DA27F',
    borderTopRightRadius: 25,
    paddingHorizontal: 16,
    paddingTop: 52,
    paddingBottom: 28,
  },

  // Tab 区
  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 25,
    borderTopRightRadius: 26,
    height: 92,
    paddingTop: 28,
    paddingBottom: 28,
    paddingHorizontal: 20,
    alignItems: 'center',
  },
  tab: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    paddingVertical: 6,
    borderRadius: 25,
  },
  tabLeft: {
    marginRight: 4,
  },
  tabRight: {
    marginLeft: 4,
  },
  tabActive: {
    backgroundColor: '#9DA27F',
  },
  tabIcon: {
    width: 30,
    height: 28,
    marginRight: 8,
  },
  tabLabel: {
    fontSize: 22,
    fontWeight: '400',
    color: '#898989',
    fontFamily: Fonts.sans,
  },
  tabLabelActive: {
    color: '#000000',
    fontWeight: '600',
  },

  // 表单
  fieldGroup: {
    marginTop: 22,
  },
  fieldLabel: {
    fontSize: 16,
    fontWeight: '400',
    color: '#000000',
    marginBottom: 10,
    fontFamily: Fonts.sans,
  },
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 5,
    paddingHorizontal: 12,
    height: 50,
  },
  fieldIcon: {
    width: 22,
    height: 22,
    marginRight: 6,
  },
  input: {
    flex: 1,
    fontSize: 12,
    color: '#000000',
    paddingVertical: 0,
    fontFamily: Fonts.sans,
  },
  divider: {
    width: 1,
    height: 24,
    backgroundColor: 'rgba(192, 192, 192, 0.57)',
    marginHorizontal: 8,
  },
  suffixText: {
    fontSize: 12,
    color: '#888888',
    fontFamily: Fonts.sans,
    letterSpacing: 0.1,
  },
  helperLink: {
    marginTop: 8,
    alignSelf: 'flex-start',
  },
  helperText: {
    fontSize: 10,
    color: '#000000',
    fontFamily: Fonts.sans,
  },
  forgotLink: {
    marginTop: 8,
    alignSelf: 'flex-end',
  },
  forgotText: {
    fontSize: 12,
    color: '#000000',
    fontFamily: Fonts.sans,
  },

  // 登录按钮
  loginBtn: {
    marginTop: 32,
    height: 56,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  loginBtnPressed: {
    opacity: 0.85,
  },
  loginBtnText: {
    fontSize: 24,
    fontWeight: '400',
    color: '#000000',
    fontFamily: Fonts.sans,
  },

  // 立即注册
  registerRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 14,
  },
  registerText: {
    fontSize: 12,
    color: '#000000',
    fontFamily: Fonts.sans,
  },
  registerLink: {
    fontWeight: '700',
  },

  // 协议
  agreement: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginTop: 28,
    paddingHorizontal: 4,
  },
  checkbox: {
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 1,
    borderColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
    marginRight: 7,
  },
  checkboxChecked: {
    backgroundColor: '#000000',
  },
  checkmark: {
    color: '#FFFFFF',
    fontSize: 10,
    lineHeight: 12,
  },
  agreementText: {
    flex: 1,
    fontSize: 10,
    color: '#000000',
    letterSpacing: 1,
    fontFamily: Fonts.sans,
    lineHeight: 13,
  },
  underlined: {
    textDecorationLine: 'underline',
  },
});