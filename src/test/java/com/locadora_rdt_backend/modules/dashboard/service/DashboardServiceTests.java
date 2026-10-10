package com.locadora_rdt_backend.modules.dashboard.service;

import com.locadora_rdt_backend.modules.dashboard.constants.DashboardConstants;
import com.locadora_rdt_backend.modules.dashboard.dto.DashboardDTO;
import com.locadora_rdt_backend.modules.dashboard.mapper.DashboardMapper;
import com.locadora_rdt_backend.modules.dashboard.model.Dashboard;
import com.locadora_rdt_backend.modules.dashboard.model.DashboardDailyRental;
import com.locadora_rdt_backend.modules.dashboard.repository.DashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTests {

    @Mock
    private DashboardRepository repository;

    @Mock
    private DashboardMapper mapper;

    @InjectMocks
    private DashboardServiceImpl service;

    private DashboardDTO dashboardDTO;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        dashboardDTO = new DashboardDTO();
        dashboardDTO.setAvailableGames(12L);
        dashboardDTO.setActiveConsoles(4L);
        dashboardDTO.setActiveRentals(8L);
        dashboardDTO.setReturnsToday(2L);
        dashboardDTO.setActiveCustomers(20L);
        dashboardDTO.setOverdueRentals(3L);

        today = LocalDate.now(DashboardConstants.PROJECT_ZONE);
    }

    @Test
    void getSummaryShouldReturnDashboard() {
        LocalDate firstDay = today.minusDays(6);
        Instant startToday = today.atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();
        Instant endToday = today.plusDays(1).atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant();
        when(repository.countAvailableGames()).thenReturn(12L);
        when(repository.countActiveConsoles()).thenReturn(4L);
        when(repository.countByStatus("RENTED")).thenReturn(8L);
        when(repository.countByStatusAndEffectiveReturnDateBetween("DELIVERED", startToday, endToday))
                .thenReturn(2L);
        when(repository.countByActiveTrue()).thenReturn(20L);
        when(repository.countByStatusAndReturnForecastDateBefore(eq("RENTED"), any(Instant.class)))
                .thenReturn(3L);
        when(repository.countByRegistrationDateBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(1L, 2L, 3L, 4L, 5L, 6L, 7L);
        when(mapper.toDTO(any(Dashboard.class))).thenCallRealMethod();

        Instant before = Instant.now();
        DashboardDTO resultado = service.getSummary();
        Instant after = Instant.now();

        assertNotNull(resultado);
        assertEquals(dashboardDTO.getAvailableGames(), resultado.getAvailableGames());
        assertEquals(dashboardDTO.getActiveConsoles(), resultado.getActiveConsoles());
        assertEquals(dashboardDTO.getActiveRentals(), resultado.getActiveRentals());
        assertEquals(dashboardDTO.getReturnsToday(), resultado.getReturnsToday());
        assertEquals(dashboardDTO.getActiveCustomers(), resultado.getActiveCustomers());
        assertEquals(dashboardDTO.getOverdueRentals(), resultado.getOverdueRentals());
        List<DashboardDailyRental> days = resultado.getDailyRentals();
        assertEquals(7, days.size());
        assertEquals(firstDay, days.get(0).getDate());
        assertEquals(firstDay.plusDays(1), days.get(1).getDate());
        assertEquals(firstDay.plusDays(2), days.get(2).getDate());
        assertEquals(firstDay.plusDays(3), days.get(3).getDate());
        assertEquals(firstDay.plusDays(4), days.get(4).getDate());
        assertEquals(firstDay.plusDays(5), days.get(5).getDate());
        assertEquals(today, days.get(6).getDate());
        assertEquals(1L, days.get(0).getQuantity());
        assertEquals(2L, days.get(1).getQuantity());
        assertEquals(3L, days.get(2).getQuantity());
        assertEquals(4L, days.get(3).getQuantity());
        assertEquals(5L, days.get(4).getQuantity());
        assertEquals(6L, days.get(5).getQuantity());
        assertEquals(7L, days.get(6).getQuantity());
        assertEquals(DashboardConstants.WEEK_DAY_LABELS.get(firstDay.getDayOfWeek().getValue() - 1),
                days.get(0).getLabel());
        assertEquals(DashboardConstants.WEEK_DAY_LABELS.get(today.getDayOfWeek().getValue() - 1),
                days.get(6).getLabel());
        verify(mapper).toDTO(any(Dashboard.class));
        verify(repository).countByStatusAndEffectiveReturnDateBetween("DELIVERED", startToday, endToday);
        ArgumentCaptor<Instant> startsCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endsCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository, times(7)).countByRegistrationDateBetween(startsCaptor.capture(), endsCaptor.capture());
        assertEquals(firstDay.atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant(),
                startsCaptor.getAllValues().get(0));
        assertEquals(firstDay.plusDays(1).atStartOfDay(DashboardConstants.PROJECT_ZONE).toInstant(),
                endsCaptor.getAllValues().get(0));
        assertEquals(startToday, startsCaptor.getAllValues().get(6));
        assertEquals(endToday, endsCaptor.getAllValues().get(6));
        ArgumentCaptor<Instant> overdueCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).countByStatusAndReturnForecastDateBefore(eq("RENTED"), overdueCaptor.capture());
        assertFalse(overdueCaptor.getValue().isBefore(before));
        assertFalse(overdueCaptor.getValue().isAfter(after));
    }

    @Test
    void getSummaryShouldThrowExceptionWhenRepositoryFails() {
        when(repository.countAvailableGames())
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.getSummary());

        verify(repository).countAvailableGames();
        verifyNoMoreInteractions(repository);
        verify(mapper, never()).toDTO(any());
    }
}
