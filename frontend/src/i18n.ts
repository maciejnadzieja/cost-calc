import { createI18n } from 'vue-i18n';
import en from './en.json';
import pl from './pl.json';

const i18n = createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'en',
  fallbackLocale: 'en',
  messages: {
    en,
    pl,
  },
});

export default i18n;
