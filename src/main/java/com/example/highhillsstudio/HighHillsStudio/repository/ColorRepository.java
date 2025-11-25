package com.example.highhillsstudio.HighHillsStudio.repository;

import com.example.highhillsstudio.HighHillsStudio.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository <Color, Long>  {

}
