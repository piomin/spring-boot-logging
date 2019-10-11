package pl.piomin.logging.reactive.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import static net.logstash.logback.argument.StructuredArguments.value;

public class ResponseLoggingInterceptor extends ServerHttpResponseDecorator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseLoggingInterceptor.class);

	private long startTime;

	public ResponseLoggingInterceptor(ServerHttpResponse delegate, long startTime) {
		super(delegate);
		this.startTime = startTime;
	}

	@Override
	public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
		body.subscribe(new Subscriber<DataBuffer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				LOGGER.info("onSubscribe ");
			}

			@Override
			public void onNext(DataBuffer dataBuffer) {
				LOGGER.info("onNext ");
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					Channels.newChannel(baos).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
					String body = IOUtils.toString(baos.toByteArray(), "UTF-8");
					LOGGER.info("Response({} ms): status={}, payload={}, audit={}", value("X-Response-Time", System.currentTimeMillis() - startTime),
							value("X-Response-Status", getStatusCode().value()), body, value("audit", true));
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(Throwable throwable) {
				LOGGER.info("onError ");
			}

			@Override
			public void onComplete() {
				LOGGER.info("onComplete ");
			}
		});
		return super.writeWith(body);
	}
}
