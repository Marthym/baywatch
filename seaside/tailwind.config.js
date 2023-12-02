module.exports = {
  content: [
    './src/**/*.html',
    './src/**/*.vue',
    './src/**/*.jsx',
  ],
  theme: {
    extend: {},
  },
  plugins: [
    require('daisyui'),
  ],
  daisyui: {
    themes: [
      {
        'baywatch': {
          'primary': '#ff5555',
          // 'primary-focus': '#ff6363',
          'primary-content': '#ffeee9',
          'secondary': '#e5e7eb',
          // 'secondary-focus': '#f9fafb',
          // 'secondary-content': '#4b5563',
          'accent': '#1ad5ff',
          // 'accent-focus': '#00b4dc',
          // 'accent-content': '#00313c',
          'neutral': '#374151',
          // 'neutral-focus': '#27272a',
          // 'neutral-content': '#a1a1aa',
          'base-100': '#4B5563',
          'base-200': '#1B263E',
          'base-300': '#111827',
          'base-content': '#d1d5db',
          'info': '#2094f3',
          'success': '#009485',
          'warning': '#ff9900',
          'error': '#d92b3a',
        },
      },
    ],
  },
}
