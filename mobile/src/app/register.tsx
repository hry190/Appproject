import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Image, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { Fonts } from '@/constants/theme';

/**
 * 注册页 —— 已接入可输入的表单。
 *
 * 视觉装饰层（背景 / 标题 / 卡片 / 装饰图）+ 交互层（账号 / 密码 / 验证码字段 + 协议）。
 * 字段由 React Native 组件渲染（label + icon + TextInput），不再是静态 PNG。
 */

export default function RegisterScreen() {
  const router = useRouter();
  const [account, setAccount] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [agreed, setAgreed] = useState(false);

  const handleSubmit = () => {
    // TODO: 接入注册 API；目前仅打印校验
    if (password !== confirm) {
      console.warn('[register] 两次密码不一致');
      return;
    }
    router.replace('/login');
  };

  return (
    <View style={styles.root}>
      {/* 背景：竹叶铺底 */}
      <Image
        source={require('@/assets/images/login/s.png')}
        style={styles.bg}
        resizeMode="cover"
      />

      {/* 返回箭头：x=20 y=100 w=22 h=22 */}
      <Pressable
        hitSlop={12}
        onPress={() => router.back()}
        accessibilityRole="button"
        accessibilityLabel="返回登录页"
        style={styles.backBtn}>
        <Image
          source={require('@/assets/images/login/Left .png')}
          style={styles.backArrow}
          resizeMode="contain"
        />
      </Pressable>

      {/* 熊猫脸图标：x=56 y=92 w=38.3 h=35 */}
      <Image
        source={require('@/assets/images/login/icon-face.png')}
        style={styles.faceIcon}
        resizeMode="contain"
      />

      {/* 「注册」标题：x=100 y=56 微软雅黑 Bold */}
      <Text style={styles.title}>注册</Text>

      {/* 「WELCOME!」标题：x=20 y=129 w=224 h=53 微软雅黑 Bold */}
      <Text style={styles.welcome}>WELCOME!</Text>

      {/* 右上装饰（竹枝插画）：x=203 y=50 w=262 h=244 */}
      <Image
        source={require('@/assets/images/login/4.png')}
        style={styles.bambooTopRight}
        resizeMode="contain"
      />

      {/* 右侧装饰：熊猫角色 */}
      <Image
        source={require('@/assets/images/login/decor-side.png')}
        style={styles.decorSide}
        resizeMode="contain"
      />

      {/* 卡其绿卡片（半透明，背景竹叶可见）*/}
      <View style={styles.card} />

      {/* —— 表单字段 —— */}

      {/* 账号字段：label + 人形图标 + 输入框 */}
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

      {/* 设置新密码 */}
      <View style={styles.fieldPassword}>
        <Text style={styles.fieldLabelNoIcon}>设置密码</Text>
        <View style={styles.inputBox}>
          <TextInput
            style={styles.input}
            placeholder="请输入密码"
            placeholderTextColor="#9DA27F"
            secureTextEntry
            autoCapitalize="none"
            autoCorrect={false}
            value={password}
            onChangeText={setPassword}
          />
        </View>
      </View>

      {/* 确定密码 */}
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

      {/* 注册按钮（PNG 渲染的可点击区域） */}
      <Pressable
        style={styles.registerBtn}
        onPress={handleSubmit}
        accessibilityRole="button"
        accessibilityLabel="注册">
        <Image
          source={require('@/assets/images/login/Group 117.png')}
          style={styles.registerBtnImage}
          resizeMode="contain"
        />
      </Pressable>

      {/* 协议：x=89 y=855 w=235 h=14（Group 118.png 背景 + 可交互复选框覆盖） */}
      <View style={styles.agreement}>
        <Image
          source={require('@/assets/images/login/Group 118.png')}
          style={styles.agreementImage}
          resizeMode="contain"
        />
        {/* 复选框覆盖层：勾选时盖住 Group 118.png 的 ○，显示 ●✓ */}
        <Pressable
          style={styles.checkboxHit}
          onPress={() => setAgreed((v) => !v)}
          accessibilityRole="checkbox"
          accessibilityState={{ checked: agreed }}>
          {agreed ? (
            <View style={styles.checkboxFilled}>
              <Text style={styles.checkmark}>✓</Text>
            </View>
          ) : null}
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#F4E7D1',
    overflow: 'hidden',
  },
  bg: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 0,
    bottom: 0,
    width: '100%',
    height: '100%',
  },
  backBtn: {
    position: 'absolute',
    left: 20,
    top: 52,
    width: 22,
    height: 22,
  },
  backArrow: {
    width: 22,
    height: 22,
  },
  faceIcon: {
    position: 'absolute',
    left: 56,
    top: 50,
    width: 38.3,
    height: 35,
  },
  bambooTopRight: {
    position: 'absolute',
    left: 143,
    top: 10,
    width: 250,
    height: 240,
    opacity: 0.85,
  },
  decorSide: {
    position: 'absolute',
    left: 300,
    top: 100,
    width: 53.41,
    height: 150.25,
  },
  title: {
    position: 'absolute',
    left: 100,
    top: 46,
    fontSize: 32,
    fontWeight: '700',
    color: '#000000',
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 5,
    lineHeight: 43,
  },
  welcome: {
    position: 'absolute',
    left: 10,
    top: 90,
    width: 224,
    height: 53,
    fontSize: 40,
    fontWeight: '700',
    color: '#000000',
    fontFamily: 'Microsoft YaHei',
    letterSpacing: 1,
    lineHeight: 53,
  },

  // 卡其绿卡片
  card: {
    position: 'absolute',
    left: 8,
    top: 245,
    width: 345,
    height: 560,
    backgroundColor: '#9DA27F',
    borderTopLeftRadius: 25,
    borderTopRightRadius: 25,
    opacity: 0.85,
  },

  // —— 表单字段 ——
  fieldAccount: {
    position: 'absolute',
    left: 15,
    top: 253,
    width: 332,
    height: 67,
  },
  accountIcon: {
    position: 'absolute',
    left: 0,
    top: 0,
    width: 22,
    height: 22,
  },
  fieldLabel: {
    position: 'absolute',
    left: 23,
    top: 0,
    fontSize: 16,
    color: '#000000',
    fontFamily: Fonts.sans,
    lineHeight: 21,
  },
  inputBox: {
    position: 'absolute',
    left: 0,
    top: 32,
    width: 332,
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

  fieldPassword: {
    position: 'absolute',
    left: 15,
    top: 333,
    width: 332,
    height: 67,
  },
  fieldConfirm: {
    position: 'absolute',
    left: 15,
    top: 410,
    width: 332,
    height: 67,
  },
  fieldLabelNoIcon: {
    fontSize: 16,
    color: '#000000',
    fontFamily: Fonts.sans,
    lineHeight: 21,
  },

  // —— 注册按钮（PNG + Pressable）——
  registerBtn: {
    position: 'absolute',
    left: 15,
    top: 507,
    width: 332,
    height: 56,
  },
  registerBtnImage: {
    width: '100%',
    height: '100%',
  },

  // —— 协议 ——
  agreement: {
    position: 'absolute',
    left: 59,
    top: 600,
    width: 235,
    height: 14,
  },
  agreementImage: {
    width: '100%',
    height: '100%',
  },
  // 复选框覆盖层：响应点击 + 勾选时盖住 Group 118.png 的空心 ○，显示实心 ●✓
  checkboxHit: {
    position: 'absolute',
    left: 0,
    top: 0,
    width: 14,
    height: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkboxFilled: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkmark: {
    color: '#FFFFFF',
    fontSize: 9,
    fontWeight: '700',
    lineHeight: 10,
  },
});