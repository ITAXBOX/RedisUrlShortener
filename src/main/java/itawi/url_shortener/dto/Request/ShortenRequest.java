package itawi.url_shortener.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record ShortenRequest(
        @NotBlank(message = "Original URL is required")
        String url,

        String customAlias,

        @PositiveOrZero(message = "Expiration seconds must be positive or zero")
        Long expirationSeconds
) {
}