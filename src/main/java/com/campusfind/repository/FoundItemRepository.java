package com.campusfind.repository;

import com.campusfind.entity.FoundItem;
import com.campusfind.entity.FoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findByStatus(FoundStatus status);

    long countByStatus(FoundStatus status);
}
