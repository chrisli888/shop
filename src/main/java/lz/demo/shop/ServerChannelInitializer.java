package lz.demo.shop;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import lz.demo.shop.web.handler.NettyServerHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> implements InitializingBean {

    @Resource
    NettyServerHandler nettyServerHandler;

    private EventExecutorGroup businessGroup;

    @Value("${netty.threadSize}")
    private int threadSize;

    @Value("${netty.maxContentLength}")
    private int maxContentLength;

    @Value("${netty.timeout}")
    private int timeout;

    public ServerChannelInitializer(){

    }


    @Override
    protected void initChannel(SocketChannel ch){
        log.info("init Channel start");
        ChannelPipeline pipeline = ch.pipeline();
        //添加编解码

        pipeline.addLast("http-decoder", new HttpRequestDecoder());
        pipeline.addLast("readCheck", new IdleStateHandler(timeout,0,0));

        pipeline.addLast("http-encoder", new HttpResponseEncoder());
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(maxContentLength));


        pipeline.addLast(this.businessGroup,new ChannelHandler[]{this.nettyServerHandler});
        log.info("init Channel end");
    }

    @Override
    public void afterPropertiesSet(){
        this.businessGroup = new DefaultEventExecutorGroup(threadSize);
    }
}
