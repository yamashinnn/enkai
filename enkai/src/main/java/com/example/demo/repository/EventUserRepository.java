package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.EventUser;

public interface EventUserRepository extends JpaRepository<EventUser, Integer>{
	List<EventUser> findByEventId(Integer eventId);
}
