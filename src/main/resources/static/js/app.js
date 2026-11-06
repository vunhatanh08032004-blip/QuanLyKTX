// Bật/tắt sidebar trên màn hình nhỏ
(function () {
  var body = document.body;
  var toggle = document.getElementById('menuToggle');
  var backdrop = document.getElementById('sidebarBackdrop');
  if (toggle) toggle.addEventListener('click', function () { body.classList.toggle('sidebar-open'); });
  if (backdrop) backdrop.addEventListener('click', function () { body.classList.remove('sidebar-open'); });
})();
