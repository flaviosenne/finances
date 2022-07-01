package com.project.finances.app.usecases.category.repository;

import com.project.finances.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface CategoryRepository extends JpaRepository<Category, String> {
    @Query("select c from Category c join c.user u " +
            "where u.id = :userId and c.description like %:description% ")
    List<Category> findCategoryByUserId(String userId, String description);

    @Query("select c from Category c join c.user u " +
            "where c.id = :id and u.id = :userId")
    Optional<Category> findByIdAndByUserId(String id, String userId);
}