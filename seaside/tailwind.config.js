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
          'primary': '#10b981',
          'primary-focus': '#bbf7d0',
          'primary-content': '#e5e7eb',
          'secondary': '#f000b8',
          'secondary-focus': '#bd0091',
          'secondary-content': '#ffffff',
          'accent': '#37cdbe',
          'accent-focus': '#2aa79b',
          'accent-content': '#ffffff',
          'neutral': '#374151',
          'neutral-focus': '#27272a',
          'neutral-content': '#a1a1aa',
          'base-100': '#4B5563',
          'base-200': '#111827',
          'base-300': '#d1d5db',
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
