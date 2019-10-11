package pl.piomin.logging.reactive.filter;

import pl.piomin.logging.reactive.interceptor.RequestLoggingInterceptor;
import pl.piomin.logging.reactive.interceptor.ResponseLoggingInterceptor;
import reactor.core.publisher.Mono;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

public class ReactiveSpringLoggingFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		final long startTime = System.currentTimeMillis();
		ServerWebExchangeDecorator exchangeDecorator = new ServerWebExchangeDecorator(exchange) {
			@Override
			public ServerHttpRequest getRequest() {
				return new RequestLoggingInterceptor(super.getRequest());
			}

			@Override
			public ServerHttpResponse getResponse() {
				return new ResponseLoggingInterceptor(super.getResponse(), startTime);
			}
		};
		return chain.filter(exchangeDecorator);
	}

}
