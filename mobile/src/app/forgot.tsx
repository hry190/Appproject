import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Image, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { Layout } from '@/constants/theme';
import pandaDecor from '@/assets/images/login/bg-decor-3.png';
import pandaThinking from '@/assets/images/login/panda-thinking.png';
import decorBamboo from '@/assets/images/login/decor-2.png';
import iconPerson from '@/assets/images/login/icon-person.png';
import iconSafe from '@/assets/images/login/icon-safe.png';

/**
 * 忘记密码页 —— 按 Figma 帧 56.png「忘记密码」实现。
 *
 * 布局策略（与 login.tsx 一致）：
 *   1. SafeAreaView 顶部 + ScrollView 兜底，适配不同屏幕
 *   2. 上半 cream 区：返回箭头 + 大标题 + 右上角熊猫装饰
 *   3. 下半 green 卡（圆角顶部）：表单字段 + 确定修改 + 协议行
 *   4. Layout token 化 —— padding/gap/radius 全走常量
 *   5. maxFontSizeMultiplier 防 iOS 系统字号放大破版
 *
 * 调色（来源 56.png 实测）：
 *   背景米黄  #F4E7D1
 *   卡其绿    #9DA27F
 *   按钮米黄  #F7ECDA
 *   输入框白  #FFFFFF
 *   主文本    #000000
 *   弱化文本  #888888 / #898989
 */

const OLIVE = '#9DA27F';
const CREAM = '#F4E7D1';
const CREAM_BTN = '#F7ECDA';
const WHITE = '#FFFFFF';
const TEXT_DARK = '#000000';
const PLACEHOLDER = '#898989';
const DIVIDER_GRAY = '#C0C0C0';
const ACTION_GRAY = '#888888';

