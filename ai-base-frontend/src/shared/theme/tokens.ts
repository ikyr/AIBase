// src/shared/theme/tokens.ts
import type { ThemeConfig } from 'antd';

export const themeTokens: ThemeConfig = {
  token: {
    colorPrimary: '#597ef7',
    colorSuccess: '#16a34a',
    colorWarning: '#d97706',
    colorError: '#eb5757',
    colorInfo: '#597ef7',
    borderRadius: 8,
    borderRadiusLG: 12,
    borderRadiusSM: 6,
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    fontSize: 13,
    fontSizeHeading1: 20,
    fontSizeHeading2: 18,
    fontSizeHeading3: 16,
    fontSizeHeading4: 14,
    fontSizeLG: 14,
    fontSizeSM: 12,
    lineHeight: 1.5,
    controlHeight: 36,
    paddingContentHorizontal: 20,
    paddingContentVertical: 16,
    colorBgContainer: '#ffffff',
    colorBgLayout: '#f5f5f5',
  },
  components: {
    Button: {
      borderRadius: 8,
      borderRadiusLG: 10,
      borderRadiusSM: 6,
      controlHeight: 36,
      controlHeightLG: 42,
      controlHeightSM: 30,
      fontWeight: 500,
    },
    Card: {
      borderRadiusLG: 12,
      paddingLG: 18,
    },
    Input: {
      borderRadius: 10,
      borderRadiusLG: 12,
      borderRadiusSM: 8,
      controlHeight: 36,
      controlHeightLG: 42,
      controlHeightSM: 30,
    },
    Tag: {
      borderRadiusSM: 6,
    },
    Modal: {
      borderRadiusLG: 16,
    },
    Menu: {
      itemBorderRadius: 8,
      itemHeight: 36,
      itemMarginInline: 8,
    },
    Layout: {
      headerHeight: 48,
    },
  },
};
