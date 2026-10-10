package com.locadora_rdt_backend.modules.dashboard.constants;

import java.time.ZoneId;
import java.util.List;

public final class DashboardConstants {

    // Situações das locações
    public static final String RENTED_STATUS = "RENTED";
    public static final String DELIVERED_STATUS = "DELIVERED";

    // Períodos e rótulos
    public static final ZoneId PROJECT_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final int DAYS_IN_WEEK = 7;
    public static final int DAYS_BEFORE_TODAY = 6;
    public static final int NEXT_DAY_OFFSET = 1;
    public static final int DAY_OF_WEEK_INDEX_OFFSET = 1;
    public static final List<String> WEEK_DAY_LABELS = List.of(
            "Seg",
            "Ter",
            "Qua",
            "Qui",
            "Sex",
            "Sáb",
            "Dom"
    );

    private DashboardConstants() {
    }
}
