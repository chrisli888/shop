package lz.demo.shop.web.label;

import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpLabel {
    private String uri;
    private HttpMethod method;
}
