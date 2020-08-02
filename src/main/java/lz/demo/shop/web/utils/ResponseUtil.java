package lz.demo.shop.web.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;


import lz.demo.shop.web.WebApiResponse;


public class ResponseUtil {


    public static void response(ChannelHandlerContext ctx, FullHttpRequest request, WebApiResponse webApiResponse)
    {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        String repData = webApiResponse.getResponseData();
        byte[] jsonBytebytes = repData.getBytes();
        ByteBuf content = Unpooled.wrappedBuffer(jsonBytebytes);

        HttpResponseStatus status = webApiResponse.getStatus();
        FullHttpResponse response;
        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status,content);


        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON+";"+HttpHeaderValues.CHARSET+"=UTF-8");

        if(!keepAlive)
        {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        }else{
            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }
    }
}
