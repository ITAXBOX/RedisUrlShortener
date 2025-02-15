package itawi.url_shortener.Controller;

import itawi.url_shortener.Aspect.RateLimit;
import itawi.url_shortener.DTO.Request.ShortenRequest;
import itawi.url_shortener.DTO.Response.ShortenResponse;
import itawi.url_shortener.Service.UrlShortenerService;
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
public class UrlController {

    private final UrlShortenerService urlService;

    @PostMapping
    @RateLimit
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
    @Operation(summary = "Get URL statistics", description = "Retrieves statistics for a shortened URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL statistics retrieved successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShortenResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    public ResponseEntity<ShortenResponse> getStats(@Parameter(description = "The short code of the URL", required = true) @PathVariable String shortCode) {
        String originalUrl = urlService.resolveUrl(shortCode);
        Long accessCount = urlService.getAccessCount(shortCode);
        String shortUrl = urlService.buildShortUrl(shortCode);
        return ResponseEntity.ok(new ShortenResponse(shortUrl, originalUrl, accessCount));
    }
}