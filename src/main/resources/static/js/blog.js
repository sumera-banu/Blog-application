/**
 * BlogSpace - Interactive JavaScript Features
 * Enhanced user experience with modern interactions
 */

// ===========================================
// Mobile Menu Functionality
// ===========================================

BlogApp.setupMobileMenu = function() {
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const navMenu = document.querySelector('.nav-menu');
    
    if (mobileMenuBtn && navMenu) {
        mobileMenuBtn.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            const icon = mobileMenuBtn.querySelector('i');
            
            if (navMenu.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
        
        // Close mobile menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!navMenu.contains(e.target) && !mobileMenuBtn.contains(e.target)) {
                navMenu.classList.remove('active');
                mobileMenuBtn.querySelector('i').classList.remove('fa-times');
                mobileMenuBtn.querySelector('i').classList.add('fa-bars');
            }
        });
    }
};

// ===========================================
// Search Functionality
// ===========================================

BlogApp.setupSearch = function() {
    const searchInput = document.querySelector('#searchInput');
    const searchForm = document.querySelector('#searchForm');
    
    if (searchInput) {
        let searchTimeout;
        
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            const query = e.target.value.trim();
            
            if (query.length >= 2) {
                searchTimeout = setTimeout(() => {
                    this.performSearch(query);
                }, this.config.debounceDelay);
            }
        });
        
        // Handle search form submission
        if (searchForm) {
            searchForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const query = searchInput.value.trim();
                if (query) {
                    this.performSearch(query, true);
                }
            });
        }
    }
};

BlogApp.performSearch = function(query, redirect = false) {
    if (redirect) {
        window.location.href = `/blog/search?keyword=${encodeURIComponent(query)}`;
        return;
    }
    
    // Show search suggestions (if on search page)
    const searchSuggestions = document.querySelector('#searchSuggestions');
    if (searchSuggestions) {
        // This would typically make an AJAX call to get suggestions
        this.showSearchSuggestions(query);
    }
};

BlogApp.showSearchSuggestions = function(query) {
    // Mock suggestions - in a real app, this would fetch from the server
    const suggestions = [
        'JavaScript tutorials',
        'Spring Boot guide',
        'Web development tips',
        'Database optimization'
    ].filter(s => s.toLowerCase().includes(query.toLowerCase()));
    
    const suggestionsEl = document.querySelector('#searchSuggestions');
    if (suggestionsEl && suggestions.length > 0) {
        suggestionsEl.innerHTML = suggestions
            .map(s => `<div class="search-suggestion" onclick="BlogApp.selectSuggestion('${s}')">${s}</div>`)
            .join('');
        suggestionsEl.style.display = 'block';
    }
};

BlogApp.selectSuggestion = function(suggestion) {
    const searchInput = document.querySelector('#searchInput');
    if (searchInput) {
        searchInput.value = suggestion;
        this.performSearch(suggestion, true);
    }
};

// ===========================================
// Post Filters and View Toggle
// ===========================================

BlogApp.setupPostFilters = function() {
    const filterSelect = document.querySelector('#postFilter');
    
    if (filterSelect) {
        filterSelect.addEventListener('change', (e) => {
            const filterValue = e.target.value;
            this.filterPosts(filterValue);
        });
    }
};

BlogApp.filterPosts = function(filter) {
    const postCards = document.querySelectorAll('.post-card');
    
    postCards.forEach(card => {
        const status = card.dataset.status;
        let show = false;
        
        switch (filter) {
            case 'all':
                show = true;
                break;
            case 'published':
                show = status === 'published';
                break;
            case 'drafts':
                show = status === 'draft';
                break;
        }
        
        if (show) {
            card.style.display = 'block';
            card.classList.add('animate-fade-in-up');
        } else {
            card.style.display = 'none';
        }
    });
    
    // Update empty state
    this.updateEmptyState();
};

BlogApp.setupViewToggle = function() {
    const viewButtons = document.querySelectorAll('.view-btn');
    const postsContainer = document.querySelector('#postsContainer');
    
    viewButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const view = btn.dataset.view;
            
            // Update active state
            viewButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            // Toggle view
            if (postsContainer) {
                postsContainer.className = view === 'grid' ? 'posts-grid' : 'posts-list';
            }
        });
    });
};

