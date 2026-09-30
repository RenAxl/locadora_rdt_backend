package com.locadora_rdt_backend.modules.settings.financial_settings.repository;

import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinancialSettingRepository extends JpaRepository<FinancialSetting, Long> {

    @Query(value = "SELECT * FROM tb_financial_setting WHERE singleton_key IS NOT DISTINCT FROM :singletonKey",
            nativeQuery = true)
    Optional<FinancialSetting> findBySingletonKey(@Param("singletonKey") String singletonKey);
}
