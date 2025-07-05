// URL Shortener Application JavaScript
class URLShortenerApp {
    constructor() {
        this.baseUrl = window.location.origin;
        this.apiUrl = `${this.baseUrl}/api/v1/urls`;
        this.currentShortCode = null;

        this.initializeElements();
        this.bindEvents();
    }

    initializeElements() {
        // Form elements
        this.shortenForm = document.getElementById('shortenForm');
        this.originalUrlInput = document.getElementById('originalUrl');
        this.customAliasInput = document.getElementById('customAlias');
        this.expirationSecondsInput = document.getElementById('expirationSeconds');
        this.shortenBtn = document.getElementById('shortenBtn');

        // Toggle elements
        this.toggleAdvancedBtn = document.getElementById('toggleAdvanced');
        this.advancedOptions = document.getElementById('advancedOptions');

        // Result elements
        this.resultSection = document.getElementById('resultSection');
        this.shortUrlInput = document.getElementById('shortUrl');
        this.originalUrlResult = document.getElementById('originalUrlResult');
        this.copyBtn = document.getElementById('copyBtn');
        this.viewStatsBtn = document.getElementById('viewStatsBtn');
        this.shortenAnotherBtn = document.getElementById('shortenAnotherBtn');

        // Stats elements
        this.statsSection = document.getElementById('statsSection');
        this.closeStatsBtn = document.getElementById('closeStatsBtn');
        this.accessCountElement = document.getElementById('accessCount');
        this.statsShortUrlElement = document.getElementById('statsShortUrl');
        this.statsOriginalUrlElement = document.getElementById('statsOriginalUrl');

        // Resolve elements
        this.resolveForm = document.getElementById('resolveForm');
        this.resolveInput = document.getElementById('resolveInput');
        this.resolvedUrlInput = document.getElementById('resolvedUrl');
        this.resolveBtn = document.getElementById('resolveBtn');
        this.copyResolvedBtn = document.getElementById('copyResolvedBtn');
        this.visitResolvedBtn = document.getElementById('visitResolvedBtn');
        this.resolveResultSection = document.getElementById('resolveResultSection');

        // Utility elements
        this.loadingOverlay = document.getElementById('loadingOverlay');
        this.toastContainer = document.getElementById('toastContainer');
    }

