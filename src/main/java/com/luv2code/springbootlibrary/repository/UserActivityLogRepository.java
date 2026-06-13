package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId);

    Page<UserActivityLog> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT l FROM UserActivityLog l WHERE l.timestamp BETWEEN :start AND :end")
    List<UserActivityLog> findByPeriod(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT l.action, COUNT(l) FROM UserActivityLog l WHERE l.user.id = :userId GROUP BY l.action")
    List<Object[]> countActionsByUser(@Param("userId") Long userId);
}