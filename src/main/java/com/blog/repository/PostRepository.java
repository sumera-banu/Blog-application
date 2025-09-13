package com.blog.repository;

import com.blog.model.Post;
import com.blog.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    List<Post> findByAuthor(User author);
    
    Page<Post> findByIsPublishedTrue(Pageable pageable);
    
    Page<Post> findByAuthor(User author, Pageable pageable);
    
    // Simple approach - handle case conversion in service layer
    @Query("SELECT p FROM Post p WHERE p.isPublished = true AND " +
           "(p.title LIKE CONCAT('%', :keyword, '%') OR " +
           "p.content LIKE CONCAT('%', :keyword, '%') OR " +
           "p.tags LIKE CONCAT('%', :keyword, '%'))")
    Page<Post> searchPublishedPosts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.author = :author AND " +
           "(p.title LIKE CONCAT('%', :keyword, '%') OR " +
           "p.content LIKE CONCAT('%', :keyword, '%') OR " +
           "p.tags LIKE CONCAT('%', :keyword, '%'))")
    Page<Post> searchUserPosts(@Param("author") User author, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p.tags FROM Post p WHERE p.tags IS NOT NULL AND p.isPublished = true")
    List<String> findAllUniqueTags();
}