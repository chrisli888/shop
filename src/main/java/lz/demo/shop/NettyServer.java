package lz.demo.shop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;


import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class NettyServer implements DisposableBean {

    @Autowired
    ServerChannelInitializer serverChannelInitializer;

    private Channel channel;

    @Value("${netty.port}")
    private Integer port;

    @Value("${netty.host}")
    private String host;




    public void destroy()
    {
        if(channel != null)
        {
            channel.close();
        }
    }

    public void start() {
        log.info("NettyServer do start");

        //boss事件轮询线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //worker事件轮询线程组
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR,true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(this.serverChannelInitializer);

            ChannelFuture  future = b.bind(host,port).sync();
            this.channel = future.channel();
            log.info("NettyServer start port ["  + port + "] OK");
            future.channel().closeFuture().sync();

        }catch (InterruptedException e)
        {
            log.info("NettyServer start Faild"  + port);
        }finally {
            //关闭主线程组
            bossGroup.shutdownGracefully();
            //关闭工作线程组
            workGroup.shutdownGracefully();
        }
    }
}
