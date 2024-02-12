package com.cognifyz.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cognifyz.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

}
