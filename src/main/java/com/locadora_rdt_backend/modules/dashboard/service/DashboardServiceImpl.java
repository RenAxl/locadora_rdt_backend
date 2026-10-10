package com.locadora_rdt_backend.modules.dashboard.service;

import com.locadora_rdt_backend.modules.dashboard.constants.DashboardConstants;
import com.locadora_rdt_backend.modules.dashboard.dto.DashboardDTO;
import com.locadora_rdt_backend.modules.dashboard.mapper.DashboardMapper;
import com.locadora_rdt_backend.modules.dashboard.model.Dashboard;
import com.locadora_rdt_backend.modules.dashboard.model.DashboardDailyRental;
import com.locadora_rdt_backend.modules.dashboard.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository repository;
    private final DashboardMapper mapper;

    public DashboardServiceImpl(
            DashboardRepository repository,
            DashboardMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getSummary() {

        LocalDate today = LocalDate.now(DashboardConstants.PROJECT_ZONE);
        Instant startToday = today.atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();
        Instant endToday = today.plusDays(DashboardConstants.NEXT_DAY_OFFSET)
                .atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();

        Dashboard dashboard = new Dashboard();

        dashboard.setAvailableGames(repository.countAvailableGames());
        dashboard.setActiveConsoles(repository.countActiveConsoles());
        dashboard.setActiveRentals(repository.countByStatus(DashboardConstants.RENTED_STATUS));
        dashboard.setReturnsToday(repository.countByStatusAndEffectiveReturnDateBetween(
                DashboardConstants.DELIVERED_STATUS, startToday, endToday));
        dashboard.setActiveCustomers(repository.countByActiveTrue());
        dashboard.setOverdueRentals(repository.countByStatusAndReturnForecastDateBefore(
                DashboardConstants.RENTED_STATUS, Instant.now()));
        dashboard.setDailyRentals(getDailyRentals(today));

        DashboardDTO dashboardDTO = mapper.toDTO(dashboard);

        return dashboardDTO;
    }

    private List<DashboardDailyRental> getDailyRentals(LocalDate today) {

        List<DashboardDailyRental> days = new ArrayList<>();
        LocalDate firstDay = today.minusDays(DashboardConstants.DAYS_BEFORE_TODAY);

        for (int index = 0; index < DashboardConstants.DAYS_IN_WEEK; index++) {
            LocalDate date = firstDay.plusDays(index);
            Instant start = date.atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();
            Instant end = date.plusDays(DashboardConstants.NEXT_DAY_OFFSET)
                    .atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();
            long quantity = repository.countByRegistrationDateBetween(start, end);
            int labelIndex = date.getDayOfWeek().getValue() - DashboardConstants.DAY_OF_WEEK_INDEX_OFFSET;
            String label = DashboardConstants.WEEK_DAY_LABELS.get(labelIndex);

            DashboardDailyRental day = new DashboardDailyRental();
            day.setDate(date);
            day.setLabel(label);
            day.setQuantity(quantity);

            days.add(day);
        }

        return days;
    }
}
