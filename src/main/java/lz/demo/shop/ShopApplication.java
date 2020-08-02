package lz.demo.shop;


import lz.demo.shop.web.router.HttpRouter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Resource;


@SpringBootApplication
public class ShopApplication {

/*    @Resource
    private NettyServer nettyServer;*/

//    @Resource
//    private HttpRouter httpRouter;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ShopApplication.class, args);
        HttpRouter httpRouter = context.getBean(HttpRouter.class);
        httpRouter.loadRouter(context);

        NettyServer nettyServer = context.getBean(NettyServer.class);
        nettyServer.start();
    }
/*    @Override
    public void run(String... args) throws Exception {


        nettyServer.start();
    }*/

}
