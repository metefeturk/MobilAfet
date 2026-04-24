package com.example.mobilafet.models;

/**
 * Single step in an evacuation checklist. Used later with routing / guidance data.
 */
public class EvacuationStep {

    private int stepNumber;
    private String title;
    private String description;

    public EvacuationStep() {
    }

    public EvacuationStep(int stepNumber, String title, String description) {
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
