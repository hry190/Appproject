import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Image, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { Layout } from '@/constants/theme';
import decorSide from '@/assets/images/login/decor-side.png';
import pandaFace from '@/assets/images/login/panda-face.png';
import decor1 from '@/assets/images/login/decor-1.png';
import decor2 from '@/assets/images/login/decor-2.png';
import decor3 from '@/assets/images/login/decor-3.png';
import peopleSafe from '@/assets/images/login/People-safe.png';
import keyIcon from '@/assets/images/login/Key.png';

/**
 * 注册页 —— flex + ScrollView 适配方案（与登录页一致）。
 *
 * 字段：
 *   1. 账号
 *   2. 设置密码
 *   3. 确定密码
 *   4. 注册按钮
 *   5. 协议行
 */

const OLIVE = '#9DA27F';
const CREAM = '#F4E7D1';
const CREAM_BTN = '#F7ECDA';
const WHITE = '#FFFFFF';
const TEXT_DARK = '#000000';
const PLACEHOLDER = '#898989';
const ACTION_GRAY = '#888888';

export default function RegisterScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const [account, setAccount] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [agreed, setAgreed] = useState(false);

  const handleSubmit = () => {
    if (!agreed) {
      console.warn('[register] 未勾选用户协议');
      return;
    }
    if (password !== confirm) {
      console.warn('[register] 两次密码不一致');
      return;
    }
    // TODO: 接入注册 API
    router.replace('/login');
  };

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <View style={styles.canvas}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}>

          {/* 1. 顶部标题区：返回按钮 + panda 脸 + 「注册」 */}
          <View style={styles.header}>
            <Pressable
              hitSlop={12}
              onPress={() => router.back()}
              accessibilityRole="button"
              accessibilityLabel="返回登录页"
              style={styles.backBtn}>
              <Text style={styles.backArrow}>‹</Text>
            </Pressable>
            <Image
              source={pandaFace}
              style={styles.faceIcon}
              resizeMode="contain"
            />
            <Text style={styles.title} maxFontSizeMultiplier={1.4} allowFontScaling>
              注册
            </Text>
          </View>

          {/* 2. WELCOME! 标题 */}
          <View style={styles.welcomeWrap}>
            <Text
              style={styles.welcome}
              maxFontSizeMultiplier={1.25}
              allowFontScaling>
              WELCOME!
            </Text>
          </View>

          {/* 3. 主区卡：flex:1 拿剩余高度 */}
          <View style={styles.card}>
            {/* 装饰图：卡背景竹枝 */}
            <Image
              source={decor2}
              style={styles.cardDecor}
              resizeMode="cover"
              pointerEvents="none"
            />

            {/* 字段 1：账号 */}
            <View style={styles.field}>
              <Text style={styles.label} allowFontScaling>账号</Text>
              <View style={styles.inputBox}>
                <PersonGlyph />
                <TextInput
                  style={styles.input}
                  placeholder="请输入账号"
                  placeholderTextColor={PLACEHOLDER}
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={account}
                  onChangeText={setAccount}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling
                />
              </View>
            </View>

            {/* 字段 2：设置密码 */}
            <View style={[styles.field, styles.fieldGap]}>
              <Text style={styles.label} allowFontScaling>设置密码</Text>
              <View style={styles.inputBox}>
                <ShieldGlyph variant="password" />
                <TextInput
                  style={styles.input}
                  placeholder="请输入密码"
                  placeholderTextColor={PLACEHOLDER}
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={password}
                  onChangeText={setPassword}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling
                />
              </View>
            </View>

            {/* 字段 3：确定密码 */}
            <View style={[styles.field, styles.fieldGap]}>
              <Text style={styles.label} allowFontScaling>确定密码</Text>
              <View style={styles.inputBox}>
                <ShieldGlyph variant="password" />
                <TextInput
                  style={styles.input}
                  placeholder="请确认密码"
                  placeholderTextColor={PLACEHOLDER}
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={confirm}
                  onChangeText={setConfirm}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling
                />
              </View>
            </View>

            {/* Spacer：把下面推到卡的下半 */}
            <View style={styles.spacer} />

            {/* 注册按钮 */}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel="注册"
              onPress={handleSubmit}
              style={({ pressed }) => [styles.registerBtn, pressed && styles.btnPressed]}>
              <Text
                style={styles.registerBtnText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                注册
              </Text>
            </Pressable>

            {/* 协议行（圆点复选框 + 文本） */}
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

            {/* 底部安全区缓冲 */}
            <View style={{ height: insets.bottom }} />
          </View>
        </ScrollView>

        {/* decor-side 角色立绘（右侧装饰） */}
        <Image
          source={decorSide}
          style={[styles.ipNiang, { pointerEvents: 'none' }]}
          resizeMode="contain"
        />

        {/* 装饰图 decor-3：右上角灰色竹叶 */}
        <Image
          source={decor3}
          style={[styles.decor3, { pointerEvents: 'none' }]}
          resizeMode="contain"
        />

        {/* 装饰图 decor-1：底部水墨氛围 */}
        <Image
          source={decor1}
          style={[styles.decor1, { pointerEvents: 'none' }]}
          resizeMode="contain"
        />
      </View>
    </SafeAreaView>
  );
}

