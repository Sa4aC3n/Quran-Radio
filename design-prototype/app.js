const iconPaths = {
 search:'<circle cx="11" cy="11" r="7"></circle><path d="m20 20-4-4"></path>',
 settings:'<circle cx="12" cy="12" r="3"></circle><path d="m19.4 15 .1.1 1.4 1.1-1.4 2.4-2.4-1.1-1.4-.1-.1.1-.1 1.1-1.4-2.4-2.4-1.4 1.1-.1.1-.1-.1-1.4-1.1-2.4 2.4 1.1 1.4.1.1-.1.1-1.1 1.4 2.4 2.4 1.4-1.1.1-.1z" transform="translate(-1 -1) scale(1.08)"></path><circle cx="12" cy="12" r="8.5"></circle>',
 radio:'<rect x="3" y="7" width="18" height="14" rx="2"></rect><path d="m7 7 10-4M7 11h5M7 15h2M15 12h3v4h-3z"></path>',
 play:'<path d="m8 5 12 7-12 7z"></path>',
 clock:'<circle cx="12" cy="12" r="9"></circle><path d="M12 7v5l4 2"></path>',
 bell:'<path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4"></path>',
 'book-open':'<path d="M12 7v14M3 5.5A2.5 2.5 0 0 1 5.5 3H12v18H5.5A2.5 2.5 0 0 0 3 23zM21 5.5A2.5 2.5 0 0 0 18.5 3H12v18h6.5a2.5 2.5 0 0 1 2.5 2z"></path>',
 globe:'<circle cx="12" cy="12" r="10"></circle><path d="M2 12h20M12 2a15 15 0 0 1 0 20M12 2a15 15 0 0 0 0 20"></path>',
 user:'<circle cx="12" cy="7" r="4"></circle><path d="M4 21v-2a8 8 0 0 1 16 0v2"></path>',
 'scale':'<path d="M12 3v18M5 7h14M7 7l-4 8h8L7 7zM17 7l-4 8h8L17 7zM8 21h8"></path>',
 sunrise:'<path d="M3 18h18M5 14a7 7 0 0 1 14 0M12 2v3M4.9 5l2.1 2.1M19.1 5 17 7.1M2 22h20"></path>',
 hands:'<path d="M8 11V5a1.5 1.5 0 0 1 3 0v5-7a1.5 1.5 0 0 1 3 0v7-5a1.5 1.5 0 0 1 3 0v7l1-2a1.7 1.7 0 0 1 3 1l-2 7a5 5 0 0 1-5 4h-2a5 5 0 0 1-4-2l-4-5a1.8 1.8 0 0 1 2.5-2.5z"></path>',
 compass:'<circle cx="12" cy="12" r="10"></circle><path d="m16 8-2 6-6 2 2-6z"></path>',
 languages:'<path d="m5 8 6 0M8 5v3c0 4-2 6-5 8M5 12c1 2 3 3 5 4M14 19l4-10 4 10M15.5 16h5"></path>',
 'moon-star':'<path d="M20.9 13A8.5 8.5 0 0 1 11 3.1 8.5 8.5 0 1 0 20.9 13z"></path><path d="m17 3 1 2 2 1-2 1-1 2-1-2-2-1 2-1z"></path>',
 landmark:'<path d="M3 21h18M5 21V10M9 21V10M15 21V10M19 21V10M2 10h20L12 3z"></path>',
 timer:'<circle cx="12" cy="13" r="8"></circle><path d="M12 9v4l3 2M9 2h6M12 2v3"></path>',
 tv:'<rect x="3" y="6" width="18" height="14" rx="2"></rect><path d="m8 2 4 4 4-4M10 11l5 3-5 3z"></path>',
 home:'<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7h-6v7H4a1 1 0 0 1-1-1z"></path>',
 heart:'<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8z"></path>',
 'more-horizontal':'<circle cx="5" cy="12" r="1"></circle><circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle>'
};
document.querySelectorAll('[data-icon]').forEach(el => {
  const name = el.dataset.icon;
  el.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true">' + (iconPaths[name] || iconPaths['book-open']) + '</svg>';
});
const categories = [
  ['globe','إذاعات الدول','من مختلف دول العالم'],
  ['user','إذاعات القراء','أشهر القراء'],
  ['radio','إذاعات رسمية','هيئات الإذاعة'],
  ['book-open','تلاوات خاشعة','تأخذك إلى عالم آخر'],
  ['book-open','تفسير القرآن','تأملات مميزة'],
  ['languages','ترجمات المعاني','بأكثر من لغة'],
  ['landmark','السنة النبوية','شرح وبيان'],
  ['compass','السيرة والقصص','عبر ودروس'],
  ['scale','الفتاوى والأحكام','من علماء موثوقين'],
  ['sunrise','أذكار الصبح والمساء','راحة وطمأنينة'],
  ['hands','الرقية الشرعية','للاطمئنان والشفاء'],
  ['compass','القبلة','اتجاهك في أي مكان']
];
const grid = document.getElementById('categoryGrid');
function renderCategories(filter='') {
  const matches = categories.filter(item => item.join(' ').includes(filter.trim()));
  grid.innerHTML = matches.length ? matches.map(([icon,title,desc]) => `
    <button class="category-card" type="button" aria-label="${title}">
      <span class="category-icon"><span class="icon" data-icon="${icon}"></span></span>
      <h3>${title}</h3><p>${desc}</p><span class="category-arrow">‹</span>
    </button>`).join('') : '<div class="empty-state">لا توجد أقسام مطابقة لبحثك</div>';
  grid.querySelectorAll('[data-icon]').forEach(el => {el.innerHTML='<svg viewBox="0 0 24 24" aria-hidden="true">'+(iconPaths[el.dataset.icon]||iconPaths['book-open'])+'</svg>';});
}
renderCategories();
document.getElementById('searchInput').addEventListener('input', e => renderCategories(e.target.value));
const playButton = document.getElementById('playButton');
playButton.addEventListener('click', () => {
  const label = document.getElementById('liveLabel');
  const isPlaying = playButton.dataset.playing === 'true';
  playButton.dataset.playing = String(!isPlaying);
  label.textContent = isPlaying ? 'جاهز للاستماع' : 'معاينة الواجهة فقط';
  playButton.setAttribute('aria-label', isPlaying ? 'تشغيل تجريبي' : 'إيقاف المعاينة');
  playButton.innerHTML = '<span class="icon" data-icon="'+(isPlaying?'play':'radio')+'"></span>';
  const icon = playButton.querySelector('[data-icon]');
  icon.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true">'+(iconPaths[icon.dataset.icon]||iconPaths.play)+'</svg>';
});
document.querySelectorAll('.quick-item,.nav-item,.outline-button,.text-button,.category-card').forEach(button => {
  button.addEventListener('click', () => {
    if (button.classList.contains('nav-item')) {
      document.querySelectorAll('.nav-item').forEach(item=>item.classList.remove('active'));
      button.classList.add('active');
    }
  });
});
