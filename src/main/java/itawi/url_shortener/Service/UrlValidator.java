package itawi.url_shortener.Service;

import itawi.url_shortener.Utils.RegexConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.util.InvalidUrlException;

import java.util.regex.Pattern;

@Service
public class UrlValidator {

    private static final Pattern URL_PATTERN = Pattern.compile(RegexConstants.URL_REGEX);

    public void validate(String url) {
        if (!URL_PATTERN.matcher(url).matches()) {
            throw new InvalidUrlException(url);
        }
    }
}