BlogApp.updateEmptyState = function() {
    const visiblePosts = document.querySelectorAll('.post-card[style*="block"], .post-card:not([style*="none"])');
    const emptyState = document.querySelector('.empty-state');
    const postsContainer = document.querySelector('#postsContainer');
    
    if (visiblePosts.length === 0 && postsContainer) {
        if (!emptyState) {
            const emptyDiv = document.createElement('div');
            emptyDiv.className = 'empty-state';
            emptyDiv.innerHTML = `
                <div class="empty-illustration">
                    <i class="fas fa-search"></i>
                </div>
                <h3>No posts found</h3>
                <p>Try adjusting your filters or search terms.</p>
            `;
            postsContainer.parentNode.appendChild(emptyDiv);
        } else {
            emptyState.style.display = 'block';
        }
    } else if (emptyState) {
        emptyState.style.display = 'none';
    }
};

// ===========================================
// Auto-save Functionality
// ===========================================

BlogApp.setupAutoSave = function() {
    const postForm = document.querySelector('.post-form');
    
    if (postForm) {
        this.state.autoSaveTimer = setInterval(() => {
            if (this.state.hasUnsavedChanges) {
                this.autoSavePost();
            }
        }, this.config.autoSaveInterval);
    }
};

BlogApp.autoSavePost = function() {
    const titleInput = document.querySelector('#title');
    const contentEditor = document.querySelector('#content');
    const summaryInput = document.querySelector('#summary');
    const tagsInput = document.querySelector('#tags');
    
    if (!titleInput || !contentEditor) return;
    
    const postData = {
        title: titleInput.value,
        content: contentEditor.value,
        summary: summaryInput?.value || '',
        tags: tagsInput?.value || '',
        isPublished: false // Always save as draft for auto-save
    };
    
    // Show saving indicator
    this.showSaveStatus('Saving...');
    
    // Simulate auto-save (in real app, this would be an AJAX call)
    setTimeout(() => {
        localStorage.setItem('blog_autosave', JSON.stringify(postData));
        this.state.hasUnsavedChanges = false;
        this.showSaveStatus('Saved');
    }, 1000);
};

BlogApp.showSaveStatus = function(status) {
    const saveStatusEl = document.querySelector('.save-status span');
    if (saveStatusEl) {
        saveStatusEl.textContent = status;
        
        if (status === 'Saved') {
            setTimeout(() => {
                saveStatusEl.textContent = 'Auto-saving...';
            }, 2000);
        }
    }
};

BlogApp.loadAutoSavedContent = function() {
    const autoSavedData = localStorage.getItem('blog_autosave');
    
    if (autoSavedData && confirm('Would you like to restore your previously saved content?')) {
        const data = JSON.parse(autoSavedData);
        
        const titleInput = document.querySelector('#title');
        const contentEditor = document.querySelector('#content');
        const summaryInput = document.querySelector('#summary');
        const tagsInput = document.querySelector('#tags');
        
        if (titleInput) titleInput.value = data.title;
        if (contentEditor) contentEditor.value = data.content;
        if (summaryInput) summaryInput.value = data.summary;
        if (tagsInput) tagsInput.value = data.tags;
        
        localStorage.removeItem('blog_autosave');
        this.showAlert('Content restored successfully!');
    }
};

// ===========================================
// Theme Management
// ===========================================

BlogApp.setupTheme = function() {
    // Check for saved theme preference or default to 'light'
    const savedTheme = localStorage.getItem('blog_theme') || 'light';
    this.state.currentTheme = savedTheme;
    document.documentElement.setAttribute('data-theme', savedTheme);
    
    // Setup theme toggle if it exists
    const themeToggle = document.querySelector('#themeToggle');
    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            this.toggleTheme();
        });
    }
};

