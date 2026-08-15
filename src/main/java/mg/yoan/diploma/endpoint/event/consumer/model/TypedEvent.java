package mg.yoan.diploma.endpoint.event.consumer.model;

import mg.yoan.diploma.PojaGenerated;
import mg.yoan.diploma.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
