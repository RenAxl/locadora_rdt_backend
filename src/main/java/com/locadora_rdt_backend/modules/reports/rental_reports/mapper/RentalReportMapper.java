package com.locadora_rdt_backend.modules.reports.rental_reports.mapper;

import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReport;
import org.springframework.stereotype.Component;

@Component
public class RentalReportMapper {

    public RentalReportMapper() {
    }

    public RentalReportDTO toDTO(RentalReport entity) {

        RentalReportDTO dto = new RentalReportDTO();

        dto.setRentalTotal(entity.getRentalTotal());
        dto.setPaidTotal(entity.getPaidTotal());
        dto.setRentalCount(entity.getRentalCount());
        dto.setPaidCount(entity.getPaidCount());
        dto.setYear(entity.getYear());
        dto.setMonths(entity.getMonths());

        return dto;
    }
}
