package itawi.url_shortener.controller;

import itawi.url_shortener.aspect.RateLimit;
import itawi.url_shortener.dto.Request.ShortenRequest;
import itawi.url_shortener.dto.Response.ShortenResponse;
import itawi.url_shortener.dto.Response.UrlStatsResponse;
import itawi.url_shortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/urls")
@RequiredArgsConstructor
@Tag(name = "URL Shortener API", description = "Endpoints for URL shortening and resolution")
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlShortenerService urlService;

    @PostMapping
    @RateLimit(100) // Increased from default 10 to 100 requests per minute for development
    @Operation(summary = "Shorten a URL", description = "Shortens a given URL and returns the shortened URL details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL shortened successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShortenResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid URL or other validation errors")
    })
    public ResponseEntity<ShortenResponse> shortenUrl(@Valid @RequestBody ShortenRequest request) {
        return ResponseEntity.ok(urlService.shortenUrl(request));
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Resolve a shortened URL", description = "Resolves a shortened URL to its original URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirects to the original URL"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public RedirectView resolveUrl(@Parameter(description = "The short code of the URL", required = true) @PathVariable String shortCode) {
        String originalUrl = urlService.resolveUrl(shortCode);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(originalUrl);
        return redirectView;
    }

    @GetMapping("/{shortCode}/stats")
    @RateLimit(200) // Increased from default to 200 requests per minute for stats
    @Operation(summary = "Get URL statistics", description = "Get statistics for a shortened URL without incrementing access count.")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.getUrlStats(shortCode));
    }
}