BlogApp.toggleTheme = function() {
    const newTheme = this.state.currentTheme === 'light' ? 'dark' : 'light';
    this.state.currentTheme = newTheme;
    
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('blog_theme', newTheme);
    
    // Update theme toggle icon
    const themeToggle = document.querySelector('#themeToggle i');
    if (themeToggle) {
        themeToggle.className = newTheme === 'light' ? 'fas fa-moon' : 'fas fa-sun';
    }
};

// ===========================================
// Editor Enhancements
// ===========================================

BlogApp.initializeComponents = function() {
    // Character counters
    this.setupCharacterCounters();
    
    // Tags input enhancement
    this.setupTagsInput();
    
    // Content preview
    this.setupContentPreview();
    
    // File upload (if needed)
    this.setupFileUpload();
    
    // Load auto-saved content on editor pages
    if (document.querySelector('.post-form')) {
        this.loadAutoSavedContent();
    }
};

BlogApp.setupCharacterCounters = function() {
    const inputs = document.querySelectorAll('input[maxlength], textarea[maxlength]');
    
    inputs.forEach(input => {
        const maxLength = parseInt(input.getAttribute('maxlength'));
        const counterId = input.id + 'Count';
        const counter = document.getElementById(counterId);
        
        if (counter) {
            const updateCounter = () => {
                const currentLength = input.value.length;
                counter.textContent = currentLength;
                
                // Add warning class if near limit
                if (currentLength > maxLength * 0.8) {
                    counter.classList.add('near-limit');
                } else {
                    counter.classList.remove('near-limit');
                }
            };
            
            input.addEventListener('input', updateCounter);
            updateCounter(); // Initial update
        }
    });
};

BlogApp.setupTagsInput = function() {
    const tagsInput = document.querySelector('#tags');
    const tagsPreview = document.querySelector('#tagsPreview');
    
    if (tagsInput && tagsPreview) {
        tagsInput.addEventListener('input', () => {
            const tags = tagsInput.value
                .split(',')
                .map(tag => tag.trim())
                .filter(tag => tag.length > 0);
            
            if (tags.length > 0) {
                tagsPreview.innerHTML = tags
                    .map(tag => `<span class="tag-preview">${this.escapeHtml(tag)}</span>`)
                    .join('');
                tagsPreview.style.display = 'flex';
            } else {
                tagsPreview.style.display = 'none';
            }
        });
    }
};

BlogApp.setupContentPreview = function() {
    // This would integrate with the preview modal functionality
    // Already handled in the HTML templates
};

BlogApp.setupFileUpload = function() {
    const fileInputs = document.querySelectorAll('input[type="file"]');
    
    fileInputs.forEach(input => {
        input.addEventListener('change', (e) => {
            const files = e.target.files;
            if (files.length > 0) {
                this.handleFileUpload(files, input);
            }
        });
    });
};

BlogApp.handleFileUpload = function(files, input) {
    // Handle file upload logic
    Array.from(files).forEach(file => {
        if (file.size > 10 * 1024 * 1024) { // 10MB limit
            this.showAlert('File size must be less than 10MB', 'error');
            return;
        }
        
        // Show upload progress (mock)
        this.showUploadProgress(file.name);
    });
};

BlogApp.showUploadProgress = function(filename) {
    this.showAlert(`Uploading ${filename}...`);
    
    // Mock upload completion
    setTimeout(() => {
        this.showAlert(`${filename} uploaded successfully!`);
    }, 2000);
};

// ===========================================
// Utility Functions
// ===========================================

BlogApp.escapeHtml = function(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
};

BlogApp.debounce = function(func, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(this, args), delay);
    };
};

BlogApp.formatDate = function(date) {
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    }).format(new Date(date));
};

BlogApp.truncateText = function(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength).trim() + '...';
};

