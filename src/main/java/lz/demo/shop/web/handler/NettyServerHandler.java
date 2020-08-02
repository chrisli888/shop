package lz.demo.shop.web.handler;



import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import lz.demo.shop.domain.ServiceResult;
import lz.demo.shop.web.WebApiResponse;
import lz.demo.shop.web.action.Action;
import lz.demo.shop.web.label.HttpLabel;
import lz.demo.shop.web.router.HttpRouter;
import lz.demo.shop.web.utils.ResponseUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyServerHandler  extends ChannelInboundHandlerAdapter  {

    @Resource
    HttpRouter httpRouter;

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof FullHttpRequest) {
            manual(ctx, (FullHttpRequest) msg);
        }
    }

    protected void manual(final ChannelHandlerContext ctx, final FullHttpRequest req) {
        System.out.println("收到" + req);
        String uri = req.uri();
        if(uri.contains("?")) {
            uri = uri.substring(0,uri.indexOf("?"));
        }
        WebApiResponse webApiResponse = null;
//        System.out.println(req.method());
        Action<WebApiResponse> action = httpRouter.getRoute(new HttpLabel(uri,req.method()));
        if(action != null)
        {
            if(action.isInjectionFullhttprequest())
            {
                webApiResponse = action.call(req);
            }else {
                webApiResponse = action.call();
            }
        }else{
            webApiResponse = new WebApiResponse(new ServiceResult(20000,"error"));
        }
        ResponseUtil.response(ctx,req,webApiResponse);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        log.info("Channel active......");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        cause.printStackTrace();
        ctx.close();
    }

}
