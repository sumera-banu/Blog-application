package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.model.Post;
import com.blog.model.User;
import com.blog.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class BlogController {
    
    @Autowired
    private PostService postService;
    
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Page<Post> postsPage = postService.getUserPosts(user, page, size);
        List<Post> posts = postsPage.getContent() != null ? postsPage.getContent() : new ArrayList<>();
        
        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalPosts", postsPage.getTotalElements());
        
        return "dashboard";
    }
    
    @GetMapping("/posts/create")
    public String showCreatePostPage(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("postDto", new PostDto());
        model.addAttribute("user", user);
        return "create-post";
    }
    
    @PostMapping("/posts/create")
    public String createPost(@Valid @ModelAttribute("postDto") PostDto postDto,
                            BindingResult result,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (result.hasErrors()) {
            return "create-post";
        }
        
        try {
            postService.createPost(postDto, user);
            redirectAttributes.addFlashAttribute("success", 
                postDto.getIsPublished() ? "Post created and published successfully!" : "Post created as draft successfully!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            result.rejectValue("title", "error.general", 
                             "Failed to create post. Please try again.");
            return "create-post";
        }
    }
    
    @GetMapping("/posts/{id}/edit")
    public String showEditPostPage(@PathVariable Long id, 
                                  Model model, 
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Optional<Post> postOptional = postService.findById(id);
        if (postOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Post not found");
            return "redirect:/dashboard";
        }
        
        Post post = postOptional.get();
        if (!post.getAuthor().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You can only edit your own posts");
            return "redirect:/dashboard";
        }
        
        PostDto postDto = postService.convertToDto(post);
        model.addAttribute("postDto", postDto);
        model.addAttribute("post", post);
        model.addAttribute("user", user);
        
        return "edit-post";
    }
    
    @PostMapping("/posts/{id}/edit")
    public String updatePost(@PathVariable Long id,
                            @Valid @ModelAttribute("postDto") PostDto postDto,
                            BindingResult result,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        if (result.hasErrors()) {
            return "edit-post";
        }
        
        try {
            postService.updatePost(id, postDto, user);
            String message = postDto.getIsPublished() ? 
                "Post updated and published successfully!" : 
                "Post updated as draft successfully!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/dashboard";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            result.rejectValue("title", "error.general", 
                             "Failed to update post. Please try again.");
            return "edit-post";
        }
    }
    
    @PostMapping("/posts/{id}/publish")
    public String publishPost(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            postService.publishPost(id, user);
            redirectAttributes.addFlashAttribute("success", "Post published successfully!");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to publish post. Please try again.");
            return "redirect:/dashboard";
        }
    }
    
    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Post> postOptional = postService.findById(id);
        if (postOptional.isEmpty()) {
            return "redirect:/dashboard";
        }
        
        Post post = postOptional.get();
        User currentUser = getCurrentUser(session);
        
        model.addAttribute("post", post);
        model.addAttribute("user", currentUser);
        model.addAttribute("isOwner", currentUser != null && 
                          currentUser.getId().equals(post.getAuthor().getId()));
        
        return "view-post";
    }
    
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, 
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            postService.deletePost(id, user);
            redirectAttributes.addFlashAttribute("success", "Post deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete post.");
        }
        
        return "redirect:/dashboard";
    }
    
    @GetMapping("/search")
    public String searchPosts(@RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model, HttpSession session) {
        
        User user = getCurrentUser(session);
        Page<Post> postsPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (user != null) {
                postsPage = postService.searchUserPosts(user, keyword.trim(), page, size);
            } else {
                postsPage = postService.searchPublishedPosts(keyword.trim(), page, size);
            }
        } else {
            if (user != null) {
                postsPage = postService.getUserPosts(user, page, size);
            } else {
                postsPage = postService.getAllPublishedPosts(page, size);
            }
        }
        
        model.addAttribute("posts", postsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("totalPosts", postsPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("user", user);
        
        return "search";
    }
}