BlogApp.handleResize = function() {
    // Handle responsive adjustments
    const isMobile = window.innerWidth <= 768;
    
    // Close mobile menu if screen becomes desktop
    if (!isMobile) {
        const navMenu = document.querySelector('.nav-menu');
        const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
        
        if (navMenu && navMenu.classList.contains('active')) {
            navMenu.classList.remove('active');
            if (mobileMenuBtn) {
                mobileMenuBtn.querySelector('i').classList.remove('fa-times');
                mobileMenuBtn.querySelector('i').classList.add('fa-bars');
            }
        }
    }
    
    // Adjust post grid if needed
    const postsGrid = document.querySelector('.posts-grid');
    if (postsGrid && isMobile) {
        postsGrid.style.gridTemplateColumns = '1fr';
    }
};

// ===========================================
// Social Sharing Functions
// ===========================================

BlogApp.shareOnTwitter = function(title, url) {
    const twitterUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`;
    window.open(twitterUrl, '_blank', 'width=550,height=420');
};

BlogApp.shareOnFacebook = function(url) {
    const facebookUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(url)}`;
    window.open(facebookUrl, '_blank', 'width=550,height=420');
};

BlogApp.shareOnLinkedIn = function(title, url) {
    const linkedInUrl = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}&title=${encodeURIComponent(title)}`;
    window.open(linkedInUrl, '_blank', 'width=550,height=420');
};

BlogApp.copyToClipboard = function(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            this.showAlert('Link copied to clipboard!');
        }).catch(() => {
            this.fallbackCopyToClipboard(text);
        });
    } else {
        this.fallbackCopyToClipboard(text);
    }
};

BlogApp.fallbackCopyToClipboard = function(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
        document.execCommand('copy');
        this.showAlert('Link copied to clipboard!');
    } catch (err) {
        this.showAlert('Failed to copy link', 'error');
    }
    
    document.body.removeChild(textArea);
};

// ===========================================
// Cleanup and Destroy
// ===========================================

BlogApp.destroy = function() {
    // Clear auto-save timer
    if (this.state.autoSaveTimer) {
        clearInterval(this.state.autoSaveTimer);
    }
    
    // Remove event listeners
    // (In a real app, you'd track and remove all listeners)
    
    console.log('BlogSpace destroyed');
};

// ===========================================
// Export functions to global scope for HTML usage
// ===========================================

window.confirmDelete = function(postId, title) {
    document.getElementById('postTitle').textContent = title;
    document.getElementById('deleteForm').action = `/blog/posts/${postId}/delete`;
    BlogApp.openModal('deleteModal');
};

window.closeDeleteModal = function() {
    const modal = document.getElementById('deleteModal');
    BlogApp.closeModal(modal);
};

window.saveDraft = function() {
    const publishedCheckbox = document.getElementById('isPublished');
    if (publishedCheckbox) {
        publishedCheckbox.checked = false;
    }
    document.querySelector('.post-form').submit();
};

window.previewPost = function() {
    const title = document.getElementById('title').value;
    const summary = document.getElementById('summary').value;
    const tags = document.getElementById('tags').value;
    
    // Get content from TinyMCE if available, otherwise from textarea
    let content = '';
    if (typeof tinymce !== 'undefined' && tinymce.get('content')) {
        content = tinymce.get('content').getContent();
    } else {
        const contentEl = document.getElementById('content');
        content = contentEl ? contentEl.value : '';
    }
    
    // Update preview modal
    document.getElementById('previewTitle').textContent = title || 'Untitled Post';
    document.getElementById('previewSummary').innerHTML = summary ? `<p class="summary">${summary}</p>` : '';
    document.getElementById('previewContent').innerHTML = content || '<p>No content yet...</p>';
    
    // Update tags
    const tagsContainer = document.getElementById('previewTags');
    if (tags.trim()) {
        const tagArray = tags.split(',').map(tag => tag.trim()).filter(tag => tag);
        tagsContainer.innerHTML = tagArray.map(tag => 
            `<span class="tag">${BlogApp.escapeHtml(tag)}</span>`
        ).join('');
        tagsContainer.style.display = 'block';
    } else {
        tagsContainer.style.display = 'none';
    }
    
    BlogApp.openModal('previewModal');
};

window.closePreview = function() {
    const modal = document.getElementById('previewModal');
    BlogApp.closeModal(modal);
};

// ===========================================
// Initialize Application
// ===========================================

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => BlogApp.init());
} else {
    BlogApp.init();
}

// Make BlogApp available globally for debugging
window.BlogApp = BlogApp;
// Global Variables and Configuration
// ===========================================

const BlogApp = {
    // Configuration
    config: {
        autoSaveInterval: 30000, // 30 seconds
        alertTimeout: 5000, // 5 seconds
        animationDuration: 300,
        debounceDelay: 500
    },
    
    // State management
    state: {
        isEditing: false,
        hasUnsavedChanges: false,
        autoSaveTimer: null,
        currentTheme: 'light'
    },
    
    // Initialize the application
    init() {
        this.setupEventListeners();
        this.initializeComponents();
        this.setupAutoSave();
        this.setupTheme();
        console.log('BlogSpace initialized successfully');
    }
};

// ===========================================
// Event Listeners Setup
// ===========================================

BlogApp.setupEventListeners = function() {
    // DOM Content Loaded
    document.addEventListener('DOMContentLoaded', () => {
        this.setupAlerts();
        this.setupDropdowns();
        this.setupModals();
        this.setupFormValidation();
        this.setupPasswordToggle();
        this.setupMobileMenu();
        this.setupSearch();
        this.setupPostFilters();
        this.setupViewToggle();
    });
    
    // Window events
    window.addEventListener('beforeunload', (e) => {
        if (this.state.hasUnsavedChanges) {
            e.preventDefault();
            e.returnValue = 'You have unsaved changes. Are you sure you want to leave?';
            return e.returnValue;
        }
    });
    
    // Handle window resize
    let resizeTimeout;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimeout);
        resizeTimeout = setTimeout(() => {
            this.handleResize();
        }, 250);
    });
};

// ===========================================
// Alert System
// ===========================================

BlogApp.setupAlerts = function() {
    // Auto-hide alerts
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        // Add close functionality
        const closeBtn = alert.querySelector('.alert-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                this.hideAlert(alert);
            });
        }
        
        // Auto-hide after timeout
        setTimeout(() => {
            this.hideAlert(alert);
        }, this.config.alertTimeout);
    });
};

BlogApp.showAlert = function(message, type = 'success') {
    const alertsContainer = document.querySelector('.alerts-container') || this.createAlertsContainer();
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        <span>${message}</span>
        <button class="alert-close">&times;</button>
    `;
    
    alertsContainer.appendChild(alert);
    
    // Add close functionality
    alert.querySelector('.alert-close').addEventListener('click', () => {
        this.hideAlert(alert);
    });
    
    // Auto-hide
    setTimeout(() => {
        this.hideAlert(alert);
    }, this.config.alertTimeout);
    
    // Animate in
    requestAnimationFrame(() => {
        alert.style.transform = 'translateX(0)';
        alert.style.opacity = '1';
    });
};

