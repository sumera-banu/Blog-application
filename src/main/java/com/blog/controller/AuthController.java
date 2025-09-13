package com.blog.controller;

import com.blog.dto.UserDto;
import com.blog.model.User;
import com.blog.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }
    
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/auth/login";
            }
            
            User user = userOptional.get();
            
            if (!userService.validatePassword(password, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/auth/login";
            }
            
            if (!user.getIsActive()) {
                redirectAttributes.addFlashAttribute("error", "Account is deactivated");
                return "redirect:/auth/login";
            }
            
            session.setAttribute("user", user);
            redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getFullName() + "!");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed. Please try again.");
            return "redirect:/auth/login";
        }
    }
    
    @GetMapping("/register")
    public String showRegistrationPage(Model model, HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("userDto", new UserDto());
        return "register";
    }
    
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("userDto") UserDto userDto,
                                    BindingResult result,
                                    RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "register";
        }
        
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", 
                             "Passwords do not match");
            return "register";
        }
        
        try {
            userService.registerUser(userDto);
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please log in.");
            return "redirect:/auth/login";
            
        } catch (IllegalArgumentException e) {
            result.rejectValue("username", "error.username", e.getMessage());
            return "register";
        } catch (Exception e) {
            result.rejectValue("username", "error.general", 
                             "Registration failed. Please try again.");
            return "register";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "You have been logged out successfully.");
        return "redirect:/auth/login";
    }
}