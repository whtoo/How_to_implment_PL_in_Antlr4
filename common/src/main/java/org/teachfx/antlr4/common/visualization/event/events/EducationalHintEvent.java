package org.teachfx.antlr4.common.visualization.event.events;

import org.teachfx.antlr4.common.visualization.event.EventType;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

public class EducationalHintEvent extends VMEvent {
    private final String hint;
    
    public EducationalHintEvent(Object source, int stepNumber, String hint) {
        super(source, EventType.EDUCATIONAL_HINT, stepNumber);
        this.hint = hint;
    }
    
    public String getHint() {
        return hint;
    }
    
    @Override
    public String getDescription() {
        return "教育提示: " + (hint != null ? hint : "");
    }
    
    @Override
    public String getDetailedDescription() {
        return String.format("步骤 %d - 教育提示: %s", 
                          getStepNumber(),
                          hint != null ? hint : "");
    }
}