BlogApp.hideAlert = function(alert) {
    alert.style.transform = 'translateX(100%)';
    alert.style.opacity = '0';
    
    setTimeout(() => {
        if (alert.parentNode) {
            alert.parentNode.removeChild(alert);
        }
    }, this.config.animationDuration);
};

BlogApp.createAlertsContainer = function() {
    const container = document.createElement('div');
    container.className = 'alerts-container';
    document.body.appendChild(container);
    return container;
};

// ===========================================
// Dropdown Functionality
// ===========================================

BlogApp.setupDropdowns = function() {
    const dropdowns = document.querySelectorAll('.user-dropdown, .dropdown');
    
    dropdowns.forEach(dropdown => {
        const trigger = dropdown.querySelector('.user-btn, .dropdown-toggle');
        const menu = dropdown.querySelector('.dropdown-menu');
        
        if (!trigger || !menu) return;
        
        // Toggle dropdown on click
        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            this.toggleDropdown(dropdown);
        });
        
        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!dropdown.contains(e.target)) {
                this.closeDropdown(dropdown);
            }
        });
        
        // Close on escape key
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeDropdown(dropdown);
            }
        });
    });
};

BlogApp.toggleDropdown = function(dropdown) {
    const menu = dropdown.querySelector('.dropdown-menu');
    const isOpen = menu.classList.contains('show');
    
    // Close all other dropdowns
    document.querySelectorAll('.dropdown-menu.show').forEach(m => {
        m.classList.remove('show');
    });
    
    if (!isOpen) {
        menu.classList.add('show');
        dropdown.classList.add('active');
    }
};

