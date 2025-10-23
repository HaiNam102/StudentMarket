// /static/js/favorite.js (updated toast system)
document.addEventListener("DOMContentLoaded", function () {
  // ===== CSRF =====
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || "";
  const csrfHeader =
    document.querySelector('meta[name="_csrf_header"]')?.content ||
    "X-CSRF-TOKEN";

  // ===== POPUP NOTIFICATIONS (Top-right, progress bar) =====
  function ensureToastRoot() {
    let root = document.getElementById("toast-root");
    if (!root) {
      root = document.createElement("div");
      root.id = "toast-root";
      root.className = "fixed top-16 right-6 z-[9999] space-y-3";
      document.body.appendChild(root);
    }
    return root;
  }

  function closeToast(el) {
    const t = el?.closest ? el.closest(".toast") : el;
    if (!t) return;
    t.classList.add("opacity-0", "translate-y-2");
    setTimeout(() => t.remove(), 250);
  }

  function makeIconSVG(kind) {
    const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("fill", "none");
    svg.setAttribute("stroke", "currentColor");
    svg.classList.add(
      "w-5",
      "h-5",
      "mt-0.5",
      kind === "success" ? "text-green-600" : "text-red-600"
    );

    const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path.setAttribute("stroke-linecap", "round");
    path.setAttribute("stroke-linejoin", "round");
    path.setAttribute("stroke-width", "2");

    if (kind === "success") {
      path.setAttribute("d", "M5 13l4 4L19 7");
    } else {
      path.setAttribute(
        "d",
        "M12 9v4m0 4h.01M5.07 19h13.86A2 2 0 0021 17.13L13.93 4.5a2 2 0 00-3.86 0L3 17.13A2 2 0 005.07 19z"
      );
    }
    svg.appendChild(path);
    return svg;
  }

  function createToast({ message, kind = "success", actionText, actionHref }) {
    const root = ensureToastRoot();

    const wrapper = document.createElement("div");
    wrapper.setAttribute("role", "alert");
    wrapper.setAttribute("aria-live", "assertive");
    wrapper.className = `toast opacity-0 translate-y-2 transition-all duration-300 bg-white border rounded-xl shadow-lg overflow-hidden max-w-sm ${
      kind === "success" ? "border-green-300" : "border-red-300"
    }`;

    const row = document.createElement("div");
    row.className = "flex items-start gap-3 px-4 py-3";

    const icon = makeIconSVG(kind);

    const content = document.createElement("div");
    content.className = "flex-1";

    const title = document.createElement("p");
    title.className = `font-semibold ${
      kind === "success" ? "text-green-700" : "text-red-700"
    }`;
    title.textContent = kind === "success" ? "Thành công" : "Cần đăng nhập";

    const desc = document.createElement("p");
    desc.className = "text-sm text-gray-700";
    desc.textContent = message || "";

    content.appendChild(title);
    content.appendChild(desc);

    const rightSide = document.createElement("div");
    rightSide.className = "flex items-center gap-2";

    if (actionText && actionHref) {
      const a = document.createElement("a");
      a.href = actionHref;
      a.textContent = actionText;
      a.className =
        "px-2 py-1 text-xs font-semibold rounded-md border hover:bg-gray-50";
      rightSide.appendChild(a);
    }

    const closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "text-gray-400 hover:text-gray-600";
    closeBtn.textContent = "✕";
    closeBtn.addEventListener("click", () => closeToast(wrapper));
    rightSide.appendChild(closeBtn);

    row.appendChild(icon);
    row.appendChild(content);
    row.appendChild(rightSide);

    const progress = document.createElement("div");
    progress.className = `${
      kind === "success" ? "bg-green-500" : "bg-red-500"
    } h-1 w-0 progress`;

    wrapper.appendChild(row);
    wrapper.appendChild(progress);
    root.appendChild(wrapper);

    // Animate in & progress bar
    requestAnimationFrame(() => {
      wrapper.classList.remove("opacity-0", "translate-y-2");
      progress.style.transition = "width 4s linear";
      requestAnimationFrame(() => (progress.style.width = "100%"));
    });

    // Auto hide after ~4.2s
    const auto = setTimeout(() => closeToast(wrapper), 4200);

    // Return a small API if needed
    return {
      el: wrapper,
      close: () => {
        clearTimeout(auto);
        closeToast(wrapper);
      },
    };
  }

  // drop-in replacements for old helpers
  function showToast(message, isSuccess = true) {
    createToast({ message, kind: isSuccess ? "success" : "error" });
  }

  function showActionToast(message, actionText, actionHref, isSuccess = true) {
    createToast({
      message,
      kind: isSuccess ? "success" : "error",
      actionText,
      actionHref,
    });
  }

  // ===== Helper set icon heart =====
  function setHeartIconState(icon, isFavorite) {
    if (!icon) return;
    icon.classList.remove("fa-regular", "fa-solid");
    if (isFavorite) {
      icon.classList.add("fa-solid");
      icon.style.setProperty("color", "#ef4444", "important");
    } else {
      icon.classList.add("fa-regular");
      icon.style.setProperty("color", "#6b7280", "important");
    }
  }

  // ===== Call API =====
  async function api(method, url, body) {
    const opts = {
      method,
      headers: {
        Accept: "application/json",
        [csrfHeader]: csrfToken,
      },
    };
    if (method !== "GET") {
      opts.headers["Content-Type"] = "application/x-www-form-urlencoded";
      opts.body = body;
    }
    const res = await fetch(url, opts);
    if (!res.ok) {
      try {
        const j = await res.json();
        throw new Error(j.message || `HTTP ${res.status}`);
      } catch {
        throw new Error(`HTTP ${res.status}`);
      }
    }
    return res.json();
  }

  // ====== TOGGLE FAVORITE (Optimistic UI) ======
  window.toggleFavorite = async function (btn, productId) {
    const icon =
      btn.querySelector("i.fa-heart") ||
      btn.querySelector(".fa-heart") ||
      btn.querySelector("i");
    let resolvedId = productId;
    if (!resolvedId) {
      resolvedId = btn.dataset ? btn.dataset.productId : undefined;
    }
    if (!resolvedId) {
      const prodEl = btn.closest && btn.closest(".product");
      if (prodEl) {
        resolvedId =
          prodEl.getAttribute("data-product-id") ||
          prodEl.getAttribute("data-id") ||
          prodEl.dataset?.productId;
      }
    }

    if (!icon) {
      showToast("Không tìm thấy icon trái tim để thao tác.", false);
      return;
    }

    if (!resolvedId) {
      showToast("Bạn cần đăng nhập để thêm vào yêu thích!", false);
      return;
    }

    productId = resolvedId;

    const wasFavorite = icon.classList.contains("fa-solid");

    // Optimistic: đổi icon ngay
    setHeartIconState(icon, !wasFavorite);

    // Disable trong lúc gọi API
    btn.disabled = true;
    const oldCursor = btn.style.cursor;
    btn.style.cursor = "wait";

    try {
      const data = await api(
        "POST",
        "/api/favorites/toggle",
        `productId=${encodeURIComponent(productId)}`
      );

      if (data.success) {
        setHeartIconState(icon, !!data.isFavorite);

        if (data.isFavorite) {
          showToast("Đã thêm vào yêu thích!");
        } else {
          showToast("Đã xóa khỏi yêu thích!");

          if (window.location.pathname.includes("/tongquan")) {
            const card = btn.closest(".product");
            if (card) {
              const style = getComputedStyle(card);
              const startHeight = card.offsetHeight;
              const startMarginTop = parseFloat(style.marginTop) || 0;
              const startMarginBottom = parseFloat(style.marginBottom) || 0;

              card.style.height = startHeight + "px";
              card.style.marginTop = startMarginTop + "px";
              card.style.marginBottom = startMarginBottom + "px";
              card.style.overflow = "hidden";
              card.style.transition =
                "height 250ms ease, margin 250ms ease, opacity 200ms ease, transform 200ms ease";

              card.getBoundingClientRect();

              card.style.height = "0px";
              card.style.marginTop = "0px";
              card.style.marginBottom = "0px";
              card.style.opacity = "0";
              card.style.transform = "scale(0.98)";

              if (typeof window.refillAfterRemove === "function") {
                window.refillAfterRemove();
              }

              const handleDone = () => {
                card.removeEventListener("transitionend", handleDone);
                card.remove();
                if (typeof window.refillAfterRemove === "function") {
                  window.refillAfterRemove();
                }
              };
              card.addEventListener("transitionend", handleDone);
            }
          }
        }

        updateFavoriteDropdown();
        updateFavoriteCount();
      } else {
        setHeartIconState(icon, wasFavorite);
        const msg = data.message || "Có lỗi xảy ra!";
        showToast(msg, false);
      }
    } catch (err) {
      setHeartIconState(icon, wasFavorite);
      const message = err.message || "Lỗi khi xử lý yêu thích!";
      showToast(message, false);
    } finally {
      btn.disabled = false;
      btn.style.cursor = oldCursor;
    }
  };

  // ===== Dropdown yêu thích trong header =====
  async function fetchDropdownData() {
    const res = await fetch("/api/favorites/dropdown", {
      headers: { Accept: "application/json" },
    });
    if (!res.ok) throw new Error("Failed to load favorite products.");
    return res.json().catch(() => ({}));
  }

  function formatPrice(price) {
    if (price == null) return "Liên hệ";
    try {
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
      }).format(price);
    } catch {
      return price + "đ";
    }
  }

  async function updateFavoriteDropdown() {
    const favDropdown = document.getElementById("favDropdown");
    if (!favDropdown) return;

    const headerEl = favDropdown.querySelector(
      ".px-4.py-2.font-semibold.border-b"
    );
    const dropdownContent =
      headerEl?.nextElementSibling ||
      (() => {
        const c = document.createElement("div");
        favDropdown.appendChild(c);
        return c;
      })();

    dropdownContent.innerHTML =
      '<div class="px-4 py-2 text-sm text-gray-500 text-center">Đang tải...</div>';

    try {
      const data = await fetchDropdownData();
      const products = Array.isArray(data) ? data : data.favorites || [];

      dropdownContent.innerHTML = "";

      if (products.length > 0) {
        const container = document.createElement("div");
        container.className = "px-2 py-2";

        const list = document.createElement("div");
        list.className = "max-h-64 overflow-y-auto space-y-1 thin-scroll-red";

        products.forEach((p) => {
          const item = document.createElement("a");
          item.href = `/chitietbaidang/${encodeURIComponent(p.productId)}`;
          item.className =
            "flex items-center gap-3 px-3 py-2 hover:bg-gray-100 rounded-md border";
          item.innerHTML = `
            <img src="${p.imageUrl || "/image/card/image.png"}" alt="${
            p.name
          }" class="w-12 h-12 rounded object-cover flex-shrink-0">
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">${
                p.name
              }</p>
              <p class="text-xs text-gray-500 mt-1">${formatPrice(p.price)}</p>
            </div>`;
          list.appendChild(item);
        });

        const footer = document.createElement("div");
        footer.className = "mt-2 text-center";
        const viewAll = document.createElement("a");
        viewAll.href = "/tongquan";
        viewAll.className =
          "inline-block px-3 py-1 text-red-600 font-semibold hover:bg-gray-100 rounded-md";
        viewAll.textContent = "Xem tất cả sản phẩm yêu thích";
        footer.appendChild(viewAll);

        container.appendChild(list);
        container.appendChild(footer);
        dropdownContent.appendChild(container);
      } else {
        dropdownContent.innerHTML =
          '<p class="px-4 py-2 text-sm text-gray-500 text-center">Chưa có sản phẩm yêu thích.</p>';
      }
    } catch (e) {
      dropdownContent.innerHTML =
        '<p class="px-4 py-2 text-sm text-red-500 text-center">Lỗi tải dữ liệu.</p>';
    }
  }

  // ===== Badge đếm yêu thích =====
  async function updateFavoriteCount() {
    const badge = document.getElementById("favoriteCount");
    if (!badge) return;

    try {
      const data = await fetchDropdownData();
      let count = 0;
      if (data == null) {
        count = 0;
      } else if (typeof data === "number") {
        count = data;
      } else if (typeof data.total === "number") {
        count = data.total;
      } else if (Array.isArray(data)) {
        count = data.length;
      } else if (Array.isArray(data.favorites)) {
        count = data.favorites.length;
      } else {
        count = 0;
      }
      if (count > 0) {
        badge.textContent = count;
        badge.classList.remove("hidden");
      } else {
        badge.classList.add("hidden");
      }
    } catch {
      badge.classList.add("hidden");
    }
  }

  // ===== Khởi tạo icon tim theo trạng thái =====
  window.initializeFavoriteStates = function () {
    const buttons = document.querySelectorAll("button[data-product-id]");
    if (!buttons.length) {
      const altButtons = document.querySelectorAll(
        'button[onclick*="toggleFavorite"]'
      );
      altButtons.forEach((b) => {
        const m = b
          .getAttribute("onclick")
          ?.match(/toggleFavorite\([^,]+,\s*(\d+)\)/);
        if (m?.[1]) b.setAttribute("data-product-id", m[1]);
      });
    }

    const all = document.querySelectorAll("button[data-product-id]");
    if (!all.length) return;

    const batchSize = 5;
    const processBatch = async (start) => {
      const end = Math.min(start + batchSize, all.length);
      const batch = Array.from(all).slice(start, end);

      await Promise.all(
        batch.map(async (btn) => {
          const productId = btn.dataset.productId;
          const icon =
            btn.querySelector("i.fa-heart") ||
            btn.querySelector(".fa-heart") ||
            btn.querySelector("i");
          if (!icon || !productId) return;

          const isRed =
            icon.classList.contains("fa-solid") &&
            (icon.style.color === "rgb(239, 68, 68)" ||
              icon.style.color === "#ef4444");
          if (isRed) return;

          try {
            const res = await fetch(
              `/api/favorites/check?productId=${productId}`,
              {
                headers: { Accept: "application/json" },
              }
            );
            const data = await res.json().catch(() => ({}));
            setHeartIconState(icon, data.isFavorite === true);
          } catch {
            setHeartIconState(icon, false);
          }
        })
      );

      if (end < all.length) setTimeout(() => processBatch(end), 100);
    };

    processBatch(0);
  };

  // ===== Init tổng =====
  function initializeAll() {
    updateFavoriteDropdown();
    updateFavoriteCount();
    setTimeout(() => window.initializeFavoriteStates(), 400);
  }

  initializeAll();

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () =>
      setTimeout(() => window.initializeFavoriteStates(), 200)
    );
  }
  window.addEventListener("load", () =>
    setTimeout(() => window.initializeFavoriteStates(), 800)
  );
});