    bindEvents() {
        // Form submission
        this.shortenForm.addEventListener('submit', (e) => this.handleShortenUrl(e));

        // Toggle advanced options
        this.toggleAdvancedBtn.addEventListener('click', () => this.toggleAdvancedOptions());

        // Copy button
        this.copyBtn.addEventListener('click', () => this.copyToClipboard());

        // View stats button
        this.viewStatsBtn.addEventListener('click', () => this.viewStats());

        // Close stats button
        this.closeStatsBtn.addEventListener('click', () => this.closeStats());

        // Shorten another button
        this.shortenAnotherBtn.addEventListener('click', () => this.resetForm());

        // Resolve form submission
        this.resolveForm.addEventListener('submit', (e) => this.handleResolveUrl(e));

        // Copy resolved URL button
        this.copyResolvedBtn.addEventListener('click', () => this.copyResolvedToClipboard());

        // Visit resolved URL button
        this.visitResolvedBtn.addEventListener('click', () => this.visitResolvedUrl());

        // Smooth scrolling for navigation links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', (e) => {
                e.preventDefault();
                const target = document.querySelector(anchor.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({ behavior: 'smooth' });
                }
            });
        });
    }

    async handleShortenUrl(event) {
        event.preventDefault();

        const originalUrl = this.originalUrlInput.value.trim();
        const customAlias = this.customAliasInput.value.trim();
        const expirationSeconds = this.expirationSecondsInput.value.trim();

        if (!originalUrl) {
            this.showToast('Please enter a URL to shorten', 'error');
            return;
        }

        // Validate URL format
        if (!this.isValidUrl(originalUrl)) {
            this.showToast('Please enter a valid URL (including http:// or https://)', 'error');
            return;
        }

        const requestData = {
            url: originalUrl,
            customAlias: customAlias || null,
            expirationSeconds: expirationSeconds ? parseInt(expirationSeconds) : null
        };

        try {
            this.showLoading(true);
            this.setShortenButtonState(true);

            const response = await fetch(this.apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayResult(data);
            this.showToast('URL shortened successfully!', 'success');

        } catch (error) {
            console.error('Error shortening URL:', error);
            this.showToast(error.message || 'Failed to shorten URL. Please try again.', 'error');
        } finally {
            this.showLoading(false);
            this.setShortenButtonState(false);
        }
    }

    async viewStats() {
        if (!this.currentShortCode) {
            this.showToast('No short code available for stats', 'error');
            return;
        }

        try {
            this.showLoading(true);

            const response = await fetch(`${this.apiUrl}/${this.currentShortCode}/stats`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            this.displayStats(data);

        } catch (error) {
            console.error('Error fetching stats:', error);
            this.showToast('Failed to load statistics. Please try again.', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    async handleResolveUrl(event) {
        event.preventDefault();
        const input = this.resolveInput.value.trim();
        if (!input) {
            this.showToast('Please enter a short code or URL to resolve', 'error');
            return;
        }

        const shortCode = input.includes('/') ? input.split('/').pop() : input;

        try {
            this.showLoading(true);
            this.setResolveButtonState(true);

            const response = await fetch(`${this.apiUrl}/${shortCode}/stats`);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error(`Short code "${shortCode}" not found or has expired`);
                } else if (response.status === 429) {
                    throw new Error('Rate limit exceeded. Please try again later');
                } else {
                    throw new Error(`Server error: ${response.status}`);
                }
            }

            const data = await response.json();
            this.resolvedUrlInput.value = data.originalUrl;
            this.resolveResultSection.classList.remove('hidden');
            this.showToast('URL resolved successfully!', 'success');

        } catch (error) {
            console.error('Error resolving URL:', error);
            this.showToast(error.message || 'Failed to resolve URL', 'error');
            this.resolveResultSection.classList.add('hidden'); // Hide result section on error
        } finally {
            this.showLoading(false);
            this.setResolveButtonState(false);
        }
    }

    displayResult(data) {
        this.shortUrlInput.value = data.shortUrl;
        this.originalUrlResult.value = data.originalUrl;

        // Extract short code from the URL for stats
        this.currentShortCode = data.shortUrl.split('/').pop();

        this.resultSection.classList.remove('hidden');
        this.statsSection.classList.add('hidden');

        // Scroll to result
        this.resultSection.scrollIntoView({ behavior: 'smooth' });
    }

    displayStats(data) {
        this.accessCountElement.textContent = data.accessCount || 0;
        this.statsShortUrlElement.textContent = data.shortUrl;
        this.statsOriginalUrlElement.textContent = data.originalUrl;

        this.statsSection.classList.remove('hidden');
        this.statsSection.scrollIntoView({ behavior: 'smooth' });
    }

    closeStats() {
        this.statsSection.classList.add('hidden');
    }

    toggleAdvancedOptions() {
        const isVisible = this.advancedOptions.classList.contains('show');

        if (isVisible) {
            this.advancedOptions.classList.remove('show');
            this.toggleAdvancedBtn.innerHTML = '<i class="fas fa-cog"></i> Advanced Options';
        } else {
            this.advancedOptions.classList.add('show');
            this.toggleAdvancedBtn.innerHTML = '<i class="fas fa-cog"></i> Hide Advanced';
        }
    }

    async copyToClipboard() {
        try {
            await navigator.clipboard.writeText(this.shortUrlInput.value);
            this.showToast('Copied to clipboard!', 'success');

            // Visual feedback
            const originalIcon = this.copyBtn.innerHTML;
            this.copyBtn.innerHTML = '<i class="fas fa-check"></i>';
            this.copyBtn.style.background = '#48bb78';

            setTimeout(() => {
                this.copyBtn.innerHTML = originalIcon;
                this.copyBtn.style.background = '';
            }, 2000);

        } catch (error) {
            // Fallback for older browsers
            this.shortUrlInput.select();
            document.execCommand('copy');
            this.showToast('Copied to clipboard!', 'success');
        }
    }

    copyResolvedToClipboard() {
        navigator.clipboard.writeText(this.resolvedUrlInput.value);
        this.showToast('Copied to clipboard!', 'success');
    }

    visitResolvedUrl() {
        window.open(this.resolvedUrlInput.value, '_blank');
    }

    resetForm() {
        this.shortenForm.reset();
        this.resultSection.classList.add('hidden');
        this.statsSection.classList.add('hidden');
        this.advancedOptions.classList.remove('show');
        this.toggleAdvancedBtn.innerHTML = '<i class="fas fa-cog"></i> Advanced Options';
        this.currentShortCode = null;

        // Focus on URL input
        this.originalUrlInput.focus();

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    showLoading(show) {
        if (show) {
            this.loadingOverlay.classList.remove('hidden');
        } else {
            this.loadingOverlay.classList.add('hidden');
        }
    }

    setShortenButtonState(loading) {
        if (loading) {
            this.shortenBtn.disabled = true;
            this.shortenBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span>Shortening...</span>';
        } else {
            this.shortenBtn.disabled = false;
            this.shortenBtn.innerHTML = '<i class="fas fa-compress-arrows-alt"></i> <span>Shorten URL</span>';
        }
    }

    setResolveButtonState(loading) {
        if (loading) {
            this.resolveBtn.disabled = true;
            this.resolveBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span>Resolving...</span>';
        } else {
            this.resolveBtn.disabled = false;
            this.resolveBtn.innerHTML = '<i class="fas fa-search"></i> <span>Resolve URL</span>';
        }
    }

    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <div style="display: flex; align-items: center; gap: 0.5rem;">
                <i class="fas fa-${this.getToastIcon(type)}"></i>
                <span>${message}</span>
            </div>
        `;

        this.toastContainer.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.animation = 'slideOutRight 0.3s ease forwards';
                setTimeout(() => toast.remove(), 300);
            }
        }, 5000);

        // Click to dismiss
        toast.addEventListener('click', () => {
            toast.style.animation = 'slideOutRight 0.3s ease forwards';
            setTimeout(() => toast.remove(), 300);
        });
    }

    getToastIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || icons.info;
    }

    isValidUrl(string) {
        try {
            const url = new URL(string);
            return url.protocol === 'http:' || url.protocol === 'https:';
        } catch (_) {
            return false;
        }
    }
}

// Additional CSS for toast animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOutRight {
        to {
            opacity: 0;
            transform: translateX(100%);
        }
    }
`;
document.head.appendChild(style);

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new URLShortenerApp();
});

// Add some utility functions for enhanced UX
document.addEventListener('DOMContentLoaded', () => {
    // Add focus states for better accessibility
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('focus', () => {
            input.parentElement.style.transform = 'scale(1.02)';
        });

        input.addEventListener('blur', () => {
            input.parentElement.style.transform = 'scale(1)';
        });
    });

    // Add hover effects to feature cards
    const featureItems = document.querySelectorAll('.feature-item');
    featureItems.forEach(item => {
        item.addEventListener('mouseenter', () => {
            item.style.background = 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)';
        });

        item.addEventListener('mouseleave', () => {
            item.style.background = '';
        });
    });
});
