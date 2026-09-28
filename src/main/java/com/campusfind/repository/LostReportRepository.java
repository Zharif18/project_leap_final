package com.campusfind.repository;

import com.campusfind.entity.LostReport;
import com.campusfind.entity.LostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LostReportRepository extends JpaRepository<LostReport, Long> {
    List<LostReport> findByStatus(LostStatus status);

    long countByStatus(LostStatus status);

    List<LostReport> findByMatchedFoundItemId(Long foundItemId);
}
