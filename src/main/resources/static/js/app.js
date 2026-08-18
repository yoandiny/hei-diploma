document.addEventListener("DOMContentLoaded", () => {
  const addMappingBtn = document.getElementById("add-mapping-row");
  const mappingContainer = document.getElementById("mapping-rows");

  if (addMappingBtn && mappingContainer) {
    addMappingBtn.addEventListener("click", () => {
      const row = mappingContainer.querySelector(".mapping-row");
      if (!row) return;
      const clone = row.cloneNode(true);
      clone.querySelectorAll("select").forEach((select) => {
        select.selectedIndex = 0;
      });
      mappingContainer.appendChild(clone);
    });
  }

  document.querySelectorAll("[data-confirm]").forEach((el) => {
    el.addEventListener("click", (event) => {
      const message = el.getAttribute("data-confirm");
      if (message && !window.confirm(message)) {
        event.preventDefault();
      }
    });
  });

  document.querySelectorAll("form[data-sending]").forEach((form) => {
    form.addEventListener("submit", () => {
      const button = form.querySelector("button[type='submit']");
      if (!button || button.disabled || button.classList.contains("is-sending")) {
        return;
      }
      button.classList.add("is-sending");
      button.setAttribute("aria-busy", "true");
      const icon = button.querySelector("i");
      if (icon) {
        icon.className = "fa-solid fa-spinner fa-spin";
      }
      const label = button.querySelector(".btn-label");
      if (label) {
        label.textContent = button.getAttribute("data-sending-label") || "Envoi en cours…";
      }
    });
  });
});
