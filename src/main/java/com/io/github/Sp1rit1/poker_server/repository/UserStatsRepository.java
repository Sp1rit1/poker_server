package com.io.github.Sp1rit1.poker_server.repository;

import com.io.github.Sp1rit1.poker_server.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, Long> {
    // Long здесь это тип userId, который является первичным ключом для UserStats
}