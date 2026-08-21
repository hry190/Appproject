/**
 * 登录 / 注册 / 忘记密码页共用的颜色常量。
 *
 * 这些颜色来自 Figma 节点 413:3271 / 413:3377 的实测,与
 * constants/theme.ts 的设计系统 token **有意的不同**(Figma 是
 * 江糊项目特有的米黄/橄榄主题)。
 *
 * 保留为本地常量而不是合并到 theme.ts 是为了:
 * 1. 让其他页面(tab / 注册等)不被本主题影响
 * 2. 改色时两页同步(都在这一文件里改)
 */
export const BG_CREAM = '#F5E8D4';
export const INPUT_BORDER = '#DCCCA1';
export const LINK_OLIVE = '#A7AD8E';
export const PLACEHOLDER = '#939393';
export const TEXT_DARK = '#000000';
export const ACTION_GRAY = '#888888'; // "获取验证码"按钮文字 / 倒计时变灰
export const ERROR_RED = '#B8323A'; // 输入校验错误提示
export const DIVIDER_GRAY = '#C0C0C0'; // 输入框内分割线