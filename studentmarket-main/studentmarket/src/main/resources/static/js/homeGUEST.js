// <!-- JS Header -->
const overlay = document.getElementById("overlay");
const dropdowns = {
  menuBtn: "menuDropdown",
  favBtn: "favDropdown",
  notiBtn: "notiDropdown",
  avatarBtn: "dropdownMenu",
};

// Helper: an toàn khi get element
function byId(id) {
  const el = document.getElementById(id);
  return el || null;
}

// Toggle dropdown theo nút bấm
Object.entries(dropdowns).forEach(([btnId, menuId]) => {
  const btn = byId(btnId);
  const menu = byId(menuId);
  if (!btn || !menu) return;

  btn.addEventListener("click", (e) => {
    e.stopPropagation();

    // kiểm tra dropdown hiện tại đang mở?
    const isOpen = !menu.classList.contains("hidden");

    // đóng tất cả dropdown
    Object.values(dropdowns).forEach((id) => {
      const m = byId(id);
      if (m) m.classList.add("hidden");
    });

    // bật/tắt dropdown hiện tại + overlay
    if (isOpen) {
      menu.classList.add("hidden");
      if (overlay) overlay.classList.add("hidden");
    } else {
      menu.classList.remove("hidden");
      if (overlay) overlay.classList.remove("hidden");
    }
  });
});

// Bấm overlay => đóng tất cả
if (overlay) {
  overlay.addEventListener("click", () => {
    overlay.classList.add("hidden");
    Object.values(dropdowns).forEach((id) => {
      const m = byId(id);
      if (m) m.classList.add("hidden");
    });
  });
}

// Bấm ra ngoài => đóng tất cả
document.addEventListener("click", (e) => {
  const clickedOnAnyBtn = Object.keys(dropdowns).some((btnId) => {
    const b = byId(btnId);
    return b && b.contains(e.target);
  });

  if (!clickedOnAnyBtn) {
    if (overlay) overlay.classList.add("hidden");
    Object.values(dropdowns).forEach((id) => {
      const m = byId(id);
      if (m) m.classList.add("hidden");
    });
  }
});

// ================== Trái tim yêu thích ==================
function toggleFavorite(btn) {
  const icon = btn.querySelector("i");
  if (!icon) return;
  icon.classList.toggle("fa-regular");
  icon.classList.toggle("fa-solid");
}
window.toggleFavorite = toggleFavorite; // nếu gọi từ HTML inline

// ================== POPUP (hiện thông báo) ==================
function closeToast(btnOrObj) {
  const t = btnOrObj.closest ? btnOrObj.closest(".toast") : btnOrObj;
  if (!t) return;
  t.classList.add("opacity-0", "translate-y-2");
  setTimeout(() => t.remove(), 250);
}

// animate all toasts on page
window.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".toast").forEach((t) => {
    requestAnimationFrame(() => {
      t.classList.remove("opacity-0", "translate-y-2");
      const bar = t.querySelector(".progress");
      if (bar) {
        bar.style.transition = "width 4s linear";
        requestAnimationFrame(() => (bar.style.width = "100%"));
      }
      setTimeout(() => closeToast(t), 4200);
    });
  });
});

// JS banner
let currentIndex = 0;
const banners = document.querySelectorAll(".banner");
const dots = document.querySelectorAll(".dot");
const prevBtn = document.querySelector(".prev");
const nextBtn = document.querySelector(".next");

function showSlide(index) {
  if (!banners.length) return;
  banners.forEach((banner, i) => {
    banner.classList.toggle("active", i === index);
    dots[i]?.classList.toggle("active", i === index);
  });
}
function nextSlide() {
  if (!banners.length) return;
  currentIndex = (currentIndex + 1) % banners.length;
  showSlide(currentIndex);
}
function prevSlide() {
  if (!banners.length) return;
  currentIndex = (currentIndex - 1 + banners.length) % banners.length;
  showSlide(currentIndex);
}

if (banners.length) {
  setInterval(nextSlide, 5000);
  nextBtn?.addEventListener("click", nextSlide);
  prevBtn?.addEventListener("click", prevSlide);
  dots.forEach((dot, i) =>
    dot.addEventListener("click", () => {
      currentIndex = i;
      showSlide(currentIndex);
    })
  );
}

// ================== Sản phẩm ==================
let visibleCount = 8;
const step = 4;
const grid = document.getElementById("productGrid");
const loadMoreBtn = document.getElementById("loadMoreBtn");

// Lưu snapshot ban đầu để lọc/sắp xếp luôn từ nguồn gốc
const initialCards = grid
  ? Array.from(grid.querySelectorAll("article.product"))
  : [];

// Tìm thông báo "rỗng" từ server nếu có (th:if)
const serverEmptyMsg = (() => {
  if (!grid) return null;
  // tìm phần tử con có text "Chưa có sản phẩm nào." và có class text-gray-500
  return (
    Array.from(grid.children).find(
      (el) =>
        el.id !== "noProducts" &&
        el.tagName === "DIV" &&
        el.textContent?.trim() === "Chưa có sản phẩm nào." &&
        el.classList?.contains("text-gray-500")
    ) || null
  );
})();

// Tạo/chuẩn bị thông báo rỗng dùng cho client-side
let noProductsMsg = grid ? document.getElementById("noProducts") : null;
if (grid && !noProductsMsg) {
  noProductsMsg = document.createElement("div");
  noProductsMsg.id = "noProducts";
  noProductsMsg.className =
    "col-span-2 md:col-span-4 text-center text-gray-500 py-10 hidden";
  noProductsMsg.textContent = "Chưa có sản phẩm nào.";
  grid.appendChild(noProductsMsg);
}

