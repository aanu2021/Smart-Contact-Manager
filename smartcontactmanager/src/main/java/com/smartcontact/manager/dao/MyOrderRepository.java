package com.smartcontact.manager.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcontact.manager.entities.MyOrder;
import com.smartcontact.manager.entities.User;

import java.util.List;

public interface MyOrderRepository extends JpaRepository<MyOrder, Long>{
    public MyOrder findByOrderId(String orderId);
    public List<MyOrder> findByUser(User user);
}
