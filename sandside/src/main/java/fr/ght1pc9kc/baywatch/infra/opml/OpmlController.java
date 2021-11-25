package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/opml")
public class OpmlController {

    private final OpmlService opmlService;

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/export/baywatch.opml")
    public Mono<ResponseEntity<Resource>> downloadCsv() {
        String fileName = String.format("baywatch-%s.opml", LocalDateTime.now());
        return opmlService.export().map(is -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(new InputStreamResource(is)));
    }
}
