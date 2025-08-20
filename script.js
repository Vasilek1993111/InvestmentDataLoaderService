(function () {
  const storageKey = "preferred-theme";

  function applyStoredTheme() {
    try {
      const stored = localStorage.getItem(storageKey);
      if (stored === "dark" || stored === "light") {
        document.documentElement.setAttribute("data-theme", stored);
      }
    } catch (err) {}
  }

  applyStoredTheme();
})();

$(function () {
  const $doc = $(document);
  const $win = $(window);
  const $body = $(document.body);
  const $navList = $(".nav-list");
  const $hamburger = $(".hamburger");
  const $backToTop = $("#backToTop");
  const $themeToggle = $("#themeToggle");
  const $year = $("#year");

  $year.text(new Date().getFullYear());

  $hamburger.on("click", function () {
    const expanded = $(this).attr("aria-expanded") === "true";
    $(this).attr("aria-expanded", String(!expanded));
    $body.toggleClass("nav-open");
  });

  $("a[href^='#']").on("click", function (e) {
    const targetId = $(this).attr("href");
    const $target = $(targetId);
    if ($target.length) {
      e.preventDefault();
      const headerHeight = $(".site-header").outerHeight() || 0;
      const targetTop = $target.offset().top - headerHeight - 6;
      $("html, body").animate({ scrollTop: targetTop }, 450);
      if ($body.hasClass("nav-open")) {
        $body.removeClass("nav-open");
        $hamburger.attr("aria-expanded", "false");
      }
    }
  });

  $win.on("scroll", function () {
    const show = $win.scrollTop() > 420;
    $backToTop.toggle(show);
  });

  $backToTop.on("click", function () {
    $("html, body").animate({ scrollTop: 0 }, 450);
  });

  $themeToggle.on("click", function () {
    const current = document.documentElement.getAttribute("data-theme") || "light";
    const next = current === "light" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", next);
    try { localStorage.setItem("preferred-theme", next); } catch (err) {}
  });

  $(".gallery-item img").on("click", function () {
    const srcFull = $(this).attr("data-full") || $(this).attr("src");
    const alt = $(this).attr("alt") || "";
    $("#modalImage").attr({ src: srcFull, alt });
    $("#imageModal").attr("aria-hidden", "false");
  });

  $("#imageModal").on("click", "[data-close]", function () {
    $("#imageModal").attr("aria-hidden", "true");
    $("#modalImage").attr({ src: "", alt: "" });
  });

  $doc.on("keydown", function (e) {
    if (e.key === "Escape") {
      $("#imageModal").attr("aria-hidden", "true");
      $("#modalImage").attr({ src: "", alt: "" });
    }
  });

  function validateEmail(email) {
    return /.+@.+\..+/.test(String(email).toLowerCase());
  }

  $("#contactForm").on("submit", function (e) {
    e.preventDefault();
    const $form = $(this);
    const name = $form.find("#name").val().trim();
    const email = $form.find("#email").val().trim();
    const message = $form.find("#message").val().trim();

    let valid = true;
    $form.find(".error").text("");

    if (!name) {
      $form.find(".error[data-for='name']").text("Введите имя");
      valid = false;
    }
    if (!email || !validateEmail(email)) {
      $form.find(".error[data-for='email']").text("Укажите корректный email");
      valid = false;
    }
    if (!message || message.length < 5) {
      $form.find(".error[data-for='message']").text("Минимум 5 символов");
      valid = false;
    }

    if (!valid) return;

    const $btn = $form.find("button[type='submit']");
    $btn.prop("disabled", true).text("Отправка...");

    setTimeout(function () {
      $btn.prop("disabled", false).text("Отправить");
      $form[0].reset();
      showToast("Сообщение отправлено (демо)");
    }, 800);
  });

  function showToast(text) {
    const $toast = $("#formToast");
    $toast.text(text).fadeIn(160);
    setTimeout(function () {
      $toast.fadeOut(200);
    }, 2200);
  }
});

