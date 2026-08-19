import { StyleSheet, Text, View } from 'react-native';

import { Fonts } from '@/constants/theme';

/**
 * 登录页 —— 极简版：只保留 WELCOME 标题 + 背景。
 * 其他元素（装饰 / 表单 / 按钮 / 协议）已全部移除。
 */
export default function LoginScreen() {
  return (
    <View style={styles.root}>
      {/* WELCOME 标题 */}
      <Text style={styles.welcome}>WELCOME!</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#F4E7D1',
  },
  welcome: {
    position: 'absolute',
    left: 20,
    top: 26,
    width: 224,
    height: 53,
    fontSize: 40,
    fontWeight: '700',
    color: '#000000',
    fontFamily: Fonts.sans,
    letterSpacing: 1,
    lineHeight: 53,
  },
});