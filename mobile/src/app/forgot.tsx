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
 * 忘记密码页 —— 按 Figma 帧 221:2109「忘记密码」实现。
 * 画布 412×917 dp。所有元素按 Figma 坐标绝对放置。
 *
 * Figma 调色：
 *   背景渐变  #FFFFFF → #F5E8D3
 *   卡其绿    #9DA27F
 *   主文本    #000000
 *   弱化文本  #898989
 *
 * 字体：Figma 用 Microsoft YaHei；这里走主题 sans-serif 栈。
 */

export default function ForgotScreen() {
  const router = useRouter();
  const [account, setAccount] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [agreed, setAgreed] = useState(false);

  const submit = () => {
    // TODO: 接入真实「找回密码」接口。
    if (newPassword !== confirm) {
      console.warn('[forgot] 两次密码不一致');
      return;
    }
    router.replace('/login');
  };

  return (
    <View style={styles.root}>
      {/* 背景：白 → 米黄 渐变（双层 View 近似实现） */}
      <View style={styles.gradientTop} />
      <View style={styles.gradientBottom} />

      {/* 卡其绿卡片（右上圆角） */}
      <View style={styles.cardBg} />

      {/* 右上角熊猫装饰 */}
      <Image
        source={require('@/assets/images/login/bg-decor-3.png')}
        style={styles.pandaTopRight}
        resizeMode="contain"
      />

      {/* 思考熊猫装饰 */}
      <Image
        source={require('@/assets/images/login/panda-thinking.png')}
        style={styles.pandaThinking}
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
          {/* Header：返回箭头 + 标题 */}
          <View style={styles.header}>
            <Pressable
              hitSlop={12}
              onPress={() => router.back()}
              accessibilityRole="button"
              accessibilityLabel="返回登录页">
              <Image
                source={require('@/assets/images/login/Left .png')}
                style={styles.backArrow}
                resizeMode="contain"
              />
            </Pressable>
            <Text style={styles.title}>忘记密码</Text>
          </View>

          {/* 表单区 */}
          <View style={styles.formArea}>
            {/* 账号（含人脸图标） */}
            <View style={styles.fieldAccount}>
              <Image
                source={require('@/assets/images/login/icon-person.png')}
                style={styles.accountIcon}
                resizeMode="contain"
              />
              <Text style={styles.fieldLabel}>账号</Text>
              <View style={styles.inputBox}>
                <TextInput
                  style={styles.input}
                  placeholder="请输入账号"
                  placeholderTextColor="#9DA27F"
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={account}
                  onChangeText={setAccount}
                />
              </View>
            </View>

            {/* 验证码（含盾牌 + 获取验证码） */}
            <View style={styles.fieldCode}>
              <Image
                source={require('@/assets/images/login/icon-safe.png')}
                style={styles.codeIcon}
                resizeMode="contain"
              />
              <Text style={styles.fieldLabel}>验证码 </Text>
              <View style={styles.codeInputRow}>
                <TextInput
                  style={styles.codeInput}
                  placeholder="请输入验证码"
                  placeholderTextColor="#9DA27F"
                  keyboardType="number-pad"
                  maxLength={6}
                  value={code}
                  onChangeText={setCode}
                />
                <View style={styles.codeDivider} />
                <Pressable hitSlop={8}>
                  <Text style={styles.suffixText}>获取验证码</Text>
                </Pressable>
              </View>
            </View>

            {/* 设置新密码（无图标） */}
            <View style={styles.fieldNewPassword}>
              <Text style={styles.fieldLabelNoIcon}>设置新密码</Text>
              <View style={styles.inputBox}>
                <TextInput
                  style={styles.input}
                  placeholder="请输入密码"
                  placeholderTextColor="#9DA27F"
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={newPassword}
                  onChangeText={setNewPassword}
                />
              </View>
            </View>

            {/* 确定密码（无图标） */}
            <View style={styles.fieldConfirm}>
              <Text style={styles.fieldLabelNoIcon}>确定密码</Text>
              <View style={styles.inputBox}>
                <TextInput
                  style={styles.input}
                  placeholder="请确认密码"
                  placeholderTextColor="#9DA27F"
                  secureTextEntry
                  autoCapitalize="none"
                  autoCorrect={false}
                  value={confirm}
                  onChangeText={setConfirm}
                />
              </View>
            </View>

            {/* 确定修改按钮 */}
            <Pressable
              style={({ pressed }) => [
                styles.submitBtn,
                pressed && styles.submitBtnPressed,
              ]}
              onPress={submit}>
              <Text style={styles.submitBtnText}>确定修改</Text>
            </Pressable>
          </View>

          {/* 协议（绝对定位，对齐 Figma y=858） */}
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

          {/* Home Indicator */}
          <Image
            source={require('@/assets/images/login/home-indicator.png')}
            style={styles.homeIndicator}
            resizeMode="contain"
          />
        </ScrollView>
      </SafeAreaView>
    </View>
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
    backgroundColor: '#FFFFFF',
  },
  gradientBottom: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: CANVAS_HEIGHT / 2,
    bottom: 0,
    backgroundColor: '#F5E8D3',
  },

  // 卡其绿卡片（右上圆角）
  cardBg: {
    position: 'absolute',
    left: 20,
    right: 20,
    top: 287,
    bottom: 0,
    backgroundColor: '#9DA27F',
    borderTopRightRadius: 25,
  },

  // 右上角熊猫
  pandaTopRight: {
    position: 'absolute',
    left: 203,
    top: 50,
    width: 262,
    height: 244,
    opacity: 0.85,
  },

  // 思考熊猫
  pandaThinking: {
    position: 'absolute',
    left: 288,
    top: 172,
    width: 95.55,
    height: 131.63,
    opacity: 0.95,
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

  // Header：返回箭头 + 标题
  header: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 88,
    height: 50,
    flexDirection: 'row',
    alignItems: 'center',
  },
  backArrow: {
    width: 22,
    height: 22,
    marginLeft: 9,
  },
  title: {
    marginLeft: 18,
    fontSize: 36,
    fontWeight: '700',
    color: '#000000',
    fontFamily: Fonts.sans,
  },

  // 表单区
  formArea: {
    marginTop: 334,
    marginHorizontal: 40,
  },

  // 账号字段（label + icon 在顶行）
  fieldAccount: {
    marginBottom: 8,
  },
  accountIcon: {
    position: 'absolute',
    left: 1,
    top: 0,
    width: 22,
    height: 22,
  },
  fieldLabel: {
    marginLeft: 23,
    fontSize: 16,
    fontWeight: '400',
    color: '#000000',
    fontFamily: Fonts.sans,
    lineHeight: 21,
  },
  inputBox: {
    marginTop: 11,
    height: 35,
    backgroundColor: '#FFFFFF',
    borderRadius: 5,
    paddingHorizontal: 14,
    justifyContent: 'center',
  },
  input: {
    fontSize: 12,
    color: '#000000',
    fontFamily: Fonts.sans,
    paddingVertical: 0,
  },

  // 验证码字段
  fieldCode: {
    marginTop: 8,
  },
  codeIcon: {
    position: 'absolute',
    left: 0,
    top: 0,
    width: 22,
    height: 22,
  },
  codeInputRow: {
    marginTop: 11,
    height: 35,
    backgroundColor: '#FFFFFF',
    borderRadius: 5,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
  },
  codeInput: {
    flex: 1,
    fontSize: 12,
    color: '#000000',
    fontFamily: Fonts.sans,
    paddingVertical: 0,
  },
  codeDivider: {
    width: 1,
    height: 24,
    backgroundColor: 'rgba(192, 192, 192, 0.57)',
    marginHorizontal: 8,
  },
  suffixText: {
    fontSize: 12,
    color: '#888888',
    fontFamily: Fonts.sans,
  },

  // 设置新密码 / 确定密码（无图标）
  fieldNewPassword: {
    marginTop: 8,
  },
  fieldConfirm: {
    marginTop: 8,
  },
  fieldLabelNoIcon: {
    fontSize: 16,
    fontWeight: '400',
    color: '#000000',
    fontFamily: Fonts.sans,
    lineHeight: 21,
  },

  // 确定修改按钮
  submitBtn: {
    marginTop: 32,
    height: 56,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  submitBtnPressed: {
    opacity: 0.85,
  },
  submitBtnText: {
    fontSize: 24,
    fontWeight: '400',
    color: '#000000',
    fontFamily: Fonts.sans,
  },

  // 协议
  agreement: {
    position: 'absolute',
    left: 89,
    right: 85,
    top: 858,
    flexDirection: 'row',
    alignItems: 'flex-start',
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

  // Home Indicator
  homeIndicator: {
    position: 'absolute',
    left: 5,
    right: 5,
    top: 878,
    width: 402,
    height: 34,
  },
});