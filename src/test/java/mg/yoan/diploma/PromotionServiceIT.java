package mg.yoan.diploma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mg.yoan.diploma.domain.Promotion;
import mg.yoan.diploma.service.DomainException;
import org.junit.jupiter.api.Test;

class PromotionServiceIT extends DiplomaIT {

  @Test
  void save_creates_then_updates() {
    Promotion promotion = newPromotion();

    Promotion updated =
        promotionService.save(promotion.getId().toString(), "Promo mise à jour", 2024, 2027);

    assertEquals(promotion.getId(), updated.getId());
    assertEquals("Promo mise à jour", updated.getLabel());
  }

  @Test
  void save_rejects_end_year_before_start_year() {
    assertThrows(DomainException.class, () -> promotionService.save(null, "P" + uid(), 2027, 2024));
  }

  @Test
  void save_accepts_end_year_equal_to_start_year() {
    Promotion promotion = promotionService.save(null, "P" + uid(), 2026, 2026);
    assertEquals(2026, promotion.getEndYear());
  }

  @Test
  void save_rejects_missing_years() {
    assertThrows(DomainException.class, () -> promotionService.save(null, "P" + uid(), null, 2027));
  }

  @Test
  void delete_removes_promotion_without_groups() {
    Promotion promotion = newPromotion();

    promotionService.delete(promotion.getId().toString());

    assertTrue(
        promotionService.listAll().stream().noneMatch(p -> p.getId().equals(promotion.getId())));
  }

  @Test
  void delete_rejects_promotion_with_groups() {
    Promotion promotion = newPromotion();
    newGroup(promotion);

    assertThrows(
        DomainException.class, () -> promotionService.delete(promotion.getId().toString()));
  }
}