export default function ForgotScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const [account, setAccount] = useState('');
  const [code, setCode] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [confirm, setConfirm] = useState('');
  const [agreed, setAgreed] = useState(false);

  const handleSubmit = () => {
    if (!agreed) {
      console.warn('[forgot] 未勾选用户协议');
      return;
    }
    if (newPwd !== confirm) {
      console.warn('[forgot] 两次密码不一致');
      return;
    }
    // TODO: 接入找回密码 API
    router.replace('/login');
  };

  return (
    <SafeAreaView edges={['top']} style={styles.root}>
      <View style={styles.canvas}>
        <ScrollView
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}>

          {/* 1. 顶部标题区：返回箭头 + 「忘记密码」 */}
          <View style={styles.header}>
            <Pressable
              hitSlop={12}
              onPress={() => router.back()}
              accessibilityRole="button"
              accessibilityLabel="返回登录页"
              style={styles.backBtn}>
              <Text style={styles.backArrow}>‹</Text>
            </Pressable>
            <Text style={styles.title} maxFontSizeMultiplier={1.4} allowFontScaling>
              忘记密码
            </Text>
          </View>

          {/* 2. 卡其绿主区（圆角顶部） */}
          <View style={styles.card}>
            {/* 卡内竹枝装饰图（绝对覆盖） */}
            <Image
              source={decorBamboo}
              style={styles.cardDecor}
              resizeMode="cover"
              pointerEvents="none"
            />

            {/* 字段 1：账号 */}
            <View style={styles.field}>
              <Text style={styles.label} allowFontScaling>账号</Text>
              <View style={styles.inputBox}>
                <Image
                  source={iconPerson}
                  style={styles.fieldIcon}
                  resizeMode="contain"
                />
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

            {/* 字段 2：验证码 */}
            <View style={[styles.field, styles.fieldGap]}>
              <Text style={styles.label} allowFontScaling>验证码</Text>
              <View style={styles.inputBox}>
                <Image
                  source={iconSafe}
                  style={styles.fieldIcon}
                  resizeMode="contain"
                />
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
                <Pressable hitSlop={8} onPress={() => { /* TODO: 触发验证码 */ }}>
                  <Text
                    style={styles.actionText}
                    maxFontSizeMultiplier={1.6}
                    allowFontScaling>
                    获取验证码
                  </Text>
                </Pressable>
              </View>
            </View>

            {/* 字段 3：设置新密码 */}
            <View style={[styles.field, styles.fieldGap]}>
              <Text style={styles.label} allowFontScaling>设置新密码</Text>
              <View style={styles.inputBox}>
                <TextInput
                  style={styles.input}
                  placeholder="请输入密码"
                  placeholderTextColor={PLACEHOLDER}
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={newPwd}
                  onChangeText={setNewPwd}
                  maxFontSizeMultiplier={1.4}
                  allowFontScaling
                />
              </View>
            </View>

            {/* 字段 4：确定密码 */}
            <View style={[styles.field, styles.fieldGap]}>
              <Text style={styles.label} allowFontScaling>确定密码</Text>
              <View style={styles.inputBox}>
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

            {/* 确定修改按钮 */}
            <Pressable
              accessibilityRole="button"
              accessibilityLabel="确定修改"
              onPress={handleSubmit}
              style={({ pressed }) => [styles.submitBtn, pressed && styles.btnPressed]}>
              <Text
                style={styles.submitBtnText}
                maxFontSizeMultiplier={1.4}
                allowFontScaling>
                确定修改
              </Text>
            </Pressable>

            {/* 协议行 */}
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

        {/* 右上角熊猫装饰（绝对覆盖） */}
        <Image
          source={pandaDecor}
          style={styles.pandaDecor}
          resizeMode="contain"
          pointerEvents="none"
        />

        {/* 思考熊猫装饰（绝对覆盖） */}
        <Image
          source={pandaThinking}
          style={styles.pandaThinking}
          resizeMode="contain"
          pointerEvents="none"
        />
      </View>
    </SafeAreaView>
  );
}

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

  /* Header：返回箭头 + 标题 */
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingTop: 24,
  },
  backBtn: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 4,
  },
  backArrow: {
    fontSize: 36,
    lineHeight: 38,
    color: TEXT_DARK,
    fontWeight: '300',
    includeFontPadding: false,
  },
  title: {
    marginLeft: 12,
    fontSize: 40,
    fontWeight: '700',
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
  },

  /* Card */
  card: {
    marginTop: 60,
    marginHorizontal: 0,
    flex: 1,
    borderTopLeftRadius: Layout.radiusCard,
    borderTopRightRadius: Layout.radiusCard,
    backgroundColor: OLIVE,
    paddingHorizontal: Layout.contentInset,
    paddingTop: 32,
    paddingBottom: 60,
  },

  /* Card Decor */
  cardDecor: {
    position: 'absolute',
    top: 0,
    left: -50,
    right: 0,
    bottom: 0,
    opacity: 0.85,
  },

  /* Panda Decorations */
  pandaDecor: {
    position: 'absolute',
    top: 30,
    right: 0,
    width: 260,
    height: 240,
    opacity: 0.9,
  },
  pandaThinking: {
    position: 'absolute',
    top: 130,
    right: 30,
    width: 110,
    height: 150,
    opacity: 0.95,
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
  fieldIcon: {
    width: 22,
    height: 22,
    marginRight: 12,
  },
  input: {
    flex: 1,
    height: Layout.inputH,    // 与 inputBox 等高（50），消除空隙抖动
    minHeight: Layout.inputH,
    fontSize: 12,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1.2,
    paddingVertical: 0,
    lineHeight: 18,              // 显式行高
    textAlignVertical: 'center', // Android 垂直居中
    includeFontPadding: false,   // 去掉 Android 默认字体内边距
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

  /* Submit Button */
  submitBtn: {
    marginTop: 32,
    paddingVertical: 16,
    borderRadius: Layout.radiusButton,
    backgroundColor: CREAM_BTN,
    justifyContent: 'center',
    alignItems: 'center',
  },
  btnPressed: { opacity: 0.85 },
  submitBtnText: {
    fontSize: 24,
    color: TEXT_DARK,
    fontFamily: 'Microsoft YaHei',
  },

  /* Agreement Row */
  agreementRow: {
    marginTop: 24,
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
    color: CREAM,
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