package pl.piomin.logging.wrapper;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

public class SpringResponseWrapper extends HttpServletResponseWrapper {

    private CharArrayWriter output;

    public SpringResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new CharArrayWriter();

    }

    public PrintWriter getWriter() {
        return new PrintWriter(output);
    }

    public String toString() {
        return output.toString();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            private OutputStream outputStream;
            private ByteArrayOutputStream copy;


            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
                copy.write(b);
            }

            public byte[] getCopy() {
                return copy.toByteArray();
            }

        };
    }
}
