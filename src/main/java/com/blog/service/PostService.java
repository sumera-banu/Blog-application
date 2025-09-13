package com.blog.service;

import com.blog.dto.PostDto;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    public Post createPost(PostDto postDto, User author) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setSummary(postDto.getSummary());
        post.setTags(postDto.getTags());
        post.setIsPublished(postDto.getIsPublished());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        
        return postRepository.save(post);
    }
    
    public Post updatePost(Long id, PostDto postDto, User author) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found");
        }
        
        Post post = optionalPost.get();
        
        // Check if the current user is the author
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only edit your own posts");
        }
        
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setSummary(postDto.getSummary());
        post.setTags(postDto.getTags());
        post.setIsPublished(postDto.getIsPublished());
        post.setUpdatedAt(LocalDateTime.now());
        
        return postRepository.save(post);
    }
    
    public void publishPost(Long id, User author) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found");
        }
        
        Post post = optionalPost.get();
        
        // Check if the current user is the author
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only publish your own posts");
        }
        
        post.setIsPublished(true);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }
    
    public void deletePost(Long id, User author) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            throw new IllegalArgumentException("Post not found");
        }
        
        Post post = optionalPost.get();
        
        // Check if the current user is the author
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }
        
        postRepository.delete(post);
    }
    
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
    
    public Page<Post> getAllPublishedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByIsPublishedTrue(pageable);
    }
    
    public Page<Post> getUserPosts(User author, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByAuthor(author, pageable);
    }
    
    public Page<Post> searchPublishedPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.searchPublishedPosts(keyword, pageable);
    }
    
    public Page<Post> searchUserPosts(User author, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.searchUserPosts(author, keyword, pageable);
    }
    
    public List<String> getAllUniqueTags() {
        return postRepository.findAllUniqueTags();
    }
    
    public List<Post> getUserPosts(User author) {
        return postRepository.findByAuthor(author);
    }
    
    // Convert Entity to DTO
    public PostDto convertToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setSummary(post.getSummary());
        dto.setTags(post.getTags());
        dto.setIsPublished(post.getIsPublished());
        dto.setAuthorName(post.getAuthor().getFullName());
        return dto;
    }
}