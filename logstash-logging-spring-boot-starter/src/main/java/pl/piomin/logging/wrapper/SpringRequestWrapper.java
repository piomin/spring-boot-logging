package pl.piomin.logging.wrapper;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpringRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public SpringRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStream() {
            public boolean isFinished() {
                return false;
            }

            public boolean isReady() {
                return true;
            }

            public void setReadListener(ReadListener readListener) {

            }

            ByteArrayInputStream byteArray = new ByteArrayInputStream(body);

            @Override
            public int read() throws IOException {
                return byteArray.read();
            }
        };
    }

    public Map<String, String> getAllHeaders() {
        final Map<String, String> headers = new HashMap<>();
        Collections.list(getHeaderNames()).forEach(it -> headers.put(it, getHeader(it)));
        return  headers;
    }
}
