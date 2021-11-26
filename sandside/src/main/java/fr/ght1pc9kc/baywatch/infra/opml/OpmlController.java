package fr.ght1pc9kc.baywatch.infra.opml;

import fr.ght1pc9kc.baywatch.api.opml.OpmlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/opml")
public class OpmlController {

    private final OpmlService opmlService;

    @ResponseBody
//    @PreAuthorize("isAuthenticated()")
    @GetMapping("/export/baywatch.opml")
    public Mono<ResponseEntity<Resource>> downloadCsv() {
        String fileName = String.format("baywatch-%s.opml", LocalDateTime.now());
        return opmlService.export()
                .switchIfEmpty(Mono.just((InputStream) new ByteArrayInputStream("empty".getBytes(StandardCharsets.UTF_8))))
                .map(is -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .body((Resource) new InputStreamResource(is))
                ).doFirst(() -> log.debug("Start OPML download")
                ).doOnTerminate(() -> log.debug("Terminate OPML download")
                ).doOnError(e -> log.error("STACKTRACE", e));
    }
}