/* —— 图标（与登录页一致） —— */

function PersonGlyph() {
  return (
    <View style={glyphStyles.wrap}>
      <View style={glyphStyles.personHead} />
      <View style={glyphStyles.personBody} />
    </View>
  );
}

function ShieldGlyph({ variant }: { variant: 'code' | 'password' }) {
  return (
    <Image
      source={variant === 'code' ? peopleSafe : keyIcon}
      style={glyphStyles.wrap}
      resizeMode="contain"
    />
  );
}

const glyphStyles = StyleSheet.create({
  wrap: {
    width: 22,
    height: 22,
    marginRight: 12,
  },
  personHead: {
    position: 'absolute',
    top: 2,
    left: 8,
    width: 6,
    height: 6,
    borderRadius: 3,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
  },
  personBody: {
    position: 'absolute',
    bottom: 0,
    left: 4,
    width: 14,
    height: 9,
    borderWidth: 1.5,
    borderColor: TEXT_DARK,
    borderTopLeftRadius: 7,
    borderTopRightRadius: 7,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
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

  /* 顶部标题区 */
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 12,
    marginTop: 12,
  },
  backBtn: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 4,
  },
  backArrow: {
    fontSize: 32,
    lineHeight: 36,
    color: TEXT_DARK,
    fontWeight: '300',
    includeFontPadding: false,
  },
  faceIcon: {
    width: 36,
    height: 36,
    marginRight: 8,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 5,
  },

  /* WELCOME! */
  welcomeWrap: {
    paddingHorizontal: Layout.contentInset,
    paddingTop: 24,
    marginTop: 8,
  },
  welcome: {
    fontSize: 40,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
  },

  /* IP娘 + decor */
  ipNiang: {
    position: 'absolute',
    top: 80,
    right: 0,
    width: 70,
    height: 100,
  },
  decor3: {
    position: 'absolute',
    top: 0,
    left: -40,
    width: 220,
    height: 220,
    opacity: 0.9,
  },
  decor1: {
    position: 'absolute',
    bottom: 0,
    left: -50,
    width: 200,
    height: 190,
    opacity: 0.5,
  },
  cardDecor: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    opacity: 0.85,
  },

  /* Card */
  card: {
    marginTop: 24,
    marginHorizontal: Layout.px,
    flex: 1,
    borderTopLeftRadius: Layout.radiusCard,
    borderTopRightRadius: Layout.radiusCard,
    backgroundColor: OLIVE,
    paddingHorizontal: Layout.contentInset,
    paddingTop: 60,
    paddingBottom: 60,
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

  /* Spacer */
  // 已移除 — 由 card.paddingBottom 60 提供呼吸区

  /* Register Button */
  registerBtn: {
    marginTop: 24,
    paddingVertical: 16,
    borderRadius: Layout.radiusButton,
    backgroundColor: CREAM_BTN,
    justifyContent: 'center',
    alignItems: 'center',
  },
  btnPressed: { opacity: 0.85 },
  registerBtnText: {
    fontSize: 24,
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