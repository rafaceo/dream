package com.demo.cards.repository;

import com.demo.cards.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findAllByOwnerId(UUID ownerId);

    boolean existsByCardNumberHash(String cardNumberHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c where c.cardId in :ids order by c.cardId")
    List<Card> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
