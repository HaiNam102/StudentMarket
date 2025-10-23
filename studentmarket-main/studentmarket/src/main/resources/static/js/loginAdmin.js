document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("loginForm");
  const overlay = document.getElementById("loadingOverlay");
  const btn = document.getElementById("loginBtn");
  const btnText = document.getElementById("btnText");
  const btnSpin = document.getElementById("btnSpin");

  form.addEventListener("submit", (e) => {
    e.preventDefault(); // chặn submit ngay lập tức

    btn.disabled = true;
    btnText.textContent = "Đang xử lý...";
    btnSpin.style.display = "inline-block";
    overlay.style.display = "flex";

    // Đợi 2 giây rồi mới submit form
    setTimeout(() => {
      form.submit();
    }, 500);
  });
});
