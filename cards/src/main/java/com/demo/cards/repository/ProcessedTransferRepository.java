package com.demo.cards.repository;

import com.demo.cards.entity.ProcessedTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedTransferRepository extends JpaRepository<ProcessedTransfer, UUID> {
}
