package com.easybilling.enums;

public enum MovementType {
    IN,          // Stock incoming (purchases, returns)
    OUT,         // Stock outgoing (sales)
    TRANSFER,    // Between locations
    ADJUSTMENT   // Manual adjustment
}
