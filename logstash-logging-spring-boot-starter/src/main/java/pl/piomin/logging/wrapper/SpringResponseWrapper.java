package pl.piomin.logging.wrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class SpringResponseWrapper extends HttpServletResponseWrapper {

	private ServletOutputStream outputStream;
	private PrintWriter writer;
	private ServletOutputStreamWrapper copier;

	public SpringResponseWrapper(HttpServletResponse response) throws IOException {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called on this response.");
		}

		if (outputStream == null) {
			outputStream = getResponse().getOutputStream();
			copier = new ServletOutputStreamWrapper(outputStream);
		}

		return copier;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (outputStream != null) {
			throw new IllegalStateException("getOutputStream() has already been called on this response.");
		}

		if (writer == null) {
			copier = new ServletOutputStreamWrapper(getResponse().getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
		}

		return writer;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (writer != null) {
			writer.flush();
		}
		else if (outputStream != null) {
			copier.flush();
		}
	}

	public byte[] getContentAsByteArray() {
		if (copier != null) {
			return copier.getCopy();
		}
		else {
			return new byte[0];
		}
	}

	public Map<String, String> getAllHeaders() {
		final Map<String, String> headers = new HashMap<>();
		getHeaderNames().forEach(it -> headers.put(it, getHeader(it)));
		return  headers;
	}

}