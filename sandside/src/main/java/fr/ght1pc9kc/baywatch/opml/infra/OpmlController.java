package fr.ght1pc9kc.baywatch.opml.infra;

import fr.ght1pc9kc.baywatch.opml.api.OpmlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/opml")
public class OpmlController {

    private final OpmlService opmlService;

    @ResponseBody
    @GetMapping("/export/baywatch.opml")
    public Mono<ResponseEntity<Resource>> exportOpml() {
        String fileName = String.format("baywatch-%s.opml", LocalDateTime.now());
        return opmlService.opmlExport()
                .switchIfEmpty(Mono.just((InputStream) new ByteArrayInputStream("empty".getBytes(StandardCharsets.UTF_8))))
                .map(is -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .body((Resource) new InputStreamResource(is)))
                .doOnNext(ignore -> log.debug("Start OPML download"))
                .doOnTerminate(() -> log.debug("Terminate OPML download"))
                .doOnError(e -> log.error("STACKTRACE", e));
    }

    @PostMapping("/import")
    public Mono<Void> importOpml(@RequestPart("opml") Mono<FilePart> opmlFilePart) {
        Flux<DataBuffer> data = opmlFilePart.flatMapMany(Part::content);
        return opmlService.opmlImport(data);
    }
}
