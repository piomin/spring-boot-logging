package pl.piomin.logging.reactive.interceptor;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import static net.logstash.logback.argument.StructuredArguments.value;

public class ResponseLoggingInterceptor extends ServerHttpResponseDecorator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);

    private long startTime;
    private boolean logHeaders;
    private String requestId;

    public ResponseLoggingInterceptor(ServerHttpResponse delegate, long startTime, boolean logHeaders, String requestId) {
        super(delegate);
        this.startTime = startTime;
        this.logHeaders = logHeaders;
        this.requestId = requestId;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        Flux<DataBuffer> buffer = Flux.from(body);
        return super.writeWith(buffer.doOnNext(dataBuffer -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                String bodyRes = IOUtils.toString(baos.toByteArray(), "UTF-8");
                if (logHeaders)
                    LOGGER.info("Response({} ms): id={}, status={}, headers={}, payload={}, audit={}", value("X-Response-Time", System.currentTimeMillis() - startTime), requestId,
                            value("X-Response-Status", getStatusCode().value()), getDelegate().getHeaders(), bodyRes, value("audit", true));
                else
                    LOGGER.info("Response({} ms): id={}, status={}, payload={}, audit={}", value("X-Response-Time", System.currentTimeMillis() - startTime), requestId,
                            value("X-Response-Status", getStatusCode().value()), bodyRes, value("audit", true));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
