package com.locadora_rdt_backend.modules.settings.system_settings.repository;

import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    @Query(
            value = "SELECT * FROM tb_system_setting WHERE singleton_key = :singletonKey LIMIT 1",
            nativeQuery = true
    )
    Optional<SystemSetting> findBySingletonKey(@Param("singletonKey") String singletonKey);
}