BlogApp.closeDropdown = function(dropdown) {
    const menu = dropdown.querySelector('.dropdown-menu');
    menu.classList.remove('show');
    dropdown.classList.remove('active');
};

// ===========================================
// Modal Functionality
// ===========================================

BlogApp.setupModals = function() {
    const modals = document.querySelectorAll('.modal');
    
    modals.forEach(modal => {
        // Close button
        const closeBtn = modal.querySelector('.modal-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                this.closeModal(modal);
            });
        }
        
        // Click outside to close
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                this.closeModal(modal);
            }
        });
        
        // Escape key to close
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && modal.classList.contains('active')) {
                this.closeModal(modal);
            }
        });
    });
};

BlogApp.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        
        // Focus first input if available
        const firstInput = modal.querySelector('input, textarea, select');
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    }
};

BlogApp.closeModal = function(modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
};

// ===========================================
// Form Validation and Enhancement
// ===========================================

BlogApp.setupFormValidation = function() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        // Real-time validation
        const inputs = form.querySelectorAll('input, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateField(input);
            });
            
            input.addEventListener('input', () => {
                if (input.classList.contains('error')) {
                    this.validateField(input);
                }
                
                // Mark as changed for auto-save
                if (form.classList.contains('post-form')) {
                    this.state.hasUnsavedChanges = true;
                }
            });
        });
        
        // Form submission
        form.addEventListener('submit', (e) => {
            if (!this.validateForm(form)) {
                e.preventDefault();
                this.showAlert('Please fix the errors before submitting', 'error');
            } else {
                this.state.hasUnsavedChanges = false;
            }
        });
    });
};

BlogApp.validateField = function(field) {
    const value = field.value.trim();
    const fieldName = field.name;
    const label = field.previousElementSibling?.textContent || fieldName;
    
    // Remove existing error
    field.classList.remove('error');
    const existingError = field.parentNode.querySelector('.error-text');
    if (existingError) {
        existingError.remove();
    }
    
    let isValid = true;
    let errorMessage = '';
    
    // Required field validation
    if (field.hasAttribute('required') && !value) {
        isValid = false;
        errorMessage = `${label} is required`;
    }
    
    // Email validation
    if (field.type === 'email' && value && !this.isValidEmail(value)) {
        isValid = false;
        errorMessage = 'Please enter a valid email address';
    }
    
    // Password validation
    if (field.type === 'password' && field.name === 'password' && value) {
        if (value.length < 6) {
            isValid = false;
            errorMessage = 'Password must be at least 6 characters long';
        }
    }
    
    // Confirm password validation
    if (field.name === 'confirmPassword') {
        const passwordField = document.querySelector('input[name="password"]');
        if (passwordField && value !== passwordField.value) {
            isValid = false;
            errorMessage = 'Passwords do not match';
        }
    }
    
    if (!isValid) {
        field.classList.add('error');
        const errorEl = document.createElement('span');
        errorEl.className = 'error-text';
        errorEl.textContent = errorMessage;
        field.parentNode.appendChild(errorEl);
    }
    
    return isValid;
};

BlogApp.validateForm = function(form) {
    const fields = form.querySelectorAll('input[required], textarea[required]');
    let isValid = true;
    
    fields.forEach(field => {
        if (!this.validateField(field)) {
            isValid = false;
        }
    });
    
    return isValid;
};

BlogApp.isValidEmail = function(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

// ===========================================
// Password Toggle Functionality
// ===========================================

BlogApp.setupPasswordToggle = function() {
    const toggleButtons = document.querySelectorAll('.password-toggle');
    
    toggleButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const input = button.parentNode.querySelector('input[type="password"], input[type="text"]');
            const icon = button.querySelector('i');
            
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
};

// ===========================================