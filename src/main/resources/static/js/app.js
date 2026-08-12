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
});
