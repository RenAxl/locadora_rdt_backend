package com.locadora_rdt_backend.modules.dashboard.model;

import java.io.Serializable;
import java.util.List;

public class Dashboard implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long availableGames;
    private Long activeConsoles;
    private Long activeRentals;
    private Long returnsToday;
    private Long activeCustomers;
    private Long overdueRentals;
    private List<DashboardDailyRental> dailyRentals;

    public Dashboard() {
    }

    public Long getAvailableGames() {
        return availableGames;
    }

    public void setAvailableGames(Long availableGames) {
        this.availableGames = availableGames;
    }

    public Long getActiveConsoles() {
        return activeConsoles;
    }

    public void setActiveConsoles(Long activeConsoles) {
        this.activeConsoles = activeConsoles;
    }

    public Long getActiveRentals() {
        return activeRentals;
    }

    public void setActiveRentals(Long activeRentals) {
        this.activeRentals = activeRentals;
    }

    public Long getReturnsToday() {
        return returnsToday;
    }

    public void setReturnsToday(Long returnsToday) {
        this.returnsToday = returnsToday;
    }

    public Long getActiveCustomers() {
        return activeCustomers;
    }

    public void setActiveCustomers(Long activeCustomers) {
        this.activeCustomers = activeCustomers;
    }

    public Long getOverdueRentals() {
        return overdueRentals;
    }

    public void setOverdueRentals(Long overdueRentals) {
        this.overdueRentals = overdueRentals;
    }

    public List<DashboardDailyRental> getDailyRentals() {
        return dailyRentals;
    }

    public void setDailyRentals(List<DashboardDailyRental> dailyRentals) {
        this.dailyRentals = dailyRentals;
    }
}
