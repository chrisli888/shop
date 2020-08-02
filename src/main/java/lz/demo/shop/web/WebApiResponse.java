package lz.demo.shop.web;


import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Data;
import lz.demo.shop.domain.ServiceResult;

@Data
public class WebApiResponse {
    private transient HttpResponseStatus status = HttpResponseStatus.OK;
    private int code;
    private String desc;
    private String responseData;

    public WebApiResponse()
    {

    }

    public WebApiResponse(ServiceResult serviceResult)
    {
        code = serviceResult.getCode();
        desc = serviceResult.getDesc();
        responseData = JSONObject.toJSONString(serviceResult);
    }
}