// Helper: bật/tắt empty state + ẩn/hiện nút xem thêm
function setEmptyState(isEmpty) {
  if (noProductsMsg) noProductsMsg.classList.toggle("hidden", !isEmpty);
  if (serverEmptyMsg) serverEmptyMsg.classList.toggle("hidden", true); // tránh hiển thị trùng
  if (loadMoreBtn)
    loadMoreBtn.style.display = isEmpty ? "none" : loadMoreBtn.style.display;
}

// Hiển thị theo visibleCount (đếm trực tiếp từ DOM để luôn đúng)
function showProducts() {
  if (!grid) return;
  const items = Array.from(grid.querySelectorAll("article.product"));

  items.forEach((p, i) => {
    p.style.display = i < visibleCount ? "block" : "none";
  });

  if (loadMoreBtn) {
    if (items.length === 0) {
      loadMoreBtn.style.display = "none";
    } else {
      loadMoreBtn.style.display =
        visibleCount >= items.length ? "none" : "inline-block";
    }
  }

  // nếu không còn item nào, đảm bảo empty state đúng
  if (items.length === 0) {
    setEmptyState(true);
  } else if (noProductsMsg) {
    noProductsMsg.classList.add("hidden");
  }
}

loadMoreBtn?.addEventListener("click", () => {
  visibleCount += step;
  showProducts();
});

// Lọc / sắp xếp (gọi từ HTML: onclick="filterProducts('hot')", ...)
function filterProducts(type) {
  if (!grid) return;

  // Luôn dựa trên dữ liệu ban đầu để không bị sort chồng
  let cards = [...initialCards];

  if (type === "hot") {
    cards = cards
      .filter((p) => p.dataset.hot === "true")
      .sort(
        (a, b) =>
          Date.parse(b.dataset.created || 0) -
          Date.parse(a.dataset.created || 0)
      );
  } else if (type === "newest") {
    cards.sort(
      (a, b) =>
        Date.parse(b.dataset.created || 0) - Date.parse(a.dataset.created || 0)
    );
  } else if (type === "asc") {
    cards.sort(
      (a, b) =>
        parseInt(a.dataset.price || "0", 10) -
        parseInt(b.dataset.price || "0", 10)
    );
  } else if (type === "desc") {
    cards.sort(
      (a, b) =>
        parseInt(b.dataset.price || "0", 10) -
        parseInt(a.dataset.price || "0", 10)
    );
  }

  // Xóa toàn bộ article hiện có (không đụng vào các div khác như thông báo rỗng)
  grid.querySelectorAll("article.product").forEach((n) => n.remove());

  if (!cards.length) {
    setEmptyState(true);
    showProducts(); // để ẩn nút Xem thêm nếu còn đang hiện
    return;
  }

  // Có kết quả
  setEmptyState(false);
  const frag = document.createDocumentFragment();
  cards.forEach((c) => {
    c.style.display = ""; // reset display
    frag.appendChild(c);
  });
  grid.appendChild(frag);

  visibleCount = Math.min(8, cards.length);
  showProducts();
}

// Khởi tạo hiển thị ban đầu (giữ thông báo server nếu ban đầu không có sp)
showProducts();

// ================== Đã xem gần đây ==================
(function () {
  const viewport = document.getElementById("recentViewport");
  const clip = document.getElementById("recentClip");
  const wrapper = document.getElementById("recentWrapper");
  if (!wrapper) return;

  const cards = Array.from(wrapper.children);
  const VISIBLE = 4;
  let index = 0;
  let STEP = 0; // card width + gap

  function calc() {
    if (cards.length === 0) return;
    const style = getComputedStyle(wrapper);
    const gap = parseFloat(style.gap || style.columnGap || 0) || 0;
    const cw = cards[0].getBoundingClientRect().width;
    STEP = cw + gap;

    const clipWidth = VISIBLE * cw + (VISIBLE - 1) * gap;
    const containerW = (
      clip?.parentElement ||
      viewport ||
      wrapper.parentElement
    ).clientWidth;

    if (clip) {
      if (clipWidth < containerW) {
        clip.style.width = clipWidth + "px";
        clip.style.marginInline = "auto";
      } else {
        clip.style.width = "100%";
        clip.style.marginInline = "0";
      }
      clip.style.overflow = "hidden";
    } else {
      (viewport || wrapper.parentElement).style.overflow = "hidden";
    }
  }

  function maxIndex() {
    return Math.max(0, cards.length - VISIBLE);
  }
  function apply() {
    wrapper.style.transform = `translateX(${-index * STEP}px)`;
  }
  function updateBtns() {
    const prev = document.querySelector('button[onclick="scrollRecent(-1)"]');
    const next = document.querySelector('button[onclick="scrollRecent(1)"]');
    const atStart = index === 0;
    const atEnd = index === maxIndex();
    if (prev) {
      prev.disabled = atStart;
      prev.classList.toggle("opacity-40", atStart);
      prev.classList.toggle("cursor-not-allowed", atStart);
    }
    if (next) {
      next.disabled = atEnd;
      next.classList.toggle("opacity-40", atEnd);
      next.classList.toggle("cursor-not-allowed", atEnd);
    }
  }

  window.scrollRecent = function (dir) {
    index = Math.min(Math.max(0, index + dir), maxIndex());
    apply();
    updateBtns();
  };

  calc();
  apply();
  updateBtns();
  window.addEventListener("resize", () => {
    const keep = index;
    wrapper.style.transform = "translateX(0)";
    calc();
    index = Math.min(keep, maxIndex());
    apply();
    updateBtns();
  });
})();
