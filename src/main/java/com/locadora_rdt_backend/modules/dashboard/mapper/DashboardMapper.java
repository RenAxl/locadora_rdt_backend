package com.locadora_rdt_backend.modules.dashboard.mapper;

import com.locadora_rdt_backend.modules.dashboard.dto.DashboardDTO;
import com.locadora_rdt_backend.modules.dashboard.model.Dashboard;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public DashboardMapper() {
    }

    public DashboardDTO toDTO(Dashboard entity) {

        DashboardDTO dto = new DashboardDTO();

        dto.setAvailableGames(entity.getAvailableGames());
        dto.setActiveConsoles(entity.getActiveConsoles());
        dto.setActiveRentals(entity.getActiveRentals());
        dto.setReturnsToday(entity.getReturnsToday());
        dto.setActiveCustomers(entity.getActiveCustomers());
        dto.setOverdueRentals(entity.getOverdueRentals());
        dto.setDailyRentals(entity.getDailyRentals());

        return dto;
    }
}
