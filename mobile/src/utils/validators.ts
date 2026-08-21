/**
 * 共享校验工具:手机号、验证码等正则。
 *
 * 校验时记得先 .trim() 掉首尾空格 — 输入框常会自动加空格。
 */

export const phoneRegex = /^1[3-9]\d{9}$/;
export const codeRegex = /^\d{6}$/;

export const isPhone = (s: string) => phoneRegex.test(s.trim());
export const isCode = (s: string) => codeRegex.test(s.trim());