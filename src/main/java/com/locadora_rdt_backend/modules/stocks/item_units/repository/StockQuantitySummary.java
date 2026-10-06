package com.locadora_rdt_backend.modules.stocks.item_units.repository;

// Resultado da consulta de contagem das unidades ativas.
public interface StockQuantitySummary {

    Long getTotalQuantity();

    Long getAvailableQuantity();

    Long getUnavailableQuantity();

    Long getMaintenanceQuantity();

    Long getDamagedQuantity();

    Long getLostQuantity();
}
