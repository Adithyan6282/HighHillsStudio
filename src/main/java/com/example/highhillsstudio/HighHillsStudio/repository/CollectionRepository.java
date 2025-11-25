package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

}
