/**
 * 水墨江湖主题：墨黑作字、朱砂点睛、暗金勾边、米黄为底。
 * 在 Expo SDK 57 默认 light/dark 模板基础上叠加《机巧江湖》色系。
 * 原 token 名称保留以保证 themed/themed-view 组件继续工作；
 * 新增 accent / accentSoft / cinnabar / bamboo / border 5 个 token。
 * There are many other ways to style your app; for now we keep the original token names
 * so themed components keep working, and add a few that are specific to this project.
 */

import '@/global.css';

import { Platform } from 'react-native';

export const Colors = {
  light: {
    text: '#1A1A2E',
    background: '#F5EFE0',
    backgroundElement: '#FFFFFF',
    backgroundSelected: '#E8D9A8',
    textSecondary: '#5A5A6A',
    accent: '#C9A961',
    accentSoft: '#E8D9A8',
    cinnabar: '#B8323A',
    bamboo: '#5A7A5A',
    border: '#D8D2C2',
  },
  dark: {
    text: '#F5EFE0',
    background: '#0F0F1E',
    backgroundElement: '#1A1A2E',
    backgroundSelected: '#2A2A3E',
    textSecondary: '#A0A0B0',
    accent: '#E0C075',
    accentSoft: '#3A3520',
    cinnabar: '#E0535E',
    bamboo: '#7AA07A',
    border: '#2A2A3E',
  },
} as const;

export type ThemeColor = keyof (typeof Colors)['light'] | keyof (typeof Colors)['dark'];

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: 'system-ui',
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: 'ui-serif',
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: 'ui-rounded',
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: 'var(--font-display)',
    serif: 'var(--font-serif)',
    rounded: 'var(--font-rounded)',
    mono: 'var(--font-mono)',
  },
});

export const Spacing = {
  half: 2,
  one: 4,
  two: 8,
  three: 16,
  four: 24,
  five: 32,
  six: 64,
} as const;

export const BottomTabInset = Platform.select({ ios: 50, android: 80 }) ?? 0;
export const MaxContentWidth = 800;
