package com.leathric.repository;

import com.leathric.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    @Query("SELECT h FROM OrderStatusHistory h WHERE h.order.id = :orderId ORDER BY h.timestamp ASC")
    List<OrderStatusHistory> findByOrderIdOrderByTimestampAsc(@Param("orderId") Long orderId);
}
