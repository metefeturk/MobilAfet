package com.example.mobilafet.utils;

/**
 * Small UI helpers for empty states; avoids scattering magic strings in Activities.
 */
public final class DisplayFormatter {

    private DisplayFormatter() {
    }

    /** Neutral placeholder when a field has no value yet. */
    public static String notAvailable() {
        return "—";
    }
}
